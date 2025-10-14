package com.cebolao.lotofacil.di

import android.content.Context
import com.cebolao.lotofacil.BuildConfig
import com.cebolao.lotofacil.data.network.ApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val RATE_LIMIT_INTERCEPTOR = "RateLimitInterceptor"

    private object Constants {
        const val BASE_URL = "https://servicebus2.caixa.gov.br/portaldeloterias/api/"
        const val CACHE_SIZE_BYTES = 10 * 1_024 * 1_024L // 10 MB
        const val CONNECT_TIMEOUT_SECONDS = 30L
        const val READ_TIMEOUT_SECONDS = 30L
        const val MAX_RETRIES = 3
        const val INITIAL_DELAY_MS = 500L
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "http_cache")
        return Cache(cacheDir, Constants.CACHE_SIZE_BYTES)
    }

    @Provides
    @Singleton
    @Named(RATE_LIMIT_INTERCEPTOR)
    fun provideRateLimitingInterceptor(): Interceptor {
        return object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                var response: Response? = null
                var tryCount = 0
                var successful = false

                while (!successful && tryCount < Constants.MAX_RETRIES) {
                    try {
                        response?.close() // Close previous response body if it exists
                        response = chain.proceed(request)
                        if (response.code == 429) { // Too Many Requests
                            tryCount++
                            val delay = Constants.INITIAL_DELAY_MS * tryCount
                            Thread.sleep(delay)
                        } else {
                            successful = true
                        }
                    } catch (e: Exception) {
                        tryCount++
                        if (tryCount >= Constants.MAX_RETRIES) {
                            throw e
                        }
                    }
                }

                if (response == null) {
                    throw IOException("Failed to execute request after ${Constants.MAX_RETRIES} attempts")
                }

                return response
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        @Named(RATE_LIMIT_INTERCEPTOR) rateLimitInterceptor: Interceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(logging)
            .addInterceptor(rateLimitInterceptor)
            .connectTimeout(Constants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}