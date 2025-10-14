package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.GameRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Caso de uso responsável por alternar o estado de "fixado" de um jogo.
 * Simplifica a ViewModel, que apenas invoca este caso de uso.
 */
class TogglePinStateUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(game: LotofacilGame) = withContext(ioDispatcher) {
        runCatching {
            gameRepository.togglePinState(game)
        }
    }
}