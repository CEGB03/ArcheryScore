package com.cegb03.archeryscore.data.local.training

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Entidad principal del entrenamiento - Solo campos globales
@Entity(tableName = "trainings")
data class TrainingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val archerName: String?
)

// Nueva entidad Series - Cada serie tiene sus propios parámetros
@Entity(
    tableName = "training_series",
    foreignKeys = [
        ForeignKey(
            entity = TrainingEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trainingId")]
)
data class SeriesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trainingId: Long,
    val seriesNumber: Int,  // Orden de la serie (1, 2, 3...)
    val distanceMeters: Int,
    val category: String,
    val targetType: String,
    val arrowsPerEnd: Int,
    val endsCount: Int,
    val targetZoneCount: Int = 10, // 6 o 10 zonas
    val puntajeSystem: String = "X_TO_M", // X_TO_M o 11_TO_M
    // Datos climáticos propios de cada serie
    val temperature: Double?,  // NUEVO: temperatura en Celsius
    val windSpeed: Double?,
    val windSpeedUnit: String?,
    val windDirectionDegrees: Int?,
    val skyCondition: String?,
    val locationLat: Double?,
    val locationLon: Double?,
    val weatherSource: String?,
    val recordedAt: Long?  // Timestamp de cuando se grabaron estos datos
)

// Tandas - Ahora pertenecen a una serie específica
@Entity(
    tableName = "training_ends",
    foreignKeys = [
        ForeignKey(
            entity = SeriesEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("seriesId")]
)
data class TrainingEndEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seriesId: Long,  // Ahora pertenece a una serie, no directamente al training
    val endNumber: Int,
    val confirmedAt: Long?,
    val scoresText: String?,
    val totalScore: Int?
)
