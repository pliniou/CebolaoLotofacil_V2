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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val DATASTORE_NAME = "user_prefs"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

private const val TAG = "UserPreferencesRepo"
const val THEME_MODE_AUTO = "auto"
const val THEME_MODE_DARK = "dark"

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

    private val dataStore = context.dataStore

    override val pinnedGames: Flow<Set<String>> = dataStore.data
        .catch { exception ->
            handleError(exception, "reading pinned games")
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[PreferenceKeys.PINNED_GAMES] ?: emptySet()
        }

    override suspend fun savePinnedGames(games: Set<String>) {
        withContext(ioDispatcher) {
            runCatching {
                dataStore.edit { preferences ->
                    preferences[PreferenceKeys.PINNED_GAMES] = games
                }
                Log.d(TAG, "Saved ${games.size} pinned games")
            }.onFailure { e ->
                handleError(e, "saving pinned games")
            }
        }
    }

    override suspend fun getHistory(): Set<String> = withContext(ioDispatcher) {
        runCatching {
            val preferences = dataStore.data.first()
            preferences[PreferenceKeys.DYNAMIC_HISTORY] ?: emptySet()
        }.getOrElse { e ->
            handleError(e, "getting history")
            emptySet()
        }
    }

    override suspend fun addDynamicHistoryEntries(newHistoryEntries: Set<String>) {
        withContext(ioDispatcher) {
            val validEntries = newHistoryEntries.filter { it.isNotBlank() }.toSet()
            if (validEntries.isEmpty()) return@withContext

            runCatching {
                dataStore.edit { preferences ->
                    val currentHistory = preferences[PreferenceKeys.DYNAMIC_HISTORY] ?: emptySet()
                    preferences[PreferenceKeys.DYNAMIC_HISTORY] = currentHistory + validEntries
                    Log.d(TAG, "Added ${validEntries.size} valid history entries. Total: ${currentHistory.size + validEntries.size}")
                }
            }.onFailure { e ->
                handleError(e, "adding dynamic history entries")
            }
        }
    }

    override val themeMode: Flow<String> = dataStore.data
        .catch { exception ->
            handleError(exception, "reading theme mode")
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[PreferenceKeys.THEME_MODE] ?: THEME_MODE_AUTO
        }

    override suspend fun setThemeMode(mode: String) {
        withContext(ioDispatcher) {
            runCatching {
                dataStore.edit { preferences ->
                    preferences[PreferenceKeys.THEME_MODE] = mode
                }
            }.onFailure { e ->
                handleError(e, "setting theme mode")
            }
        }
    }

    override val hasCompletedOnboarding: Flow<Boolean> = dataStore.data
        .catch { exception ->
            handleError(exception, "reading onboarding status")
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[PreferenceKeys.ONBOARDING_COMPLETED] ?: false
        }

    override suspend fun setHasCompletedOnboarding(completed: Boolean) {
        withContext(ioDispatcher) {
            runCatching {
                dataStore.edit { preferences ->
                    preferences[PreferenceKeys.ONBOARDING_COMPLETED] = completed
                }
            }.onFailure { e ->
                handleError(e, "setting onboarding status")
            }
        }
    }

    override val accentPalette: Flow<String> = dataStore.data
        .catch { exception ->
            handleError(exception, "reading accent palette")
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[PreferenceKeys.ACCENT_PALETTE] ?: AccentPalette.DEFAULT.name
        }

    override suspend fun setAccentPalette(paletteName: String) {
        withContext(ioDispatcher) {
            runCatching {
                dataStore.edit { preferences ->
                    preferences[PreferenceKeys.ACCENT_PALETTE] = paletteName
                }
            }.onFailure { e ->
                handleError(e, "setting accent palette")
            }
        }
    }

    private fun handleError(exception: Throwable, contextMessage: String) {
        if (exception is IOException) {
            Log.e(TAG, "DataStore IO error $contextMessage: ${exception.message}", exception)
        } else {
            Log.e(TAG, "DataStore error $contextMessage: ${exception.message}", exception)
        }
    }
}