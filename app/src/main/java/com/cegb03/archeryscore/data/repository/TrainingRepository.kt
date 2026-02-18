package com.cegb03.archeryscore.data.repository

import android.util.Log
import com.cegb03.archeryscore.data.local.training.TrainingDao
import com.cegb03.archeryscore.data.local.training.TrainingEndEntity
import com.cegb03.archeryscore.data.local.training.TrainingEntity
import com.cegb03.archeryscore.data.local.training.TrainingWithEnds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingRepository @Inject constructor(
    private val trainingDao: TrainingDao
) {
    fun observeTrainings(): Flow<List<TrainingEntity>> = trainingDao.observeTrainings()

    fun observeTrainingWithEnds(trainingId: Long): Flow<TrainingWithEnds?> {
        Log.d("ArcheryScore_Debug", "observeTrainingWithEnds called for trainingId: $trainingId")
        return combine(
            trainingDao.observeTrainingById(trainingId),
            trainingDao.observeTrainingEnds(trainingId)
        ) { training, ends ->
            Log.d("ArcheryScore_Debug", "Flow emission - training: ${training != null}, ends: ${ends.size}")
            if (training != null) {
                TrainingWithEnds(training = training, ends = ends)
            } else {
                null
            }
        }
    }

    suspend fun createTraining(training: TrainingEntity): Long {
        Log.d("ArcheryScore_Debug", "createTraining called - name: ${training.archerName}, ends: ${training.endsCount}")
        val trainingId = trainingDao.insertTraining(training)
        Log.d("ArcheryScore_Debug", "Training inserted with ID: $trainingId")
        val ends = (1..training.endsCount).map { number ->
            TrainingEndEntity(
                trainingId = trainingId,
                endNumber = number,
                confirmedAt = null,
                scoresText = null,
                totalScore = null
            )
        }
        trainingDao.insertEnds(ends)
        Log.d("ArcheryScore_Debug", "Inserted ${ends.size} ends for training $trainingId")
        return trainingId
    }

    suspend fun confirmEnd(endId: Long, confirmedAt: Long, scoresText: String, totalScore: Int) {
        trainingDao.confirmEnd(endId, confirmedAt, scoresText, totalScore)
    }

    suspend fun addNewEnd(trainingId: Long) {
        Log.d("ArcheryScore_Debug", "addNewEnd called for trainingId: $trainingId")
        val maxEndNumber = trainingDao.getMaxEndNumber(trainingId) ?: 0
        Log.d("ArcheryScore_Debug", "Current maxEndNumber: $maxEndNumber")
        val newEnd = TrainingEndEntity(
            trainingId = trainingId,
            endNumber = maxEndNumber + 1,
            confirmedAt = null,
            scoresText = null,
            totalScore = null
        )
        trainingDao.insertEnd(newEnd)
        Log.d("ArcheryScore_Debug", "Inserted new end with number ${maxEndNumber + 1}")
    }
}
