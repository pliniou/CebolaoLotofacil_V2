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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserPreferencesRepository {

    companion object {
        private const val TAG = "UserPreferencesRepo"
        private val PINNED_GAMES_KEY = stringSetPreferencesKey("pinned_games")
        private val DYNAMIC_HISTORY_KEY = stringSetPreferencesKey("dynamic_history")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    }

    override val pinnedGames: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            handleError(exception, "reading pinned games")
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[PINNED_GAMES_KEY] ?: emptySet()
        }

    override suspend fun savePinnedGames(games: Set<String>) {
        withContext(ioDispatcher) {
            try {
                context.dataStore.edit { preferences ->
                    preferences[PINNED_GAMES_KEY] = games
                }
                Log.d(TAG, "Saved ${games.size} pinned games")
            } catch (e: IOException) {
                handleError(e, "saving pinned games")
            }
        }
    }

    override suspend fun getHistory(): Set<String> = withContext(ioDispatcher) {
        try {
            context.dataStore.data
                .map { preferences ->
                    preferences[DYNAMIC_HISTORY_KEY] ?: emptySet()
                }
                .first()
        } catch (e: Exception) {
            handleError(e, "getting history")
            emptySet()
        }
    }

    override suspend fun addDynamicHistoryEntries(newHistoryEntries: Set<String>) {
        withContext(ioDispatcher) {
            if (newHistoryEntries.isEmpty()) return@withContext
            try {
                context.dataStore.edit { preferences ->
                    val currentHistory = preferences[DYNAMIC_HISTORY_KEY] ?: emptySet()
                    val validEntries = newHistoryEntries.filter { entry ->
                        entry.matches(Regex("\\d+ - [\\d,]+"))
                    }.toSet()

                    if (validEntries.isNotEmpty()) {
                        preferences[DYNAMIC_HISTORY_KEY] = currentHistory + validEntries
                        Log.d(TAG, "Added ${validEntries.size} valid history entries")
                    }
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
            preferences[THEME_MODE_KEY] ?: "auto"
        }

    override suspend fun setThemeMode(mode: String) {
        withContext(ioDispatcher) {
            try {
                context.dataStore.edit { preferences ->
                    preferences[THEME_MODE_KEY] = mode
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
            preferences[ONBOARDING_COMPLETED_KEY] ?: false
        }


    override suspend fun setHasCompletedOnboarding(completed: Boolean) {
        withContext(ioDispatcher) {
            try {
                context.dataStore.edit { preferences ->
                    preferences[ONBOARDING_COMPLETED_KEY] = completed
                }
            } catch (e: IOException) {
                handleError(e, "setting onboarding status")
            }
        }
    }

    private fun handleError(exception: Throwable, contextMessage: String) {
        Log.e(TAG, "Error $contextMessage", exception)
    }
}