package com.cebolao.lotofacil.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import com.cebolao.lotofacil.ui.theme.AccentPalette
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

private const val TAG = "UserPreferencesRepo"
private const val DEFAULT_THEME_MODE = "auto"

private object PreferenceKeys {
    val PINNED_GAMES = stringSetPreferencesKey("pinned_games")
    val DYNAMIC_HISTORY = stringSetPreferencesKey("dynamic_history")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val ACCENT_PALETTE = stringPreferencesKey("accent_palette")
}

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserPreferencesRepository {

    override val pinnedGames: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            handleError(exception, "reading pinned games")
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[PreferenceKeys.PINNED_GAMES] ?: emptySet()
        }

    override suspend fun savePinnedGames(games: Set<String>) {
        withContext(ioDispatcher) {
            try {
                context.dataStore.edit { preferences ->
                    preferences[PreferenceKeys.PINNED_GAMES] = games
                }
                Log.d(TAG, "Saved ${games.size} pinned games")
            } catch (e: IOException) {
                handleError(e, "saving pinned games")
            }
        }
    }

    override suspend fun getHistory(): Set<String> = withContext(ioDispatcher) {
        try {
            val preferences = context.dataStore.data.firstOrNull() ?: emptyPreferences()
            preferences[PreferenceKeys.DYNAMIC_HISTORY] ?: emptySet()
        } catch (e: Exception) {
            handleError(e, "getting history")
            emptySet()
        }
    }

    override suspend fun addDynamicHistoryEntries(newHistoryEntries: Set<String>) {
        withContext(ioDispatcher) {
            val validEntries = newHistoryEntries.filter { it.isNotBlank() }.toSet()
            if (validEntries.isEmpty()) return@withContext

            try {
                context.dataStore.edit { preferences ->
                    val currentHistory = preferences[PreferenceKeys.DYNAMIC_HISTORY] ?: emptySet()
                    preferences[PreferenceKeys.DYNAMIC_HISTORY] = currentHistory + validEntries
                    Log.d(TAG, "Added ${validEntries.size} valid history entries")
                }
            } catch (e: IOException) {
                handleError(e, "adding dynamic history entries")
            }
        }
    }

    override val themeMode: Flow<String> = context.dataStore.data
        .catch { exception ->
            handleError(exception, "reading theme mode")
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[PreferenceKeys.THEME_MODE] ?: DEFAULT_THEME_MODE
        }

    override suspend fun setThemeMode(mode: String) {
        withContext(ioDispatcher) {
            try {
                context.dataStore.edit { preferences ->
                    preferences[PreferenceKeys.THEME_MODE] = mode
                }
            } catch (e: IOException) {
                handleError(e, "setting theme mode")
            }
        }
    }

    override val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            handleError(exception, "reading onboarding status")
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[PreferenceKeys.ONBOARDING_COMPLETED] ?: false
        }


    override suspend fun setHasCompletedOnboarding(completed: Boolean) {
        withContext(ioDispatcher) {
            try {
                context.dataStore.edit { preferences ->
                    preferences[PreferenceKeys.ONBOARDING_COMPLETED] = completed
                }
            } catch (e: IOException) {
                handleError(e, "setting onboarding status")
            }
        }
    }

    override val accentPalette: Flow<String> = context.dataStore.data
        .catch { exception ->
            handleError(exception, "reading accent palette")
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[PreferenceKeys.ACCENT_PALETTE] ?: AccentPalette.DEFAULT.name
        }

    override suspend fun setAccentPalette(paletteName: String) {
        withContext(ioDispatcher) {
            try {
                context.dataStore.edit { preferences ->
                    preferences[PreferenceKeys.ACCENT_PALETTE] = paletteName
                }
            } catch (e: IOException) {
                handleError(e, "setting accent palette")
            }
        }
    }

    private fun handleError(exception: Throwable, contextMessage: String) {
        Log.e(TAG, "Error $contextMessage", exception)
    }
}