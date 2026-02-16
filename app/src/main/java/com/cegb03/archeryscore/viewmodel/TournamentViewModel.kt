package com.cegb03.archeryscore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cegb03.archeryscore.data.model.Tournament
import com.cegb03.archeryscore.data.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TournamentViewModel @Inject constructor(
    private val repository: TournamentRepository
) : ViewModel() {
    
    private val _tournaments = MutableStateFlow<List<Tournament>>(emptyList())
    val tournaments: StateFlow<List<Tournament>> = _tournaments.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private var hasLoaded = false
    
    init {
        android.util.Log.d("ArcheryScore_Debug", "🏹 TournamentViewModel - Inicializado (lazy load)")
        // Lazy load - no cargar en init block para evitar crashes al iniciar la app
    }
    
    fun loadTournaments() {
        android.util.Log.d("ArcheryScore_Debug", "🔄 TournamentViewModel.loadTournaments() - Iniciando carga")
        // Evitar cargar dos veces
        if (hasLoaded || _isLoading.value) {
            android.util.Log.d("ArcheryScore_Debug", "⚠️ TournamentViewModel - Ya cargado o en proceso")
            return
        }
        
        hasLoaded = true
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            android.util.Log.d("ArcheryScore_Debug", "🔄 TournamentViewModel - Llamando repository.fetchTournaments()")
            
            repository.fetchTournaments()
                .onSuccess { tournamentList ->
                    android.util.Log.d("ArcheryScore_Debug", "✅ TournamentViewModel - Torneos cargados exitosamente: ${tournamentList.size} torneos")
                    _tournaments.value = tournamentList
                    _isLoading.value = false
                }
                .onFailure { error ->
                    android.util.Log.e("ArcheryScore_Debug", "❌ ERROR en TournamentViewModel - Error al cargar torneos: ${error.message}", error)
                    _errorMessage.value = "Error al cargar torneos: ${error.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun retry() {
        hasLoaded = false
        loadTournaments()
    }
}
