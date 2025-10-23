package com.cebolao.lotofacil.domain.usecase

import android.util.Log
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "GetLastDrawUseCase"

/**
 * Encapsula a lógica de negócio para obter o último sorteio do histórico.
 * Garante que a ViewModel não acesse o repositório diretamente, seguindo a Clean Architecture.
 */
class GetLastDrawUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Busca o último sorteio.
     * @return Result.success(HistoricalDraw) se encontrado, Result.success(null) se histórico vazio, Result.failure se erro.
     */
    suspend operator fun invoke(): Result<HistoricalDraw?> = withContext(ioDispatcher) {
        runCatching {
            historyRepository.getLastDraw()
        }.onFailure { e ->
            Log.e(TAG, "Failed to get last draw", e)
        }
        // O runCatching já retorna Result<HistoricalDraw?>, o chamador trata o null interno.
    }
}