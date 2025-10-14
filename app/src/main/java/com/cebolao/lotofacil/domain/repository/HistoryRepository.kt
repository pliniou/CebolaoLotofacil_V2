package com.cebolao.lotofacil.domain.repository

import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data object Success : SyncStatus
    data class Failed(val message: String) : SyncStatus
}

interface HistoryRepository {
    val syncStatus: StateFlow<SyncStatus>

    suspend fun getHistory(): List<HistoricalDraw>
    suspend fun getLastDraw(): HistoricalDraw?
    fun syncHistory(): Job

    /** Retorna os detalhes completos do último concurso, incluindo prêmios e próximo sorteio. */
    suspend fun getLatestContestDetails(): LotofacilApiResult?
}