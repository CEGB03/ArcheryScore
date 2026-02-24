package com.cegb03.archeryscore.data.remote.supabase.repository

import android.util.Log
import com.cegb03.archeryscore.data.remote.supabase.SupabaseClient
import com.cegb03.archeryscore.data.remote.supabase.models.*
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gestión de entrenamientos en Supabase
 * 
 * Maneja entrenamientos, series, ends y compartir entre usuarios
 */
@Singleton
class SupabaseTrainingRepository @Inject constructor(
    private val authRepository: SupabaseAuthRepository
) {
    
    private val client = SupabaseClient.client
    private val TAG = "SupabaseTraining_Debug"
    
    // ============================================================================
    // TRAININGS - CRUD
    // ============================================================================
    
    /**
     * Crear nuevo entrenamiento
     */
    suspend fun createTraining(request: TrainingRequest): Result<Training> {
        return try {
            Log.d(TAG, "🆕 Creando entrenamiento para userId: ${request.userId}")
            
            val training = client.from("trainings")
                .insert(request)
                .decodeSingle<Training>()
            
            Log.d(TAG, "✅ Entrenamiento creado con ID: ${training.id}")
            Result.success(training)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al crear entrenamiento: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtener entrenamientos del usuario actual
     * Incluye propios y compartidos
     */
    suspend fun getMyTrainings(): Result<List<TrainingWithShareInfo>> {
        return try {
            val userId = authRepository.getCurrentUserId()
            
            if (userId == null) {
                Log.e(TAG, "❌ Error: No hay usuario autenticado")
                return Result.failure(Exception("No hay usuario autenticado"))
            }
            
            Log.d(TAG, "📋 Obteniendo entrenamientos para userId: $userId")
            
            // Usar función SQL que ya maneja propios + compartidos
            val trainings = client.from("get_my_trainings_with_shares")
                .select()
                .decodeList<TrainingWithShareInfo>()
            
            Log.d(TAG, "✅ Obtenidos ${trainings.size} entrenamientos")
            Result.success(trainings)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener entrenamientos: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtener un entrenamiento específico por ID
     */
    suspend fun getTrainingById(trainingId: Long): Result<Training> {
        return try {
            Log.d(TAG, "🔍 Obteniendo entrenamiento ID: $trainingId")
            
            val training = client.from("trainings")
                .select {
                    filter {
                        eq("id", trainingId)
                    }
                }
                .decodeSingle<Training>()
            
            Log.d(TAG, "✅ Entrenamiento obtenido")
            Result.success(training)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener entrenamiento: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar entrenamiento
     */
    suspend fun updateTraining(trainingId: Long, request: TrainingRequest): Result<Training> {
        return try {
            Log.d(TAG, "📝 Actualizando entrenamiento ID: $trainingId")
            
            client.from("trainings")
                .update(request) {
                    filter {
                        eq("id", trainingId)
                    }
                }
            
            // Obtener entrenamiento actualizado
            val updated = getTrainingById(trainingId).getOrThrow()
            
            Log.d(TAG, "✅ Entrenamiento actualizado")
            Result.success(updated)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al actualizar entrenamiento: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Eliminar entrenamiento (soft delete)
     */
    suspend fun deleteTraining(trainingId: Long): Result<Unit> {
        return try {
            Log.d(TAG, "🗑️ Eliminando (soft delete) entrenamiento ID: $trainingId")
            
            client.from("trainings")
                .update(mapOf("deleted_at" to System.currentTimeMillis().toString())) {
                    filter {
                        eq("id", trainingId)
                    }
                }
            
            Log.d(TAG, "✅ Entrenamiento eliminado")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar entrenamiento: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ============================================================================
    // TRAINING SERIES - CRUD
    // ============================================================================
    
    /**
     * Crear nueva serie en un entrenamiento
     */
    suspend fun createSeries(request: TrainingSeriesRequest): Result<TrainingSeries> {
        return try {
            Log.d(TAG, "🆕 Creando serie #${request.seriesNumber} para training: ${request.trainingId}")
            
            val series = client.from("training_series")
                .insert(request)
                .decodeSingle<TrainingSeries>()
            
            Log.d(TAG, "✅ Serie creada con ID: ${series.id}")
            Result.success(series)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al crear serie: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtener todas las series de un entrenamiento
     */
    suspend fun getSeriesByTrainingId(trainingId: Long): Result<List<TrainingSeries>> {
        return try {
            Log.d(TAG, "📋 Obteniendo series para training ID: $trainingId")
            
            val series = client.from("training_series")
                .select {
                    filter {
                        eq("training_id", trainingId)
                    }
                }
                .decodeList<TrainingSeries>()
            
            Log.d(TAG, "✅ Obtenidas ${series.size} series")
            Result.success(series)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener series: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtener una serie específica por ID
     */
    suspend fun getSeriesById(seriesId: Long): Result<TrainingSeries> {
        return try {
            Log.d(TAG, "🔍 Obteniendo serie ID: $seriesId")
            
            val series = client.from("training_series")
                .select {
                    filter {
                        eq("id", seriesId)
                    }
                }
                .decodeSingle<TrainingSeries>()
            
            Log.d(TAG, "✅ Serie obtenida")
            Result.success(series)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener serie: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar serie
     */
    suspend fun updateSeries(seriesId: Long, request: TrainingSeriesRequest): Result<TrainingSeries> {
        return try {
            Log.d(TAG, "📝 Actualizando serie ID: $seriesId")
            
            client.from("training_series")
                .update(request) {
                    filter {
                        eq("id", seriesId)
                    }
                }
            
            val updated = getSeriesById(seriesId).getOrThrow()
            
            Log.d(TAG, "✅ Serie actualizada")
            Result.success(updated)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al actualizar serie: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Eliminar serie (soft delete)
     */
    suspend fun deleteSeries(seriesId: Long): Result<Unit> {
        return try {
            Log.d(TAG, "🗑️ Eliminando serie ID: $seriesId")
            
            client.from("training_series")
                .update(mapOf("deleted_at" to System.currentTimeMillis().toString())) {
                    filter {
                        eq("id", seriesId)
                    }
                }
            
            Log.d(TAG, "✅ Serie eliminada")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar serie: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ============================================================================
    // TRAINING ENDS - CRUD
    // ============================================================================
    
    /**
     * Crear nuevo end en una serie
     */
    suspend fun createEnd(request: TrainingEndRequest): Result<TrainingEnd> {
        return try {
            Log.d(TAG, "🆕 Creando end #${request.endNumber} para serie: ${request.seriesId}")
            
            val end = client.from("training_ends")
                .insert(request)
                .decodeSingle<TrainingEnd>()
            
            Log.d(TAG, "✅ End creado con ID: ${end.id}")
            Result.success(end)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al crear end: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtener todos los ends de una serie
     */
    suspend fun getEndsBySeriesId(seriesId: Long): Result<List<TrainingEnd>> {
        return try {
            Log.d(TAG, "📋 Obteniendo ends para serie ID: $seriesId")
            
            val ends = client.from("training_ends")
                .select {
                    filter {
                        eq("series_id", seriesId)
                    }
                }
                .decodeList<TrainingEnd>()
            
            Log.d(TAG, "✅ Obtenidos ${ends.size} ends")
            Result.success(ends)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener ends: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar end
     */
    suspend fun updateEnd(endId: Long, request: TrainingEndRequest): Result<TrainingEnd> {
        return try {
            Log.d(TAG, "📝 Actualizando end ID: $endId")
            
            client.from("training_ends")
                .update(request) {
                    filter {
                        eq("id", endId)
                    }
                }
            
            val updated = client.from("training_ends")
                .select {
                    filter {
                        eq("id", endId)
                    }
                }
                .decodeSingle<TrainingEnd>()
            
            Log.d(TAG, "✅ End actualizado")
            Result.success(updated)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al actualizar end: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ============================================================================
    // COMPARTIR ENTRENAMIENTOS
    // ============================================================================
    
    /**
     * Compartir entrenamiento con otro usuario
     */
    suspend fun shareTraining(request: ShareTrainingRequest): Result<TrainingShare> {
        return try {
            Log.d(TAG, "🔗 Compartiendo training ${request.trainingId} con usuario ${request.sharedWithId}")
            
            val share = client.from("training_shares")
                .insert(request)
                .decodeSingle<TrainingShare>()
            
            Log.d(TAG, "✅ Entrenamiento compartido")
            Result.success(share)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al compartir entrenamiento: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtener con quién está compartido un entrenamiento
     */
    suspend fun getTrainingShares(trainingId: Long): Result<List<TrainingShare>> {
        return try {
            Log.d(TAG, "📋 Obteniendo compartidos para training ID: $trainingId")
            
            val shares = client.from("training_shares")
                .select {
                    filter {
                        eq("training_id", trainingId)
                    }
                }
                .decodeList<TrainingShare>()
            
            Log.d(TAG, "✅ Obtenidos ${shares.size} shares")
            Result.success(shares)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener shares: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Revocar compartir (eliminar share)
     */
    suspend fun revokeShare(shareId: Long): Result<Unit> {
        return try {
            Log.d(TAG, "🚫 Revocando share ID: $shareId")
            
            client.from("training_shares")
                .delete {
                    filter {
                        eq("id", shareId)
                    }
                }
            
            Log.d(TAG, "✅ Share revocado")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al revocar share: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar permisos de un share existente
     */
    suspend fun updateSharePermission(shareId: Long, newPermission: String): Result<TrainingShare> {
        return try {
            Log.d(TAG, "📝 Actualizando permisos de share ID: $shareId a $newPermission")
            
            client.from("training_shares")
                .update(mapOf("permission" to newPermission)) {
                    filter {
                        eq("id", shareId)
                    }
                }
            
            val updated = client.from("training_shares")
                .select {
                    filter {
                        eq("id", shareId)
                    }
                }
                .decodeSingle<TrainingShare>()
            
            Log.d(TAG, "✅ Permisos actualizados")
            Result.success(updated)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al actualizar permisos: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ============================================================================
    // SINCRONIZACIÓN
    // ============================================================================
    
    /**
     * Marcar entrenamiento como sincronizado
     */
    suspend fun markAsSynced(trainingId: Long): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis().toString()
            
            client.from("trainings")
                .update(mapOf("synced_at" to currentTime)) {
                    filter {
                        eq("id", trainingId)
                    }
                }
            
            Log.d(TAG, "✅ Training $trainingId marcado como sincronizado")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al marcar como sincronizado: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtener entrenamientos modificados después de una fecha
     * (Para sincronización incremental)
     */
    suspend fun getTrainingsModifiedAfter(timestamp: Long): Result<List<Training>> {
        return try {
            val userId = authRepository.getCurrentUserId() ?: return Result.failure(Exception("No autenticado"))
            
            Log.d(TAG, "🔄 Obteniendo entrenamientos modificados después de $timestamp")
            
            val trainings = client.from("trainings")
                .select {
                    filter {
                        eq("user_id", userId)
                        gte("updated_at", timestamp.toString())
                    }
                }
                .decodeList<Training>()
            
            Log.d(TAG, "✅ Obtenidos ${trainings.size} entrenamientos modificados")
            Result.success(trainings)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener entrenamientos modificados: ${e.message}", e)
            Result.failure(e)
        }
    }
}
