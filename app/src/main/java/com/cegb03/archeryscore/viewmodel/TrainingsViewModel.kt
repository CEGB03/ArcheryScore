package com.cegb03.archeryscore.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cegb03.archeryscore.data.local.training.SeriesEntity
import com.cegb03.archeryscore.data.local.training.TrainingEntity
import com.cegb03.archeryscore.data.local.training.TrainingWithSeries
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

// Data class para representar una serie en el formulario
data class SeriesFormData(
    val distanceMeters: Int,
    val category: String,
    val targetType: String,
    val arrowsPerEnd: Int,
    val endsCount: Int,
    val targetZoneCount: Int = 10,
    val puntajeSystem: String = "X_TO_M",
    val temperature: Double?,
    val weather: WeatherSnapshot?,
    val locationLat: Double?,
    val locationLon: Double?,
    val weatherSource: String?
)

@HiltViewModel
class TrainingsViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val selectedTrainingId = MutableStateFlow<Long?>(null)

    val trainings: StateFlow<List<TrainingWithSeries>> = trainingRepository
        .observeAllTrainingsWithSeries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val trainingDetail: StateFlow<TrainingWithSeries?> = selectedTrainingId
        .flatMapLatest { id ->
            Log.d("ArcheryScore_Debug", "flatMapLatest triggered with id: $id")
            if (id == null) flowOf(null) else trainingRepository.observeTrainingWithSeries(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectTraining(id: Long?) {
        Log.d("ArcheryScore_Debug", "selectTraining called with id: $id")
        selectedTrainingId.value = id
    }

    // Nueva función para crear training con múltiples series
    fun createTrainingWithSeries(
        archerName: String?,
        seriesList: List<SeriesFormData>,
        trainingType: String,
        isGroup: Boolean
    ) {
        Log.d("ArcheryScore_Debug", "createTrainingWithSeries - archer: $archerName, series count: ${seriesList.size}")
        viewModelScope.launch {
            val training = TrainingEntity(
                createdAt = System.currentTimeMillis(),
                archerName = archerName,
                trainingType = trainingType,
                isGroup = isGroup
            )
            val trainingId = trainingRepository.createTrainingWithSeries(training, seriesList)
            Log.d("ArcheryScore_Debug", "Training with series created with ID: $trainingId")
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

    fun addNewEndToSeries(seriesId: Long) {
        viewModelScope.launch {
            Log.d("ArcheryScore_Debug", "addNewEndToSeries called - seriesId: $seriesId")
            trainingRepository.addNewEndToSeries(seriesId)
            Log.d("ArcheryScore_Debug", "New end added to series successfully")
        }
    }

    suspend fun fetchWeather(lat: Double, lon: Double): WeatherSnapshot? {
        return weatherRepository.getCurrentWeather(lat, lon)
    }
}
