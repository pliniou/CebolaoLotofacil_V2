package com.cebolao.lotofacil.domain.repository

import com.cebolao.lotofacil.data.LotofacilGame
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

interface GameRepository {
    val games: StateFlow<ImmutableList<LotofacilGame>>
    val pinnedGames: StateFlow<ImmutableList<LotofacilGame>>

    suspend fun addGeneratedGames(newGames: List<LotofacilGame>)
    suspend fun clearUnpinnedGames()
    suspend fun togglePinState(gameToToggle: LotofacilGame)
    suspend fun deleteGame(gameToDelete: LotofacilGame)

    // Export/import utilities for sharing and persistence
    suspend fun exportGames(): String
    suspend fun importGames(serialized: String): Int
}