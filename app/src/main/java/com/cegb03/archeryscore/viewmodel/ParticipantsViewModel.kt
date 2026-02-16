package com.cegb03.archeryscore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cegb03.archeryscore.data.model.ParticipantsPage
import com.cegb03.archeryscore.data.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParticipantsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val data: ParticipantsPage? = null
)

@HiltViewModel
class ParticipantsViewModel @Inject constructor(
    private val repository: TournamentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ParticipantsUiState())
    val state: StateFlow<ParticipantsUiState> = _state.asStateFlow()

    private var lastUrl: String? = null

    fun load(url: String) {
        if (url == lastUrl && _state.value.data != null) return
        lastUrl = url
        _state.value = ParticipantsUiState(isLoading = true)

        viewModelScope.launch {
            repository.fetchParticipants(url)
                .onSuccess { data ->
                    _state.value = ParticipantsUiState(data = data)
                }
                .onFailure { error ->
                    _state.value = ParticipantsUiState(errorMessage = error.message)
                }
        }
    }
}
