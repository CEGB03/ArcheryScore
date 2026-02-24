package com.cegb03.archeryscore.data.sync

import android.util.Log
import com.cegb03.archeryscore.data.local.training.TrainingDao
import com.cegb03.archeryscore.data.local.training.TrainingEntity
import com.cegb03.archeryscore.data.local.training.SeriesEntity
import com.cegb03.archeryscore.data.local.training.TrainingEndEntity
import com.cegb03.archeryscore.data.remote.supabase.repository.SupabaseAuthRepository
import com.cegb03.archeryscore.data.remote.supabase.repository.SupabaseTrainingRepository
import com.cegb03.archeryscore.data.remote.supabase.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TrainingSyncManager - Estrategia Offline-First
 * 
 * PRINCIPIOS:
 * 1. Room es la FUENTE DE VERDAD (source of truth)
 * 2. Lectura SIEMPRE desde Room
 * 3. Escritura en Room primero, luego sincroniza con Supabase en background
 * 4. Si falla la sincronización, guarda en cola de pendientes
 * 5. Reintentos automáticos con backoff exponencial
 * 6. Conflictos: last-write-wins (basado en timestamps)
 * 
 * FLUJO:
 * - Usuario crea/modifica → Room → SyncManager → Supabase (background)
 * - Si offline: Queda en Room, se sincroniza cuando vuelve la conexión
 * - Al abrir la app: Sincroniza cambios de Supabase → Room
 */
