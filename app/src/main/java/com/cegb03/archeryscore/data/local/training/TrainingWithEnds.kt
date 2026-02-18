package com.cegb03.archeryscore.data.local.training

import androidx.room.Embedded
import androidx.room.Relation

data class TrainingWithEnds(
    @Embedded val training: TrainingEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "trainingId"
    )
    val ends: List<TrainingEndEntity>
)
