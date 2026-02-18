package com.cegb03.archeryscore.data.local.training

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {
    @Query("SELECT * FROM trainings ORDER BY createdAt DESC")
    fun observeTrainings(): Flow<List<TrainingEntity>>

    @Query("SELECT * FROM trainings WHERE id = :trainingId")
    fun observeTrainingById(trainingId: Long): Flow<TrainingEntity?>

    @Query("SELECT * FROM training_ends WHERE trainingId = :trainingId ORDER BY endNumber")
    fun observeTrainingEnds(trainingId: Long): Flow<List<TrainingEndEntity>>

    @Transaction
    @Query("SELECT * FROM trainings WHERE id = :trainingId")
    fun observeTrainingWithEnds(trainingId: Long): Flow<TrainingWithEnds?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraining(training: TrainingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnds(ends: List<TrainingEndEntity>)

    @Query("SELECT * FROM training_ends WHERE trainingId = :trainingId AND confirmedAt IS NULL ORDER BY endNumber LIMIT 1")
    suspend fun getNextPendingEnd(trainingId: Long): TrainingEndEntity?

    @Query("UPDATE training_ends SET confirmedAt = :confirmedAt, scoresText = :scoresText, totalScore = :totalScore WHERE id = :endId")
    suspend fun confirmEnd(endId: Long, confirmedAt: Long, scoresText: String, totalScore: Int)

    @Query("SELECT MAX(endNumber) FROM training_ends WHERE trainingId = :trainingId")
    suspend fun getMaxEndNumber(trainingId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnd(end: TrainingEndEntity)

    @Query("SELECT * FROM training_ends WHERE trainingId = :trainingId ORDER BY endNumber")
    suspend fun getTrainingEnds(trainingId: Long): List<TrainingEndEntity>
}
