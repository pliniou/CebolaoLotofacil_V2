package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.GameRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Caso de uso com a responsabilidade única de deletar um jogo específico.
 * Melhora a testabilidade e a separação de conceitos.
 */
class DeleteGameUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(game: LotofacilGame) = withContext(ioDispatcher) {
        runCatching {
            gameRepository.deleteGame(game)
        }
    }
}