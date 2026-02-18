package com.cegb03.archeryscore.di

import android.content.Context
import androidx.room.Room
import com.cegb03.archeryscore.data.local.training.AppDatabase
import com.cegb03.archeryscore.data.local.training.MIGRATION_4_5
import com.cegb03.archeryscore.data.local.training.TrainingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "archeryscore.db"
        )
            .addMigrations(MIGRATION_4_5)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTrainingDao(db: AppDatabase): TrainingDao = db.trainingDao()
}
