package com.cegb03.archeryscore.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cegb03.archeryscore.data.model.InvitationContent
import com.cegb03.archeryscore.data.model.InvitationHtml
import com.cegb03.archeryscore.data.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class InvitationUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val html: InvitationHtml? = null,
    val pdfFile: File? = null
)

@HiltViewModel
class InvitationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TournamentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InvitationUiState())
    val state: StateFlow<InvitationUiState> = _state.asStateFlow()

    private var lastUrl: String? = null

    fun load(url: String) {
        if (url == lastUrl && (_state.value.html != null || _state.value.pdfFile != null)) return
        lastUrl = url
        _state.value = InvitationUiState(isLoading = true)

        viewModelScope.launch {
            repository.fetchInvitation(url)
                .onSuccess { content ->
                    when (content) {
                        is InvitationContent.Html -> {
                            _state.value = InvitationUiState(html = content.data)
                        }
                        is InvitationContent.Pdf -> {
                            val file = writePdfToCache(url, content.bytes)
                            _state.value = InvitationUiState(pdfFile = file)
                        }
                    }
                }
                .onFailure { error ->
                    _state.value = InvitationUiState(errorMessage = error.message)
                }
        }
    }

    private fun writePdfToCache(url: String, bytes: ByteArray): File {
        val safeName = "invitation_${url.hashCode()}.pdf"
        val file = File(context.cacheDir, safeName)
        file.writeBytes(bytes)
        return file
    }
}
