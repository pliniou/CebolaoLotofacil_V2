package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.GameRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Caso de uso responsável por exportar a lista de jogos gerados para um formato de texto.
 */
class ExportGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Exporta todos os jogos atuais para uma única string serializada.
     * @return Uma string contendo os jogos serializados.
     */
    suspend operator fun invoke(): String = withContext(ioDispatcher) {
        gameRepository.exportGames()
    }
}