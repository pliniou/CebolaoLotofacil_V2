package com.cebolao.lotofacil.domain.repository

import com.cebolao.lotofacil.data.HistoricalDraw
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

/**
 * Define o status da sincronização de dados de rede.
 * Permite que a UI reaja a diferentes estados do processo de atualização.
 */
sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data object Success : SyncStatus
    data class Failed(val message: String) : SyncStatus
}

interface HistoryRepository {
    /**
     * Um StateFlow que emite o status atual da sincronização de dados de rede.
     * Substitui o booleano `isSyncing` para fornecer informações mais detalhadas.
     */
    val syncStatus: StateFlow<SyncStatus>

    suspend fun getHistory(): List<HistoricalDraw>
    suspend fun getLastDraw(): HistoricalDraw?
    fun syncHistory(): Job
}