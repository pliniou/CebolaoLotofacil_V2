package com.cebolao.lotofacil.data.network

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("lotofacil/{contest}")
    suspend fun getResultByContest(@Path("contest") contestNumber: Int): LotofacilApiResult

    // O endpoint da Caixa, sem um número de concurso, retorna o resultado mais recente.
    @GET("lotofacil")
    suspend fun getLatestResult(): LotofacilApiResult
}