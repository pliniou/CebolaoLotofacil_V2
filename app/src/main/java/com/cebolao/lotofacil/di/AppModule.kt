package com.cebolao.lotofacil.di

import android.content.Context
import androidx.work.WorkManager
import com.cebolao.lotofacil.domain.service.FilterSuccessCalculator
import com.cebolao.lotofacil.domain.service.GameGenerator
import com.cebolao.lotofacil.domain.service.StatisticsAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideGameGenerator(@DefaultDispatcher dispatcher: CoroutineDispatcher): GameGenerator =
        GameGenerator(dispatcher)

    @Provides
    @Singleton
    fun provideStatisticsAnalyzer(@DefaultDispatcher dispatcher: CoroutineDispatcher): StatisticsAnalyzer =
        StatisticsAnalyzer(dispatcher)

    @Provides
    @Singleton
    fun provideFilterSuccessCalculator(): FilterSuccessCalculator =
        FilterSuccessCalculator()

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
    ): CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)
}