package com.cegb03.archeryscore.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cegb03.archeryscore.data.local.training.TrainingEntity
import com.cegb03.archeryscore.data.local.training.TrainingWithEnds
import com.cegb03.archeryscore.data.model.WeatherSnapshot
import com.cegb03.archeryscore.data.repository.TrainingRepository
import com.cegb03.archeryscore.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingsViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val selectedTrainingId = MutableStateFlow<Long?>(null)

    val trainings: StateFlow<List<TrainingEntity>> = trainingRepository
        .observeTrainings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val trainingDetail: StateFlow<TrainingWithEnds?> = selectedTrainingId
        .flatMapLatest { id ->
            Log.d("ArcheryScore_Debug", "flatMapLatest triggered with id: $id")
            if (id == null) flowOf(null) else trainingRepository.observeTrainingWithEnds(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectTraining(id: Long?) {
        Log.d("ArcheryScore_Debug", "selectTraining called with id: $id")
        selectedTrainingId.value = id
    }

    fun createTraining(
        archerName: String?,
        distanceMeters: Int,
        category: String,
        targetType: String,
        arrowsPerEnd: Int,
        endsCount: Int,
        weather: WeatherSnapshot?,
        locationLat: Double?,
        locationLon: Double?,
        weatherSource: String?,
        targetZoneCount: Int = 10,
        puntajeSystem: String = "X_TO_M"
    ) {
        Log.d("ArcheryScore_Debug", "createTraining called - archer: $archerName, distance: $distanceMeters, category: $category, target: $targetType, arrows: $arrowsPerEnd, ends: $endsCount, zones: $targetZoneCount, system: $puntajeSystem")
        viewModelScope.launch {
            val training = TrainingEntity(
                createdAt = System.currentTimeMillis(),
                archerName = archerName,
                distanceMeters = distanceMeters,
                category = category,
                targetType = targetType,
                arrowsPerEnd = arrowsPerEnd,
                endsCount = endsCount,
                windSpeed = weather?.windSpeed,
                windSpeedUnit = weather?.windSpeedUnit,
                windDirectionDegrees = weather?.windDirectionDegrees,
                skyCondition = weather?.skyCondition,
                locationLat = locationLat,
                locationLon = locationLon,
                weatherSource = weatherSource,
                targetZoneCount = targetZoneCount,
                puntajeSystem = puntajeSystem
            )
            val trainingId = trainingRepository.createTraining(training)
            Log.d("ArcheryScore_Debug", "Training created with ID: $trainingId")
            selectedTrainingId.value = trainingId
        }
    }

    fun confirmEnd(endId: Long, scoresText: String, totalScore: Int) {
        Log.d("ArcheryScore_Debug", "confirmEnd called - endId: $endId, scores: $scoresText, total: $totalScore")
        viewModelScope.launch {
            trainingRepository.confirmEnd(endId, System.currentTimeMillis(), scoresText, totalScore)
            Log.d("ArcheryScore_Debug", "End confirmed successfully")
        }
    }

    fun addNewEnd() {
        viewModelScope.launch {
            val currentTrainingId = selectedTrainingId.value
            Log.d("ArcheryScore_Debug", "addNewEnd called - trainingId: $currentTrainingId")
            if (currentTrainingId != null) {
                trainingRepository.addNewEnd(currentTrainingId)
                Log.d("ArcheryScore_Debug", "New end added successfully")
            } else {
                Log.w("ArcheryScore_Debug", "addNewEnd called but no training selected")
            }
        }
    }

    suspend fun fetchWeather(lat: Double, lon: Double): WeatherSnapshot? {
        return weatherRepository.getCurrentWeather(lat, lon)
    }
}
