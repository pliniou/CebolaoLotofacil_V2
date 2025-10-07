package com.cebolao.lotofacil.data.repository

import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.ApplicationScope
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationScope private val repositoryScope: CoroutineScope
) : GameRepository {

    private val gamesMutex = Mutex()
    private val _games = MutableStateFlow<ImmutableList<LotofacilGame>>(persistentListOf())
    override val games: StateFlow<ImmutableList<LotofacilGame>> = _games.asStateFlow()

    override val pinnedGames: StateFlow<ImmutableList<LotofacilGame>> = games
        .map { gamesList -> gamesList.filter { it.isPinned }.toImmutableList() }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentListOf()
        )

    init {
        repositoryScope.launch {
            val pinnedGameStrings = userPreferencesRepository.pinnedGames.first()
            val loadedGames = pinnedGameStrings.mapNotNull { LotofacilGame.fromCompactString(it) }
            _games.value = loadedGames.toImmutableList()
        }
    }

    override suspend fun addGeneratedGames(newGames: List<LotofacilGame>) {
        gamesMutex.withLock {
            _games.update { currentGames ->
                val currentPinned = currentGames.filter { it.isPinned }
                (currentPinned + newGames)
                    .distinctBy { it.numbers }
                    .sortedWith(compareBy<LotofacilGame> { !it.isPinned }.thenByDescending { it.creationTimestamp })
                    .toImmutableList()
            }
        }
        persistPinnedGames()
    }

    override suspend fun clearUnpinnedGames() {
        gamesMutex.withLock {
            _games.update { currentGames ->
                currentGames.filter { it.isPinned }.toImmutableList()
            }
        }
        persistPinnedGames()
    }

    override suspend fun togglePinState(gameToToggle: LotofacilGame) {
        val updatedGame = gameToToggle.copy(isPinned = !gameToToggle.isPinned)
        gamesMutex.withLock {
            _games.update { currentGames ->
                currentGames
                    .map { if (it.numbers == updatedGame.numbers) updatedGame else it }
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
        userPreferencesRepository.savePinnedGames(pinned.map { it.toCompactString() }.toSet())
    }

    override suspend fun exportGames(): String {
        // Serializa todos os jogos em linhas, uma por jogo
        return _games.value.joinToString("\n") { it.toCompactString() }
    }

    override suspend fun importGames(serialized: String): Int {
        val lines = serialized.lines()
        val parsed = lines.mapNotNull { LotofacilGame.fromCompactString(it.trim()) }
        if (parsed.isEmpty()) return 0
        addGeneratedGames(parsed)
        return parsed.size
    }
}