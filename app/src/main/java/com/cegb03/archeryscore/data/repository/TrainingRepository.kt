package com.cegb03.archeryscore.data.repository

import android.util.Log
import com.cegb03.archeryscore.data.local.training.SeriesEntity
import com.cegb03.archeryscore.data.local.training.TrainingDao
import com.cegb03.archeryscore.data.local.training.TrainingEndEntity
import com.cegb03.archeryscore.data.local.training.TrainingEntity
import com.cegb03.archeryscore.data.local.training.TrainingWithSeries
import com.cegb03.archeryscore.viewmodel.SeriesFormData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingRepository @Inject constructor(
    private val trainingDao: TrainingDao
) {
    fun observeTrainings(): Flow<List<TrainingEntity>> = trainingDao.observeTrainings()

    fun observeAllTrainingsWithSeries(): Flow<List<TrainingWithSeries>> = trainingDao.observeAllTrainingsWithSeries()

    fun observeTrainingWithSeries(trainingId: Long): Flow<TrainingWithSeries?> {
        Log.d("ArcheryScore_Debug", "observeTrainingWithSeries called for trainingId: $trainingId")
        return trainingDao.observeTrainingWithSeries(trainingId)
    }

    suspend fun createTrainingWithSeries(
        training: TrainingEntity,
        seriesList: List<SeriesFormData>
    ): Long {
        Log.d("ArcheryScore_Debug", "createTrainingWithSeries - name: ${training.archerName}, series: ${seriesList.size}")
        
        // 1. Insertar training
        val trainingId = trainingDao.insertTraining(training)
        Log.d("ArcheryScore_Debug", "Training inserted with ID: $trainingId")
        
        // 2. Insertar cada serie con sus tandas
        seriesList.forEachIndexed { index, seriesData ->
            val series = SeriesEntity(
                trainingId = trainingId,
                seriesNumber = index + 1,
                distanceMeters = seriesData.distanceMeters,
                category = seriesData.category,
                targetType = seriesData.targetType,
                arrowsPerEnd = seriesData.arrowsPerEnd,
                endsCount = seriesData.endsCount,
                targetZoneCount = seriesData.targetZoneCount,
                puntajeSystem = seriesData.puntajeSystem,
                temperature = seriesData.temperature,
                windSpeed = seriesData.weather?.windSpeed,
                windSpeedUnit = seriesData.weather?.windSpeedUnit,
                windDirectionDegrees = seriesData.weather?.windDirectionDegrees,
                skyCondition = seriesData.weather?.skyCondition,
                locationLat = seriesData.locationLat,
                locationLon = seriesData.locationLon,
                weatherSource = seriesData.weatherSource,
                recordedAt = System.currentTimeMillis()
            )
            
            val seriesId = trainingDao.insertSeries(series)
            Log.d("ArcheryScore_Debug", "Series ${index + 1} inserted with ID: $seriesId")
            
            // 3. Crear tandas para esta serie
            val ends = (1..seriesData.endsCount).map { endNumber ->
                TrainingEndEntity(
                    seriesId = seriesId,
                    endNumber = endNumber,
                    confirmedAt = null,
                    scoresText = null,
                    totalScore = null
                )
            }
            trainingDao.insertEnds(ends)
            Log.d("ArcheryScore_Debug", "Inserted ${ends.size} ends for series $seriesId")
        }
        
        return trainingId
    }

    suspend fun confirmEnd(endId: Long, confirmedAt: Long, scoresText: String, totalScore: Int) {
        trainingDao.confirmEnd(endId, confirmedAt, scoresText, totalScore)
    }

    suspend fun addNewEndToSeries(seriesId: Long) {
        Log.d("ArcheryScore_Debug", "addNewEndToSeries called for seriesId: $seriesId")
        val maxEndNumber = trainingDao.getMaxEndNumber(seriesId) ?: 0
        Log.d("ArcheryScore_Debug", "Current maxEndNumber: $maxEndNumber")
        val newEnd = TrainingEndEntity(
            seriesId = seriesId,
            endNumber = maxEndNumber + 1,
            confirmedAt = null,
            scoresText = null,
            totalScore = null
        )
        trainingDao.insertEnd(newEnd)
        Log.d("ArcheryScore_Debug", "Inserted new end with number ${maxEndNumber + 1}")
    }
}
