package com.cebolao.lotofacil.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LotofacilApiResult(
    @SerialName("numero")
    val numero: Int = 0,

    @SerialName("listaDezenas")
    val listaDezenas: List<String> = emptyList(),

    @SerialName("dataApuracao")
    val dataApuracao: String? = null,

    @SerialName("dataProximoConcurso")
    val dataProximoConcurso: String? = null,

    @SerialName("valorEstimadoProximoConcurso")
    val valorEstimadoProximoConcurso: Double = 0.0,

    @SerialName("valorAcumuladoConcurso_0_5")
    val valorAcumuladoConcurso05: Double = 0.0,

    @SerialName("listaRateioPremio")
    val listaRateioPremio: List<RateioPremio> = emptyList()
)

@Serializable
data class RateioPremio(
    @SerialName("descricaoFaixa")
    val descricaoFaixa: String = "",

    @SerialName("numeroDeGanhadores")
    val numeroDeGanhadores: Int = 0,

    @SerialName("valorPremio")
    val valorPremio: Double = 0.0
)