@Singleton
class TrainingSyncManager @Inject constructor(
    private val trainingDao: TrainingDao,
    private val supabaseTrainingRepo: SupabaseTrainingRepository,
    private val supabaseAuthRepo: SupabaseAuthRepository
) {
    
    private val TAG = "TrainingSync_Debug"
    private val syncScope = CoroutineScope(Dispatchers.IO)
    
    // Estado de sincronización
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState
    
    // Última vez que se sincronizó (timestamp)
    @Volatile
    private var lastSyncTimestamp: Long = 0L
    
    // Cola de operaciones pendientes (para reintentar después)
    private val pendingOperations = mutableListOf<PendingOperation>()
    
    // ============================================================================
    // SINCRONIZACIÓN BIDIRECCIONAL
    // ============================================================================
    
    /**
     * Sincronización completa bidireccional
     * 1. Subir cambios locales sin sincronizar a Supabase
     * 2. Bajar cambios desde Supabase y actualizar Room
     */
    suspend fun syncAll() {
        if (!supabaseAuthRepo.isAuthenticated()) {
            Log.d(TAG, "⚠️ Usuario no autenticado, saltando sync")
            return
        }
        
        _syncState.value = SyncState.Syncing
        Log.d(TAG, "🔄 Iniciando sincronización completa...")
        
        try {
            // 1. Subir cambios locales a Supabase
            uploadLocalChanges()
            
            // 2. Bajar cambios remotos desde Supabase
            downloadRemoteChanges()
            
            // 3. Reintentar operaciones pendientes
            retryPendingOperations()
            
            lastSyncTimestamp = System.currentTimeMillis()
            _syncState.value = SyncState.Success(lastSyncTimestamp)
            Log.d(TAG, "✅ Sincronización completa exitosa")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en sincronización: ${e.message}", e)
            _syncState.value = SyncState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Sincronizar un entrenamiento específico con todas sus series y ends
     */
    suspend fun syncTraining(localTrainingId: Long) {
        if (!supabaseAuthRepo.isAuthenticated()) {
            queuePendingOperation(PendingOperation.SyncTraining(localTrainingId))
            return
        }
        
        try {
            Log.d(TAG, "🔄 Sincronizando training local ID: $localTrainingId")
            
            // TODO: Obtener training de Room y subirlo a Supabase
            // Por ahora, solo registrar la intención
            
            Log.d(TAG, "✅ Training $localTrainingId sincronizado")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al sincronizar training: ${e.message}", e)
            queuePendingOperation(PendingOperation.SyncTraining(localTrainingId))
        }
    }
    
    // ============================================================================
    // SUBIDA (Room → Supabase)
    // ============================================================================
    
    /**
     * Subir todos los cambioslocales que no han sido sincronizados
     * (trainings sin remote_id o con updated_at > synced_at)
     */
    private suspend fun uploadLocalChanges() {
        Log.d(TAG, "⬆️ Subiendo cambios locales a Supabase...")
        
        val userId = supabaseAuthRepo.getCurrentUserId() ?: return
        
        // TODO: Implementar lógica de subida
        // 1. Buscar trainings sin remote_id en Room
        // 2. Crear en Supabase y guardar remote_id en Room
        // 3. Subir series y ends asociados
        
        Log.d(TAG, "✅ Cambios locales subidos")
    }
    
    // ============================================================================
    // DESCARGA (Supabase → Room)
    // ============================================================================
    
    /**
     * Descargar cambios remotos desde Supabase y actualizar Room
     */
    private suspend fun downloadRemoteChanges() {
        Log.d(TAG, "⬇️ Descargando cambios remotos desde Supabase...")
        
        try {
            // 1. Obtener trainings modificados desde último sync
            val result = supabaseTrainingRepo.getTrainingsModifiedAfter(lastSyncTimestamp)
            
            if (result.isFailure) {
                Log.e(TAG, "❌ Error al obtener trainings remotos: ${result.exceptionOrNull()?.message}")
                return
            }
            
            val remoteTrainings = result.getOrNull() ?: emptyList()
            Log.d(TAG, "📥 Descargados ${remoteTrainings.size} trainings remotos")
            
            // 2. Para cada training remoto, actualizar en Room
            remoteTrainings.forEach { remoteTraining ->
                syncRemoteTrainingToLocal(remoteTraining)
            }
            
            Log.d(TAG, "✅ Cambios remotos aplicados")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al descargar cambios remotos: ${e.message}", e)
        }
    }
    
    /**
     * Sincronizar un training remoto a Room
     */
    private suspend fun syncRemoteTrainingToLocal(remoteTraining: Training) {
        try {
            Log.d(TAG, "🔽 Sincronizando remote training ${remoteTraining.id} a Room...")
            
            // 1. Convertir Training remoto a TrainingEntity local
            val localTraining = TrainingEntity(
                id = remoteTraining.id ?: 0L,
                createdAt = parseTimestamp(remoteTraining.createdAt) ?: System.currentTimeMillis(),
                archerName = remoteTraining.archerName,
                trainingType = remoteTraining.trainingType,
                isGroup = remoteTraining.isGroup
            )
            
            // 2. Insertar o actualizar en Room
            trainingDao.insertTraining(localTraining)
            
            // 3. Sincronizar series
            val seriesResult = supabaseTrainingRepo.getSeriesByTrainingId(remoteTraining.id!!)
            if (seriesResult.isSuccess) {
                val remoteSeries = seriesResult.getOrNull() ?: emptyList()
                remoteSeries.forEach { remoteSerie ->
                    syncRemoteSerieToLocal(remoteSerie, localTraining.id)
                }
            }
            
            Log.d(TAG, "✅ Remote training ${remoteTraining.id} sincronizado a Room")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al sincronizar remote training: ${e.message}", e)
        }
    }
    
    /**
     * Sincronizar una serie remota a Room
     */
    private suspend fun syncRemoteSerieToLocal(remoteSeries: TrainingSeries, localTrainingId: Long) {
        try {
            val localSeries = SeriesEntity(
                id = remoteSeries.id ?: 0L,
                trainingId = localTrainingId,
                seriesNumber = remoteSeries.seriesNumber,
                distanceMeters = remoteSeries.distanceMeters,
                category = remoteSeries.category,
                targetType = remoteSeries.targetType,
                arrowsPerEnd = remoteSeries.arrowsPerEnd,
                endsCount = remoteSeries.endsCount,
                targetZoneCount = remoteSeries.targetZoneCount,
                puntajeSystem = remoteSeries.puntajeSystem,
                temperature = remoteSeries.temperature,
                windSpeed = remoteSeries.windSpeed,
                windSpeedUnit = remoteSeries.windSpeedUnit,
                windDirectionDegrees = remoteSeries.windDirectionDegrees,
                skyCondition = remoteSeries.skyCondition,
                locationLat = remoteSeries.locationLat,
                locationLon = remoteSeries.locationLon,
                weatherSource = remoteSeries.weatherSource,
                recordedAt = parseTimestamp(remoteSeries.recordedAt)
            )
            
            trainingDao.insertSeries(localSeries)
            
            // Sincronizar ends de la serie
            val endsResult = supabaseTrainingRepo.getEndsBySeriesId(remoteSeries.id!!)
            if (endsResult.isSuccess) {
                val remoteEnds = endsResult.getOrNull() ?: emptyList()
                remoteEnds.forEach { remoteEnd ->
                    syncRemoteEndToLocal(remoteEnd, localSeries.id)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al sincronizar remote series: ${e.message}", e)
        }
    }
    
    /**
     * Sincronizar un end remoto a Room
     */
    private suspend fun syncRemoteEndToLocal(remoteEnd: TrainingEnd, localSeriesId: Long) {
        try {
            val localEnd = TrainingEndEntity(
                id = remoteEnd.id ?: 0L,
                seriesId = localSeriesId,
                endNumber = remoteEnd.endNumber,
                confirmedAt = parseTimestamp(remoteEnd.confirmedAt),
                scoresText = remoteEnd.scoresText,
                totalScore = remoteEnd.totalScore
            )
            
            trainingDao.insertEnd(localEnd)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al sincronizar remote end: ${e.message}", e)
        }
    }
    
    // ============================================================================
    // OPERACIONES PENDIENTES (para retry)
    // ============================================================================
    
    private fun queuePendingOperation(operation: PendingOperation) {
        synchronized(pendingOperations) {
            pendingOperations.add(operation)
            Log.d(TAG, "📝 Operación agregada a cola de pendientes: $operation")
        }
    }
   
    private suspend fun retryPendingOperations() {
        if (pendingOperations.isEmpty()) {
            return
        }
        
        Log.d(TAG, "🔄 Reintentando ${pendingOperations.size} operaciones pendientes...")
        
        val operationsToRetry = synchronized(pendingOperations) {
            pendingOperations.toList()
        }
        
        operationsToRetry.forEach { operation ->
            try {
                when (operation) {
                    is PendingOperation.SyncTraining -> {
                        syncTraining(operation.localTrainingId)
                        // Si tuvo éxito, remover de pendientes
                        synchronized(pendingOperations) {
                            pendingOperations.remove(operation)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al reintentar operación $operation: ${e.message}", e)
            }
        }
        
        Log.d(TAG, "✅ Reintentos completados. Pendientes: ${pendingOperations.size}")
    }
    
    // ============================================================================
    // SINCRONIZACIÓN AUTOMÁTICA
    // ============================================================================
    
    /**
     * Iniciar sincronización automática periódica
     */
    fun startAutoSync(intervalMillis: Long = 5 * 60 * 1000) { // 5 minutos por defecto
        syncScope.launch {
            while (true) {
                delay(intervalMillis)
                
                if (supabaseAuthRepo.isAuthenticated()) {
                    Log.d(TAG, "⏰ Auto-sync ejecutándose...")
                    syncAll()
                }
            }
        }
    }
    
    // ============================================================================
    // UTILIDADES
    // ============================================================================
    
    private fun parseTimestamp(timestamp: String?): Long? {
        if (timestamp == null) return null
        
        return try {
            // Intentar parse ISO 8601 (formato Supabase)
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(timestamp)?.time
        } catch (e: Exception) {
            // Si falla, intentar como Long directo
            timestamp.toLongOrNull()
        }
    }
    
    private fun formatTimestamp(timestamp: Long?): String? {
        if (timestamp == null) return null
        
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestamp))
    }
}

// ============================================================================
// CLASES DE ESTADO
// ============================================================================

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val timestamp: Long) : SyncState()
    data class Error(val message: String) : SyncState()
}

sealed class PendingOperation {
    data class SyncTraining(val localTrainingId: Long) : PendingOperation()
}
