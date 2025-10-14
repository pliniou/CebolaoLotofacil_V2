package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.GameRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Caso de uso para a lógica de negócio de remover todos os jogos que não estão fixados.
 * A ViewModel apenas dispara a ação, sem conhecer os detalhes da implementação.
 */
class ClearUnpinnedGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke() = withContext(ioDispatcher) {
        runCatching {
            gameRepository.clearUnpinnedGames()
        }
    }
}