package com.cegb03.archeryscore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cegb03.archeryscore.data.model.User
import com.cegb03.archeryscore.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _loginSuccess = MutableStateFlow<Boolean?>(null)
    val loginSuccess: StateFlow<Boolean?> = _loginSuccess

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _registrationSuccess = MutableStateFlow<Boolean?>(null)
    val registrationSuccess: StateFlow<Boolean?> = _registrationSuccess

    /**
     * @Deprecated("Usar AuthViewModel.loginWithCredentials() en su lugar")
     * Esta función está obsoleta. AuthViewModel maneja el estado de sesión _isLoggedIn
     * de forma centralizada. Usar esta función no actualizará el estado de autenticación global.
     */
    @Deprecated(
        message = "Usar AuthViewModel.loginWithCredentials() para mantener consistencia en el estado de sesión",
        replaceWith = ReplaceWith("authViewModel.loginWithCredentials(email, password)", "com.cegb03.archeryscore.viewmodel.AuthViewModel")
    )
    fun loginUser(email: String, password: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val (success, message) = repository.loginUser(email, password)
            if (success) {
                _loginSuccess.value = true
                onSuccess?.invoke()
            } else {
                _errorMessage.value = message
                _loginSuccess.value = false
            }
            _isLoading.value = false
        }
    }

    /**
     * @Deprecated("Usar AuthViewModel.registerWithCredentials() en su lugar")
     * Esta función está obsoleta y NO incluye el campo birthDate requerido.
     * AuthViewModel.registerWithCredentials() incluye todos los campos necesarios
     * y maneja el estado de sesión correctamente.
     */
    @Deprecated(
        message = "Usar AuthViewModel.registerWithCredentials() que incluye birthDate y maneja el estado de sesión",
        replaceWith = ReplaceWith("authViewModel.registerWithCredentials(username, email, password, birthDate)", "com.cegb03.archeryscore.viewmodel.AuthViewModel")
    )
    suspend fun registerUser(username: String, email: String, password: String): Pair<Boolean, String?> {
        return repository.registerUser(username, email, password)
    }

    fun clearLoginResult() {
        _loginSuccess.value = null
    }
}