package com.cegb03.archeryscore.data.local.training

import androidx.room.Embedded
import androidx.room.Relation

// Relación completa: Training -> Series -> Ends
data class TrainingWithSeries(
    @Embedded val training: TrainingEntity,
    @Relation(
        entity = SeriesEntity::class,
        parentColumn = "id",
        entityColumn = "trainingId"
    )
    val series: List<SeriesWithEnds>
)

// Relación intermedia: Series -> Ends
data class SeriesWithEnds(
    @Embedded val series: SeriesEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "seriesId"
    )
    val ends: List<TrainingEndEntity>
)
