package com.cegb03.archeryscore.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.cegb03.archeryscore.data.local.preference.PreferencesManager
import com.cegb03.archeryscore.data.repository.UserRepository
import com.cegb03.archeryscore.ui.theme.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: UserRepository,
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

    private fun mapAuthError(rawMessage: String?): String {
        val message = rawMessage ?: "Error desconocido"
        return when {
            message.contains("rate limit", ignoreCase = true) ->
                "Limite de intentos alcanzado. Espera 60 segundos y vuelve a intentar."
            message.contains("only request this after", ignoreCase = true) ->
                "Demasiados intentos. Espera 30 segundos y vuelve a intentar."
            else -> message
        }
    }

    // ✅ Función para verificar si ya está logueado al iniciar la app
    fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                _isLoggedIn.value = repository.isAuthenticated()
                _isInitialized.value = true

                Log.d("ArcheryScore_Debug", "🔍 Auth status - LoggedIn: ${_isLoggedIn.value}")
            } catch (e: Exception) {
                Log.e("ArcheryScore_Debug", "❌ Error en checkAuthStatus: ${e.message}")
                _isLoggedIn.value = false
                _isInitialized.value = true
            }
        }
    }

    // ✅ Función para refresh
    fun refresh() {
        viewModelScope.launch {
            _isLoggedIn.value = repository.isAuthenticated()
            Log.d("ArcheryScore_Debug", "🔄 Auth refresh - LoggedIn: ${_isLoggedIn.value}")
        }
    }

    // 🔄 Función opcional para cargar perfil de usuario
    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                Log.d("ArcheryScore_Debug", "🔄 Cargando perfil de usuario...")
                // Aquí puedes cargar datos adicionales del usuario si es necesario
            } catch (e: Exception) {
                Log.e("ArcheryScore_Debug", "Error cargando perfil: ${e.message}")
            }
        }
    }

    fun onLoginSuccess(navController: NavController) {
        viewModelScope.launch {
            navController.navigate(Screen.Feed.route) {
                popUpTo(0) { inclusive = true } // ✅ Limpia toda la pila
                launchSingleTop = true
            }
            Log.d("ArcheryScore_Debug", "✅ Navegando a Feed desde AuthViewModel")
        }
    }

    // ✅ Función para logout
    fun logout(onComplete: () -> Unit = {}) {
        Log.d("ArcheryScore_Debug", "🚪 Iniciando logout - limpiando token y preferencias")
        viewModelScope.launch {
            try {
                repository.logout()
                Log.d("ArcheryScore_Debug", "✅ Sesión cerrada en Supabase")

                // 2️⃣ Limpiar DataStore COMPLETO (incluyendo biometría)
                Log.d("ArcheryScore_Debug", "🧹 Limpiando todas las preferencias del DataStore")
                preferencesManager.clearAll()
                Log.d("ArcheryScore_Debug", "✅ DataStore completamente limpiado")
                
                // 3️⃣ Actualizar estado
                _isLoggedIn.value = false
                Log.d("ArcheryScore_Debug", "✅ Token limpiado, DataStore limpiado")
                Log.i("ArcheryScore_Debug", "✅ Cierre de sesión exitoso")
                
                // 4️⃣ Ejecutar callback
                onComplete()
                Log.d("ArcheryScore_Debug", "🚪 Usuario hizo logout")
            } catch (e: Exception) {
                Log.e("ArcheryScore_Debug", "❌ Error en logout", e)
            }
        }
    }

    // ✅ Función para limpiar estado de autenticación
    fun clearAuthState() {
        _errorMessage.value = null
        Log.d("ArcheryScore_Debug", "🧹 Estado de auth limpiado")
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
    fun loginWithCredentials(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _errorMessage.value = "Usuario y contraseña son requeridos"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Llamar al repositorio para validar credenciales
                val (success, message) = repository.loginUser(email, password)
                
                if (success) {
                    _isLoggedIn.value = true
                    Log.d("ArcheryScore_Debug", "✅ Login exitoso para: $email")
                    _errorMessage.value = null
                } else {
                    _isLoggedIn.value = false
                    _errorMessage.value = mapAuthError(message ?: "Error en login")
                    Log.e("ArcheryScore_Debug", "❌ Error en login: $message")
                }
            } catch (e: Exception) {
                _isLoggedIn.value = false
                _errorMessage.value = mapAuthError(e.message)
                Log.e("ArcheryScore_Debug", "❌ Excepción en login", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ Registrarse con usuario, contraseña y fecha de nacimiento
    fun registerWithCredentials(
        username: String,
        email: String,
        password: String
    ) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
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
                    Log.d("ArcheryScore_Debug", "✅ Registro exitoso para: $email")
                    _errorMessage.value = null
                } else {
                    _isLoggedIn.value = false
                    _errorMessage.value = mapAuthError(message ?: "Error en registro")
                    Log.e("ArcheryScore_Debug", "❌ Error en registro: $message")
                }
            } catch (e: Exception) {
                _isLoggedIn.value = false
                _errorMessage.value = mapAuthError(e.message)
                Log.e("ArcheryScore_Debug", "❌ Excepción en registro", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
