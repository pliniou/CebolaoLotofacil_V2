package com.cebolao.lotofacil.data.repository

import android.util.Log
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.datasource.HistoryLocalDataSource
import com.cebolao.lotofacil.data.datasource.HistoryRemoteDataSource
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HistoryRepositoryImpl"

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val localDataSource: HistoryLocalDataSource,
    private val remoteDataSource: HistoryRemoteDataSource,
    @ApplicationScope private val applicationScope: CoroutineScope // Escopo da aplicação injetado
) : HistoryRepository {

    private val cacheMutex = Mutex() // Mutex para acesso seguro ao cache
    // Cache em memória (ConcurrentHashMap pode ser uma alternativa se houver muita contenção)
    private val historyCache = mutableMapOf<Int, HistoricalDraw>()
    private var latestApiResultCache: LotofacilApiResult? = null // Cache para o último resultado da API

    // StateFlow para expor o status da sincronização
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        // No início, carrega o histórico local e inicia a primeira sincronização
        applicationScope.launch {
            loadInitialHistory()
            syncHistory() // Inicia a sincronização após carregar o histórico local inicial
        }
    }

    // Carrega o histórico das fontes locais (assets + datastore) para o cache em memória
    private suspend fun loadInitialHistory() {
        // Usa runCatching para tratar erros ao carregar do DataSource local
        runCatching {
            localDataSource.getLocalHistory()
        }.onSuccess { localHistory ->
            updateCache(localHistory, clearExisting = true) // Atualiza o cache
            Log.d(TAG, "Successfully loaded ${localHistory.size} contests into cache.")
        }.onFailure { e ->
            Log.e(TAG, "Error initializing local history cache", e)
            // Mesmo com falha, o app pode continuar com o cache vazio (ou tentar sync)
        }
    }

    // Retorna a lista de sorteios históricos (do cache ou recarregando se vazio)
    override suspend fun getHistory(): List<HistoricalDraw> {
        cacheMutex.withLock {
            // Retorna o cache ordenado se não estiver vazio
            if (historyCache.isNotEmpty()) {
                return historyCache.values.sortedByDescending { it.contestNumber }
            }
        }

        // Se o cache estava vazio, tenta recarregar
        Log.w(TAG, "History cache was empty, attempting to reload...")
        loadInitialHistory() // Tenta carregar novamente
        cacheMutex.withLock {
            // Retorna o que conseguiu carregar (pode ser vazio se falhou de novo)
            return historyCache.values.sortedByDescending { it.contestNumber }
        }
    }

    // Retorna o sorteio mais recente (prioriza API, depois cache local)
    override suspend fun getLastDraw(): HistoricalDraw? {
        // Tenta obter do cache da API primeiro (mais recente)
        latestApiResultCache?.let { HistoricalDraw.fromApiResult(it)?.let { draw -> return draw } }
        // Se não, tenta obter o mais recente do histórico local (do cache ou recarregado)
        return getHistory().firstOrNull()
    }

    // Inicia o processo de sincronização com a API
    override fun syncHistory(): Job = applicationScope.launch {
        // Evita múltiplas sincronizações simultâneas
        if (_syncStatus.value is SyncStatus.Syncing) {
            Log.d(TAG, "Sync already in progress. Skipping.")
            return@launch
        }
        _syncStatus.value = SyncStatus.Syncing // Atualiza status
        Log.d(TAG, "Starting history sync...")

        // Envolve toda a lógica de sincronização em runCatching
        runCatching {
            val latestLocal = getLatestLocalContestNumber()
            Log.d(TAG, "Latest local contest: $latestLocal")

            fetchAndProcessRemoteData(latestLocal)

            Log.d(TAG, "History sync completed successfully.")
            _syncStatus.value = SyncStatus.Success // Define sucesso
        }.onFailure { e ->
            // Trata cancelamento separadamente para não logar como erro fatal
            if (e is CancellationException) {
                Log.w(TAG, "History sync was cancelled.")
                _syncStatus.value = SyncStatus.Idle // Volta para Idle se cancelado
                throw e // Relança para que a coroutine seja cancelada corretamente
            }
            // Loga outros erros e define status de falha
            Log.e(TAG, "Failed to sync history", e)
            _syncStatus.value = SyncStatus.Failed(e)
        }
    }

    /** Obtém o número do último concurso presente no cache local. */
    private suspend fun getLatestLocalContestNumber(): Int {
        // Garante que o histórico local seja carregado se o cache estiver vazio
        if (historyCache.isEmpty()) {
            loadInitialHistory()
        }
        // Retorna o maior número de concurso do cache ou 0 se vazio
        return cacheMutex.withLock { historyCache.keys.maxOrNull() ?: 0 }
    }

    /** Busca dados da API remota, compara com o local e atualiza se necessário. */
    private suspend fun fetchAndProcessRemoteData(latestLocal: Int) {
        // Busca o resultado mais recente da API
        val latestRemoteResult = remoteDataSource.getLatestDraw()
            ?: throw IllegalStateException("Failed to fetch latest draw from remote source.") // Lança exceção se falhar

        latestApiResultCache = latestRemoteResult // Atualiza cache da API
        val latestRemoteNumber = latestRemoteResult.numero
        Log.d(TAG, "Latest remote contest: $latestRemoteNumber")

        // Verifica se há novos concursos para buscar
        if (latestRemoteNumber > latestLocal) {
            val rangeToFetch = (latestLocal + 1)..latestRemoteNumber
            Log.d(TAG, "Fetching contests in range: $rangeToFetch")

            // Busca os concursos faltantes (pode lançar exceção)
            val newDraws = remoteDataSource.getDrawsInRange(rangeToFetch)
            Log.d(TAG, "Fetched ${newDraws.size} new contests.")

            if (newDraws.isNotEmpty()) {
                // Salva localmente (pode lançar exceção)
                localDataSource.saveNewContests(newDraws)
                // Atualiza o cache em memória
                updateCacheAndNotify(newDraws)
            }
        } else {
            Log.d(TAG, "Local history is up to date.")
        }
    }

    /** Atualiza o cache em memória com novos sorteios. */
    private suspend fun updateCacheAndNotify(newDraws: List<HistoricalDraw>) {
        updateCache(newDraws, clearExisting = false)
        Log.d(TAG, "Updated cache with ${newDraws.size} new contests. Total cache size: ${historyCache.size}")
    }

    /** Função auxiliar para atualizar o cache com controle de Mutex. */
    private suspend fun updateCache(draws: List<HistoricalDraw>, clearExisting: Boolean) {
        cacheMutex.withLock { // Acesso seguro ao cache
            if (clearExisting) {
                historyCache.clear()
            }
            draws.forEach { historyCache[it.contestNumber] = it }
        }
    }


    // Retorna o último resultado bruto da API (do cache ou buscando novamente)
    override suspend fun getLatestApiResult(): LotofacilApiResult? {
        // Usa o cache ou busca na rede se o cache estiver vazio
        return latestApiResultCache ?: runCatching { remoteDataSource.getLatestDraw() }
            .getOrNull() // Retorna null em caso de falha na busca
            ?.also { latestApiResultCache = it } // Atualiza o cache se a busca foi bem-sucedida
    }
}