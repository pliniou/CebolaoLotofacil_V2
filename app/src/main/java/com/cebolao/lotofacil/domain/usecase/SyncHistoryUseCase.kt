package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.domain.repository.HistoryRepository
import javax.inject.Inject

/**
 * Caso de uso dedicado para iniciar a sincronização do histórico de sorteios.
 * Abstrai a chamada ao repositório, facilitando testes e manutenibilidade.
 */
class SyncHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke() = historyRepository.syncHistory()
}