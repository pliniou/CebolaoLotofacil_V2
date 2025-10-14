package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.GameRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Caso de uso para salvar um novo jogo gerado ou inserido manualmente.
 * Garante o fluxo correto de dados da apresentação para o domínio.
 */
class SaveGameUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(game: LotofacilGame): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            gameRepository.addGeneratedGames(listOf(game))
        }
    }
}