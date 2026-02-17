package com.cegb03.archeryscore.data.local.training

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "trainings")
data class TrainingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val archerName: String?,
    val distanceMeters: Int,
    val category: String,
    val targetType: String,
    val arrowsPerEnd: Int,
    val endsCount: Int,
    val windSpeed: Double?,
    val windSpeedUnit: String?,
    val windDirectionDegrees: Int?,
    val skyCondition: String?,
    val locationLat: Double?,
    val locationLon: Double?,
    val weatherSource: String?,
    val targetZoneCount: Int = 10, // 6 o 10 zonas
    val puntajeSystem: String = "X_TO_M" // X_TO_M o 11_TO_M
)

@Entity(
    tableName = "training_ends",
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
data class TrainingEndEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trainingId: Long,
    val endNumber: Int,
    val confirmedAt: Long?,
    val scoresText: String?,
    val totalScore: Int?
)
