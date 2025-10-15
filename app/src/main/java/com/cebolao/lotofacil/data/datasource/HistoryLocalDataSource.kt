package com.cebolao.lotofacil.data.datasource

import android.content.Context
import android.util.Log
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.HistoryParser
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HistoryLocalDataSource"
private const val ASSET_HISTORY_FILENAME = "lotofacil_resultados.txt"

interface HistoryLocalDataSource {
    suspend fun getLocalHistory(): List<HistoricalDraw>
    suspend fun saveNewContests(newDraws: List<HistoricalDraw>)
}

@Singleton
class HistoryLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HistoryLocalDataSource {

    private var assetHistoryCache: List<HistoricalDraw>? = null

    override suspend fun getLocalHistory(): List<HistoricalDraw> = withContext(ioDispatcher) {
        val assetHistory = parseHistoryFromAssets()
        val savedHistoryStrings = userPreferencesRepository.getHistory()
        val savedHistory = savedHistoryStrings.mapNotNull { HistoryParser.parseLine(it) }

        val allDraws = (savedHistory + assetHistory)
            .distinctBy { it.contestNumber }
            .sortedByDescending { it.contestNumber }

        Log.d(
            TAG,
            "Loaded ${allDraws.size} contests from local sources (Assets: ${assetHistory.size}, DataStore: ${savedHistory.size})"
        )
        allDraws
    }

    override suspend fun saveNewContests(newDraws: List<HistoricalDraw>) {
        if (newDraws.isEmpty()) return

        // Formata: "NUMERO - 01,02,..."
        val newHistoryEntries = newDraws.map { draw ->
            "${draw.contestNumber} - ${
                draw.numbers.sorted().joinToString(",") { "%02d".format(it) }
            }"
        }.toSet()

        userPreferencesRepository.addDynamicHistoryEntries(newHistoryEntries)
        Log.d(TAG, "Persisted ${newDraws.size} new contests locally.")
    }

    private suspend fun parseHistoryFromAssets(): List<HistoricalDraw> {
        return assetHistoryCache ?: withContext(ioDispatcher) {
            try {
                context.assets.open(ASSET_HISTORY_FILENAME).bufferedReader().use { reader ->
                    reader.lineSequence()
                        .filter { it.isNotBlank() }
                        .mapNotNull { line ->
                            val parsed = HistoryParser.parseLine(line)
                            if (parsed == null) {
                                Log.w(TAG, "Ignored malformed history line: $line")
                            }
                            parsed
                        }
                        .toList()
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to read history file from assets: $ASSET_HISTORY_FILENAME", e)
                emptyList()
            }
        }.also { assetHistoryCache = it }
    }
}