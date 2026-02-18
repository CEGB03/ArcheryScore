package com.cegb03.archeryscore.data.local.training

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TrainingEntity::class,
        TrainingEndEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trainingDao(): TrainingDao
}
