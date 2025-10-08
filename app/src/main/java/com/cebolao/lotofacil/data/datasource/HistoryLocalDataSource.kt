package com.cebolao.lotofacil.data.datasource

import android.content.Context
import android.util.Log
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.HistoryParser
import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface HistoryLocalDataSource {
    suspend fun getLocalHistory(): List<HistoricalDraw>
    suspend fun saveNewContests(newDraws: List<HistoricalDraw>)
}

@Singleton
class HistoryLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) : HistoryLocalDataSource {

    private val historyFileName = "lotofacil_resultados.txt"

    companion object {
        private const val TAG = "HistoryLocalDataSource"
    }

    override suspend fun getLocalHistory(): List<HistoricalDraw> = withContext(Dispatchers.IO) {
        val assetHistory = parseHistoryFromAssets()
        val savedHistoryStrings = userPreferencesRepository.getHistory()
        val savedHistory = savedHistoryStrings.mapNotNull { HistoryParser.parseLine(it) }

        // CORREÇÃO: Junta as listas dando prioridade ao histórico salvo (mais recente/remoto)
        // e usa distinctBy para uma desduplicação clara e eficiente.
        val allDraws = (savedHistory + assetHistory)
            .distinctBy { it.contestNumber }
            .sortedByDescending { it.contestNumber }

        Log.d(TAG, "Loaded ${allDraws.size} contests from local sources (Assets: ${assetHistory.size}, DataStore: ${savedHistory.size})")
        allDraws
    }

    override suspend fun saveNewContests(newDraws: List<HistoricalDraw>) {
        if (newDraws.isEmpty()) return

        val newHistoryEntries = newDraws.map { draw ->
            // A data só vem do remote, mas é melhor não salvar no DataStore se for nula
            // A estrutura de salvamento local deve ser compatível com o parser local
            "${draw.contestNumber} - ${draw.numbers.sorted().joinToString(",")}"
        }.toSet()

        userPreferencesRepository.addDynamicHistoryEntries(newHistoryEntries)
        Log.d(TAG, "Persisted ${newDraws.size} new contests locally.")
    }

    private suspend fun parseHistoryFromAssets(): List<HistoricalDraw> = withContext(Dispatchers.IO) {
        try {
            context.assets.open(historyFileName).bufferedReader().use { reader ->
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
            Log.e(TAG, "Failed to read history file from assets: $historyFileName", e)
            emptyList()
        }
    }
}