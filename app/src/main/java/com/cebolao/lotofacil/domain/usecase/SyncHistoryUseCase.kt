package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/**
 * Caso de uso dedicado para iniciar a sincronização do histórico de sorteios.
 * Abstrai a chamada ao repositório, facilitando testes e manutenibilidade.
 */
class SyncHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke() = historyRepository.syncHistory()
}