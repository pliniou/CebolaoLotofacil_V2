package com.cebolao.lotofacil.data.repository

import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.di.STATE_IN_TIMEOUT_MS
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationScope private val repositoryScope: CoroutineScope,
    private val json: Json
) : GameRepository {

    private val gamesMutex = Mutex()
    private val _games = MutableStateFlow<ImmutableList<LotofacilGame>>(persistentListOf())
    override val games: StateFlow<ImmutableList<LotofacilGame>> = _games.asStateFlow()

    override val pinnedGames: StateFlow<ImmutableList<LotofacilGame>> = games
        .map { gamesList -> gamesList.filter { it.isPinned }.toImmutableList() }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            initialValue = persistentListOf()
        )

    init {
        repositoryScope.launch {
            val pinnedGameStrings = userPreferencesRepository.pinnedGames.first()
            val loadedGames = pinnedGameStrings.mapNotNull {
                try {
                    json.decodeFromString<LotofacilGame>(it)
                } catch (_: Exception) {
                    null
                }
            }
            _games.value = loadedGames.toImmutableList()
        }
    }

    override suspend fun addGeneratedGames(newGames: List<LotofacilGame>) {
        gamesMutex.withLock {
            _games.update { currentGames ->
                (currentGames + newGames)
                    .distinctBy { it.numbers }
                    .sortedWith(compareByDescending<LotofacilGame> { it.isPinned }.thenByDescending { it.creationTimestamp })
                    .toImmutableList()
            }
        }
        if (newGames.any { it.isPinned }) {
            persistPinnedGames()
        }
    }

    override suspend fun clearUnpinnedGames() {
        gamesMutex.withLock {
            _games.update { currentGames ->
                currentGames.filter { it.isPinned }.toImmutableList()
            }
        }
    }

    override suspend fun togglePinState(gameToToggle: LotofacilGame) {
        gamesMutex.withLock {
            _games.update { currentGames ->
                val updatedGame = gameToToggle.copy(isPinned = !gameToToggle.isPinned)
                currentGames
                    .map { if (it.numbers == updatedGame.numbers) updatedGame else it }
                    .sortedWith(compareByDescending<LotofacilGame> { it.isPinned }.thenByDescending { it.creationTimestamp })
                    .toImmutableList()
            }
        }
        persistPinnedGames()
    }

    override suspend fun deleteGame(gameToDelete: LotofacilGame) {
        gamesMutex.withLock {
            _games.update { currentGames ->
                currentGames.filterNot { it.numbers == gameToDelete.numbers }.toImmutableList()
            }
        }
        if (gameToDelete.isPinned) {
            persistPinnedGames()
        }
    }

    private suspend fun persistPinnedGames() {
        val pinned = _games.value.filter { it.isPinned }
        val pinnedAsJson = pinned.map { json.encodeToString(it) }.toSet()
        userPreferencesRepository.savePinnedGames(pinnedAsJson)
    }

    override suspend fun exportGames(): String {
        return json.encodeToString(_games.value)
    }

    override suspend fun importGames(serialized: String): Int {
        val parsed = try {
            json.decodeFromString<List<LotofacilGame>>(serialized.trim())
        } catch (_: Exception) {
            return 0
        }

        if (parsed.isEmpty()) return 0

        gamesMutex.withLock {
            _games.update { currentGames ->
                (currentGames + parsed)
                    .distinctBy { it.numbers }
                    .sortedWith(compareByDescending<LotofacilGame> { it.isPinned }.thenByDescending { it.creationTimestamp })
                    .toImmutableList()
            }
        }
        persistPinnedGames()
        return parsed.size
    }
}