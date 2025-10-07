package com.cebolao.lotofacil.data.network

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("latest")
    suspend fun getLatestResult(): LotofacilApiResult

    @GET("{contest}")
    suspend fun getResultByContest(@Path("contest") contestNumber: Int): LotofacilApiResult

    // Observação: para otimização de sincronização incremental, a camada de rede
    // pode adicionar interceptors (OkHttp) que exponham ETag/Last-Modified e permitir
    // fetch condicional. Não alteramos a interface Retrofit por compatibilidade.
}