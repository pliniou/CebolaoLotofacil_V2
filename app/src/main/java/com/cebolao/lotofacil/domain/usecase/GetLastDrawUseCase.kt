package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Encapsula a lógica de negócio para obter o último sorteio do histórico.
 * Garante que a ViewModel não acesse o repositório diretamente, seguindo a Clean Architecture.
 */
class GetLastDrawUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(): Result<HistoricalDraw?> = withContext(ioDispatcher) {
        runCatching {
            historyRepository.getLastDraw()
        }
    }
}