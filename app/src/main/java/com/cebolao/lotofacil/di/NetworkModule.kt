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
    private const val HTTP_CACHE_DIR = "http_cache"
    private const val HTTP_TOO_MANY_REQUESTS = 429
    private const val CONTENT_TYPE_JSON = "application/json"

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
        val cacheDir = File(context.cacheDir, HTTP_CACHE_DIR)
        return Cache(cacheDir, Constants.CACHE_SIZE_BYTES)
    }

    @Provides
    @Singleton
    @Named(RATE_LIMIT_INTERCEPTOR)
    fun provideRateLimitingInterceptor(): Interceptor {
        return object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                var response: Response? = null
                var exception: IOException? = null
                var tryCount = 0

                while (tryCount < Constants.MAX_RETRIES) {
                    try {
                        response = chain.proceed(request)
                        // Sai do loop se a resposta for bem-sucedida ou um erro diferente de 429
                        if (response.code != HTTP_TOO_MANY_REQUESTS) {
                            return response
                        }
                        // Prepara para re-tentativa se o código for 429
                        val delay = Constants.INITIAL_DELAY_MS * (tryCount + 1)
                        Thread.sleep(delay)

                    } catch (e: IOException) {
                        exception =
                            e // Guarda a exceção para relançar se todas as tentativas falharem
                    } finally {
                        // Fecha o corpo da resposta apenas se for uma resposta 429, para permitir a re-tentativa
                        if (response?.code == HTTP_TOO_MANY_REQUESTS) {
                            response.body?.close()
                        }
                    }
                    tryCount++
                }

                // Se todas as tentativas falharem, retorna a última resposta ou lança a última exceção
                return response ?: throw exception
                    ?: IOException("Request failed after ${Constants.MAX_RETRIES} attempts")
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
        val contentType = CONTENT_TYPE_JSON.toMediaType()
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