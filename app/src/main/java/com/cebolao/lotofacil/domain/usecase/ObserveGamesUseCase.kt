package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.domain.repository.GameRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Caso de uso para observar as listas de jogos gerenciadas pelo GameRepository.
 * Encapsula o acesso direto ao fluxo do repositório, mantendo a ViewModel
 * desacoplada da implementação concreta do repositório.
 */
class ObserveGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    /**
     * Retorna um StateFlow que emite a lista completa de jogos (fixados e não fixados).
     */
    fun observeAllGames(): StateFlow<ImmutableList<LotofacilGame>> = gameRepository.games

    /**
     * Retorna um StateFlow que emite apenas a lista de jogos fixados.
     */
    fun observePinnedGames(): StateFlow<ImmutableList<LotofacilGame>> = gameRepository.pinnedGames

    // Alternativamente, poderia ser um único operator fun invoke() retornando um Pair ou data class
    // se quisesse observar ambos com uma única chamada, mas métodos separados são mais claros.
}