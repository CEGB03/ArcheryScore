package com.cegb03.archeryscore.data.remote.supabase.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelos de datos para Supabase con serialización JSON
 * Estos modelos mapean directamente a las tablas de PostgreSQL en Supabase
 */

// ============================================================================
// PERFIL DE USUARIO
// ============================================================================

@Serializable
data class UserProfile(
    @SerialName("id") val id: String, // UUID como String
    @SerialName("username") val username: String,
    @SerialName("email") val email: String,
    @SerialName("tel") val tel: String? = null,
    @SerialName("documento") val documento: String? = null, // DNI/CI para verificación FATARCO
    @SerialName("club") val club: String? = null,
    @SerialName("fecha_nacimiento") val fechaNacimiento: String? = null,
    @SerialName("role") val role: List<String> = listOf("arquero"), // Lista: arquero, entrenador, juez, admin
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

// Request para actualizar perfil (sin id)
@Serializable
data class UpdateProfileRequest(
    @SerialName("username") val username: String? = null,
    @SerialName("tel") val tel: String? = null,
    @SerialName("documento") val documento: String? = null,
    @SerialName("club") val club: String? = null,
    @SerialName("fecha_nacimiento") val fechaNacimiento: String? = null,
    @SerialName("role") val role: List<String>? = null
)

@Serializable
data class FatarcoProfile(
    @SerialName("user_id") val userId: String,
    @SerialName("dni") val dni: String,
    @SerialName("nombre") val nombre: String,
    @SerialName("fecha_nacimiento") val fechaNacimiento: String? = null,
    @SerialName("club") val club: String? = null,
    @SerialName("roles") val roles: List<String> = emptyList()
)

// ============================================================================
// ENTRENAMIENTO
// ============================================================================

@Serializable
data class Training(
    @SerialName("id") val id: Long? = null, // null al crear, asignado por DB
    @SerialName("user_id") val userId: String, // UUID del propietario
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("synced_at") val syncedAt: String? = null,
    @SerialName("archer_name") val archerName: String? = null,
    @SerialName("training_type") val trainingType: String = "TRAINING", // TRAINING o TOURNAMENT
    @SerialName("is_group") val isGroup: Boolean = false,
    @SerialName("deleted_at") val deletedAt: String? = null
)

// Request para crear/actualizar entrenamiento
@Serializable
data class TrainingRequest(
    @SerialName("user_id") val userId: String,
    @SerialName("archer_name") val archerName: String? = null,
    @SerialName("training_type") val trainingType: String = "TRAINING",
    @SerialName("is_group") val isGroup: Boolean = false
)

// ============================================================================
// SERIE DE ENTRENAMIENTO
// ============================================================================

@Serializable
data class TrainingSeries(
    @SerialName("id") val id: Long? = null,
    @SerialName("training_id") val trainingId: Long,
    @SerialName("series_number") val seriesNumber: Int,
    @SerialName("distance_meters") val distanceMeters: Int,
    @SerialName("category") val category: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("arrows_per_end") val arrowsPerEnd: Int,
    @SerialName("ends_count") val endsCount: Int,
    @SerialName("target_zone_count") val targetZoneCount: Int = 10,
    @SerialName("puntaje_system") val puntajeSystem: String = "X_TO_M",
    // Datos climáticos
    @SerialName("temperature") val temperature: Double? = null,
    @SerialName("wind_speed") val windSpeed: Double? = null,
    @SerialName("wind_speed_unit") val windSpeedUnit: String? = null,
    @SerialName("wind_direction_degrees") val windDirectionDegrees: Int? = null,
    @SerialName("sky_condition") val skyCondition: String? = null,
    @SerialName("location_lat") val locationLat: Double? = null,
    @SerialName("location_lon") val locationLon: Double? = null,
    @SerialName("weather_source") val weatherSource: String? = null,
    @SerialName("recorded_at") val recordedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null
)

// Request para crear/actualizar serie
@Serializable
data class TrainingSeriesRequest(
    @SerialName("training_id") val trainingId: Long,
    @SerialName("series_number") val seriesNumber: Int,
    @SerialName("distance_meters") val distanceMeters: Int,
    @SerialName("category") val category: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("arrows_per_end") val arrowsPerEnd: Int,
    @SerialName("ends_count") val endsCount: Int,
    @SerialName("target_zone_count") val targetZoneCount: Int = 10,
    @SerialName("puntaje_system") val puntajeSystem: String = "X_TO_M",
    @SerialName("temperature") val temperature: Double? = null,
    @SerialName("wind_speed") val windSpeed: Double? = null,
    @SerialName("wind_speed_unit") val windSpeedUnit: String? = null,
    @SerialName("wind_direction_degrees") val windDirectionDegrees: Int? = null,
    @SerialName("sky_condition") val skyCondition: String? = null,
    @SerialName("location_lat") val locationLat: Double? = null,
    @SerialName("location_lon") val locationLon: Double? = null,
    @SerialName("weather_source") val weatherSource: String? = null,
    @SerialName("recorded_at") val recordedAt: String? = null
)

// ============================================================================
// TANDA/END DE ENTRENAMIENTO
// ============================================================================

@Serializable
data class TrainingEnd(
    @SerialName("id") val id: Long? = null,
    @SerialName("series_id") val seriesId: Long,
    @SerialName("end_number") val endNumber: Int,
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    @SerialName("scores_text") val scoresText: String? = null, // JSON string con los puntajes
    @SerialName("total_score") val totalScore: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null
)

// Request para crear/actualizar end
@Serializable
data class TrainingEndRequest(
    @SerialName("series_id") val seriesId: Long,
    @SerialName("end_number") val endNumber: Int,
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    @SerialName("scores_text") val scoresText: String? = null,
    @SerialName("total_score") val totalScore: Int? = null
)

// ============================================================================
// COMPARTIR ENTRENAMIENTO
// ============================================================================

@Serializable
data class TrainingShare(
    @SerialName("id") val id: Long? = null,
    @SerialName("training_id") val trainingId: Long,
    @SerialName("owner_id") val ownerId: String, // UUID
    @SerialName("shared_with_id") val sharedWithId: String, // UUID
    @SerialName("permission") val permission: String = "view", // view, edit, admin
    @SerialName("shared_at") val sharedAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null
)

// Request para crear share
@Serializable
data class ShareTrainingRequest(
    @SerialName("training_id") val trainingId: Long,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("shared_with_id") val sharedWithId: String,
    @SerialName("permission") val permission: String = "view",
    @SerialName("expires_at") val expiresAt: String? = null
)

// Response de entrenamiento con información de compartición
@Serializable
data class TrainingWithShareInfo(
    @SerialName("training_id") val trainingId: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("archer_name") val archerName: String?,
    @SerialName("training_type") val trainingType: String,
    @SerialName("is_group") val isGroup: Boolean,
    @SerialName("is_shared") val isShared: Boolean,
    @SerialName("shared_by_username") val sharedByUsername: String?,
    @SerialName("permission") val permission: String
)

// ============================================================================
// TORNEO
// ============================================================================

@Serializable
data class Tournament(
    @SerialName("id") val id: Long? = null,
    @SerialName("name") val name: String,
    @SerialName("location") val location: String? = null,
    @SerialName("start_date") val startDate: String? = null, // ISO date
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("pdf_url") val pdfUrl: String? = null,
    @SerialName("source") val source: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

// Request para crear torneo
@Serializable
data class TournamentRequest(
    @SerialName("name") val name: String,
    @SerialName("location") val location: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("pdf_url") val pdfUrl: String? = null,
    @SerialName("source") val source: String? = null
)

// ============================================================================
// RESPUESTAS DE OPERACIONES
// ============================================================================

@Serializable
data class SupabaseError(
    @SerialName("message") val message: String,
    @SerialName("code") val code: String? = null,
    @SerialName("details") val details: String? = null
)

// Response genérica para operaciones exitosas
@Serializable
data class OperationResult(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: String? = null // JSON string si hay datos
)
