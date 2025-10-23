package com.cebolao.lotofacil.data.network

import retrofit2.http.GET
import retrofit2.http.Path

private const val ENDPOINT_LATEST = "lotofacil"
private const val ENDPOINT_BY_CONTEST = "lotofacil/{contest}"
private const val CONTEST_PATH = "contest"

interface ApiService {
    @GET(ENDPOINT_BY_CONTEST)
    suspend fun getResultByContest(@Path(CONTEST_PATH) contestNumber: Int): LotofacilApiResult

    @GET(ENDPOINT_LATEST)
    suspend fun getLatestResult(): LotofacilApiResult
}