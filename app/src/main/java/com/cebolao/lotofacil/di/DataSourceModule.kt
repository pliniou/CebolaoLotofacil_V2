package com.cebolao.lotofacil.di

import com.cebolao.lotofacil.data.datasource.HistoryLocalDataSource
import com.cebolao.lotofacil.data.datasource.HistoryLocalDataSourceImpl
import com.cebolao.lotofacil.data.datasource.HistoryRemoteDataSource
import com.cebolao.lotofacil.data.datasource.HistoryRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindHistoryLocalDataSource(
        historyLocalDataSourceImpl: HistoryLocalDataSourceImpl
    ): HistoryLocalDataSource

    @Binds
    @Singleton
    abstract fun bindHistoryRemoteDataSource(
        historyRemoteDataSourceImpl: HistoryRemoteDataSourceImpl
    ): HistoryRemoteDataSource
}