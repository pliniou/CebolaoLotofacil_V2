package com.cebolao.lotofacil.data.datasource

import android.util.Log
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.network.ApiService
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HistoryRemoteDataSource"
private const val API_REQUEST_BATCH_SIZE = 50

interface HistoryRemoteDataSource {
    suspend fun getLatestDraw(): LotofacilApiResult?
    suspend fun getDrawsInRange(range: IntRange): List<HistoricalDraw>
}

@Singleton
class HistoryRemoteDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HistoryRemoteDataSource {

    override suspend fun getLatestDraw(): LotofacilApiResult? = withContext(ioDispatcher) {
        try {
            apiService.getLatestResult()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch latest draw", e)
            null
        }
    }

    override suspend fun getDrawsInRange(range: IntRange): List<HistoricalDraw> = withContext(ioDispatcher) {
        if (range.isEmpty()) return@withContext emptyList()

        coroutineScope {
            range.chunked(API_REQUEST_BATCH_SIZE).flatMap { batch ->
                batch.map { contestNumber ->
                    async {
                        try {
                            val result = apiService.getResultByContest(contestNumber)
                            HistoricalDraw.fromApiResult(result)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to fetch contest $contestNumber", e)
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }
        }
    }
}