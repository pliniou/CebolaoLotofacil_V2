package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.GameRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Encapsula a lógica de negócio para salvar uma lista de jogos gerados.
 * Garante que a ViewModel não interaja diretamente com a camada de dados para operações de escrita.
 */
class SaveGeneratedGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(games: List<LotofacilGame>): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            gameRepository.addGeneratedGames(games)
        }
    }
}