package com.cebolao.lotofacil.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val pinnedGames: Flow<Set<String>>
    val themeMode: Flow<String>
    val hasCompletedOnboarding: Flow<Boolean>
    val accentPalette: Flow<String>

    suspend fun savePinnedGames(games: Set<String>)
    suspend fun getHistory(): Set<String>
    suspend fun addDynamicHistoryEntries(newHistoryEntries: Set<String>)
    suspend fun setThemeMode(mode: String)
    suspend fun setHasCompletedOnboarding(completed: Boolean)
    suspend fun setAccentPalette(paletteName: String)
}