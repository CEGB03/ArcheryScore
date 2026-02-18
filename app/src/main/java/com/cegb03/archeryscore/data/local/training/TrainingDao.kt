package com.cegb03.archeryscore.data.local.training

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {
    // === TRAININGS ===
    @Query("SELECT * FROM trainings ORDER BY createdAt DESC")
    fun observeTrainings(): Flow<List<TrainingEntity>>

    @Query("SELECT * FROM trainings WHERE id = :trainingId")
    fun observeTrainingById(trainingId: Long): Flow<TrainingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraining(training: TrainingEntity): Long

    // === SERIES ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesEntity): Long

    @Query("SELECT * FROM training_series WHERE trainingId = :trainingId ORDER BY seriesNumber")
    fun observeSeriesByTraining(trainingId: Long): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM training_series WHERE id = :seriesId")
    suspend fun getSeriesById(seriesId: Long): SeriesEntity?

    @Query("SELECT MAX(seriesNumber) FROM training_series WHERE trainingId = :trainingId")
    suspend fun getMaxSeriesNumber(trainingId: Long): Int?

    // === ENDS ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnds(ends: List<TrainingEndEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnd(end: TrainingEndEntity)

    @Query("SELECT * FROM training_ends WHERE seriesId = :seriesId ORDER BY endNumber")
    fun observeEndsBySeries(seriesId: Long): Flow<List<TrainingEndEntity>>

    @Query("SELECT * FROM training_ends WHERE seriesId = :seriesId ORDER BY endNumber")
    suspend fun getEndsBySeries(seriesId: Long): List<TrainingEndEntity>

    @Query("SELECT * FROM training_ends WHERE seriesId = :seriesId AND confirmedAt IS NULL ORDER BY endNumber LIMIT 1")
    suspend fun getNextPendingEnd(seriesId: Long): TrainingEndEntity?

    @Query("UPDATE training_ends SET confirmedAt = :confirmedAt, scoresText = :scoresText, totalScore = :totalScore WHERE id = :endId")
    suspend fun confirmEnd(endId: Long, confirmedAt: Long, scoresText: String, totalScore: Int)

    @Query("SELECT MAX(endNumber) FROM training_ends WHERE seriesId = :seriesId")
    suspend fun getMaxEndNumber(seriesId: Long): Int?

    // === RELACIONES COMPLETAS ===
    @Transaction
    @Query("SELECT * FROM trainings ORDER BY createdAt DESC")
    fun observeAllTrainingsWithSeries(): Flow<List<TrainingWithSeries>>

    @Transaction
    @Query("SELECT * FROM trainings WHERE id = :trainingId")
    fun observeTrainingWithSeries(trainingId: Long): Flow<TrainingWithSeries?>

    @Transaction
    @Query("SELECT * FROM training_series WHERE id = :seriesId")
    fun observeSeriesWithEnds(seriesId: Long): Flow<SeriesWithEnds?>
}
