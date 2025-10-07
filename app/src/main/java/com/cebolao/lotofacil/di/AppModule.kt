package com.cebolao.lotofacil.di

import com.cebolao.lotofacil.domain.service.GameGenerator
import com.cebolao.lotofacil.domain.service.GameStatsAnalyzer
import com.cebolao.lotofacil.domain.service.StatisticsAnalyzer
import com.cebolao.lotofacil.domain.usecase.GetGameSimpleStatsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGameGenerator(@DefaultDispatcher dispatcher: CoroutineDispatcher): GameGenerator {
        return GameGenerator(dispatcher)
    }

    @Provides
    @Singleton
    fun provideStatisticsAnalyzer(@DefaultDispatcher dispatcher: CoroutineDispatcher): StatisticsAnalyzer {
        return StatisticsAnalyzer(dispatcher)
    }

    @Provides
    @Singleton
    fun provideGameStatsAnalyzer(): GameStatsAnalyzer {
        return GameStatsAnalyzer()
    }

    @Provides
    @Singleton
    fun provideGetGameSimpleStatsUseCase(
        gameStatsAnalyzer: GameStatsAnalyzer,
        @DefaultDispatcher dispatcher: CoroutineDispatcher
    ): GetGameSimpleStatsUseCase {
        return GetGameSimpleStatsUseCase(gameStatsAnalyzer, dispatcher)
    }

    @IoDispatcher
    @Provides
    @Singleton
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @DefaultDispatcher
    @Provides
    @Singleton
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @ApplicationScope
    @Provides
    @Singleton
    fun providesApplicationScope(
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob() + defaultDispatcher)
    }
}