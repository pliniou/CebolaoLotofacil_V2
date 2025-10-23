package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Caso de uso para observar o status da sincronização do histórico.
 * Abstrai o acesso direto ao fluxo do repositório.
 */
class ObserveSyncStatusUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    /**
     * Retorna um StateFlow que emite o status atual da sincronização do histórico.
     */
    operator fun invoke(): StateFlow<SyncStatus> = historyRepository.syncStatus
}