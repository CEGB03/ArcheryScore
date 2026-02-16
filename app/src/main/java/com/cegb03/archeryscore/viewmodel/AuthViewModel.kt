package com.cegb03.archeryscore.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.cegb03.archeryscore.data.local.AuthTokenProvider
import com.cegb03.archeryscore.data.local.preference.PreferencesManager
import com.cegb03.archeryscore.data.model.GoogleUser
import com.cegb03.archeryscore.data.repository.UserRepository
import com.cegb03.archeryscore.ui.theme.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: UserRepository,
    private val tokenProvider: AuthTokenProvider,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    init {
        Log.d("ArcheryScore_Debug", "🔐 AuthViewModel - Inicializado")
    }

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    // ✅ Función para verificar si ya está logueado al iniciar la app
    fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                // ⏰ Pequeño delay para asegurar que el token esté disponible
                kotlinx.coroutines.delay(500)

                val token = tokenProvider.getToken()
                val userId = tokenProvider.getUserId()

                // Verificación más robusta
                val isValidSession = token != null &&
                        userId != null &&
                        token.isNotBlank() &&
                        userId > 0

                _isLoggedIn.value = isValidSession
                _isInitialized.value = true

                Log.d("DebugDev", "🔍 Auth status - Token: ${token?.take(10)}..., UserId: $userId")
                Log.d("DebugDev", "🔍 Auth status - LoggedIn: ${_isLoggedIn.value}")
            } catch (e: Exception) {
                Log.e("DebugDev", "❌ Error en checkAuthStatus: ${e.message}")
                _isLoggedIn.value = false
                _isInitialized.value = true
            }
        }
    }

    // ✅ Función para refresh
    fun refresh() {
        viewModelScope.launch {
            val token = tokenProvider.getToken()
            val userId = tokenProvider.getUserId()
            _isLoggedIn.value = token != null && userId != null
            Log.d("DebugDev", "🔄 Auth refresh - LoggedIn: ${_isLoggedIn.value}")
        }
    }

    // 🔄 Función opcional para cargar perfil de usuario
    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                Log.d("DebugDev", "🔄 Cargando perfil de usuario...")
                // Aquí puedes cargar datos adicionales del usuario si es necesario
            } catch (e: Exception) {
                Log.e("DebugDev", "Error cargando perfil: ${e.message}")
            }
        }
    }

    fun onLoginSuccess(navController: NavController) {
        viewModelScope.launch {
            navController.navigate(Screen.Feed.route) {
                popUpTo(0) { inclusive = true } // ✅ Limpia toda la pila
                launchSingleTop = true
            }
            Log.d("DebugDev", "✅ Navegando a Feed desde AuthViewModel")
        }
    }

    // ✅ Función para logout
    fun logout(onComplete: () -> Unit = {}) {
        Log.d("DebugDev", "🚪 Iniciando logout - limpiando token y preferencias")
        viewModelScope.launch {
            try {
                // 1️⃣ Limpiar token
                tokenProvider.clearToken()
                Log.d("DebugDev", "✅ Token limpiado")
//                biometricEnabledFlow
                // 2️⃣ Limpiar DataStore COMPLETO (incluyendo biometría)
                Log.d("DebugDev", "🧹 Limpiando todas las preferencias del DataStore")
                preferencesManager.clearAll()
                Log.d("DebugDev", "✅ DataStore completamente limpiado")
                
                // 3️⃣ Actualizar estado
                _isLoggedIn.value = false
                Log.d("DebugDev", "✅ Token limpiado, DataStore limpiado")
                Log.i("DebugDev", "✅ Cierre de sesión exitoso")
                
                // 4️⃣ Ejecutar callback
                onComplete()
                Log.d("DebugDev", "🚪 Usuario hizo logout")
            } catch (e: Exception) {
                Log.e("DebugDev", "❌ Error en logout", e)
            }
        }
    }

    // ✅ Función para limpiar estado de autenticación
    fun clearAuthState() {
        _errorMessage.value = null
        Log.d("DebugDev", "🧹 Estado de auth limpiado")
    }

    // ✅ Función para limpiar errores
    fun clearError() {
        _errorMessage.value = null
    }

    // ════════════════════════════════════════════════════════════════════════════════
    // 🔐 AUTENTICACIÓN - Funciones principales (Única fuente de verdad)
    // ════════════════════════════════════════════════════════════════════════════════
    // ⚠️ NOTA: UserViewModel.loginUser() y UserViewModel.registerUser() están
    //    marcadas como @Deprecated. Usar únicamente estas funciones de AuthViewModel
    //    para mantener consistencia en el estado de sesión global (_isLoggedIn).
    // ════════════════════════════════════════════════════════════════════════════════

    // ✅ Login con usuario y contraseña (sin dependencia de Google)
    fun loginWithCredentials(username: String, password: String) {
        if (username.isEmpty() || password.isEmpty()) {
            _errorMessage.value = "Usuario y contraseña son requeridos"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Llamar al repositorio para validar credenciales
                val (success, message) = repository.loginUser(username, password)
                
                if (success) {
                    _isLoggedIn.value = true
                    Log.d("DebugDev", "✅ Login exitoso para: $username")
                    _errorMessage.value = null
                } else {
                    _isLoggedIn.value = false
                    _errorMessage.value = message ?: "Error en login"
                    Log.e("DebugDev", "❌ Error en login: $message")
                }
            } catch (e: Exception) {
                _isLoggedIn.value = false
                _errorMessage.value = e.message ?: "Error desconocido"
                Log.e("DebugDev", "❌ Excepción en login", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ Registrarse con usuario, contraseña y fecha de nacimiento
    fun registerWithCredentials(
        username: String,
        email: String,
        password: String,
        birthDate: String
    ) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || birthDate.isEmpty()) {
            _errorMessage.value = "Todos los campos son requeridos"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Llamar al repositorio para registrar usuario
                val (success, message) = repository.registerUser(username, email, password)
                
                if (success) {
                    _isLoggedIn.value = true
                    Log.d("DebugDev", "✅ Registro exitoso para: $email")
                    _errorMessage.value = null
                } else {
                    _isLoggedIn.value = false
                    _errorMessage.value = message ?: "Error en registro"
                    Log.e("DebugDev", "❌ Error en registro: $message")
                }
            } catch (e: Exception) {
                _isLoggedIn.value = false
                _errorMessage.value = e.message ?: "Error desconocido"
                Log.e("DebugDev", "❌ Excepción en registro", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
