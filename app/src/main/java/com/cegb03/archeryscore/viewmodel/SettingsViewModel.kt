package com.cegb03.archeryscore.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cegb03.archeryscore.data.local.preference.PreferencesManager
import com.cegb03.archeryscore.data.model.FatarcoVerificationResult
import com.cegb03.archeryscore.data.model.User
import com.cegb03.archeryscore.data.repository.FatarcoRepository
import com.cegb03.archeryscore.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val repository: UserRepository,
    private val preferencesManager: PreferencesManager,
    private val fatarcoRepository: FatarcoRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _logoutSuccess = MutableStateFlow(false)
    val logoutSuccess: StateFlow<Boolean> = _logoutSuccess.asStateFlow()

    private val _changePasswordResult = MutableStateFlow<Result<Unit>?>(null)
    val changePasswordResult: StateFlow<Result<Unit>?> = _changePasswordResult.asStateFlow()

    private val _fatarcoVerificationResult = MutableStateFlow<FatarcoVerificationResult?>(null)
    val fatarcoVerificationResult: StateFlow<FatarcoVerificationResult?> = _fatarcoVerificationResult.asStateFlow()

    val notificationsEnabled: StateFlow<Boolean> = preferencesManager.notificationsEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val biometricEnabled: StateFlow<Boolean> = preferencesManager.biometricEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Supabase-only: Google login no aplica
    val isGoogleUser: StateFlow<Boolean> = MutableStateFlow(false)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Control de carga para no repetir innecesariamente
    private var userLoaded = false
    private var attemptedWithoutToken = false

    /**
     * Asegura que el usuario se cargue solo una vez si hay token válido.
     */
    fun ensureUserLoaded() {
        if (userLoaded) return
        val isAuthenticated = repository.isAuthenticated()
        Log.i("ArcheryScore_Debug", "ensureUserLoaded: isAuthenticated=$isAuthenticated")
        if (!isAuthenticated) {
            if (!attemptedWithoutToken) {
                Log.i("ArcheryScore_Debug", "No hay sesion Supabase, no se carga usuario todavía")
                attemptedWithoutToken = true
            }
            return
        }
        loadCurrentUserInternal()
    }

    private fun loadCurrentUserInternal() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val fetched = repository.getCurrentUser()
                Log.i("ArcheryScore_Debug", "loadCurrentUserInternal: fetched=$fetched")
                if (fetched != null) {
                    _user.value = fetched
                    userLoaded = true
                } else {
                    _errorMessage.value = "No se pudo obtener el usuario"
                    Log.w("ArcheryScore_Debug", "No se pudo obtener el usuario (fetched=null)")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar el usuario: ${e.localizedMessage ?: "desconocido"}"
                Log.e("ArcheryScore_Debug", "Exception loading current user", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fuerza recarga (por ejemplo tras login).
     */
    fun refreshUserIfLoggedIn() {
        userLoaded = false
        attemptedWithoutToken = false
        ensureUserLoaded()
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                Log.d("ArcheryScore_Debug", "🚪 Iniciando logout - limpiando token y preferencias")
                repository.logout()
                preferencesManager.clearAll()  // ← Limpiar DataStore (biometricEnabled, notificationsEnabled, etc)
                Log.d("ArcheryScore_Debug", "✅ Token limpiado, DataStore limpiado")
                
                userLoaded = false
                attemptedWithoutToken = false
                _user.value = null
                _logoutSuccess.value = true
                Log.i("ArcheryScore_Debug", "✅ Cierre de sesión exitoso")
            } catch (e: Exception) {
                _errorMessage.value = "Error al cerrar sesión: ${e.localizedMessage ?: "desconocido"}"
                _logoutSuccess.value = false
                Log.e("ArcheryScore_Debug", "❌ Error en logout", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(updatedUser: User) {
        Log.d("ArcheryScore_Debug", "🔄 updateUser iniciado - user=${updatedUser.id}")
        Log.d("ArcheryScore_Debug", "   username: ${updatedUser.username}")
        Log.d("ArcheryScore_Debug", "   email: ${updatedUser.email}")
        Log.d("ArcheryScore_Debug", "   tel: ${updatedUser.tel}")
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                Log.d("ArcheryScore_Debug", "📤 Enviando PUT /api/users/${updatedUser.id}")
                val updated = repository.updateUser(updatedUser)
                Log.d("ArcheryScore_Debug", "✅ updateUser respuesta - updated=${updated != null}")

                if (updated != null) {
                    _user.value = updated
                    Log.d("ArcheryScore_Debug", "✅ Usuario actualizado correctamente en ViewModel: $updated")
                } else {
                    _errorMessage.value = "No pudimos actualizar tus datos. Intenta más tarde."
                    Log.e("ArcheryScore_Debug", "❌ updateUser devolvió null")
                }
            } catch (e: Exception) {
                val errorMsg = "Error al actualizar usuario: ${e.localizedMessage ?: e.message ?: "desconocido"}"
                _errorMessage.value = errorMsg
                Log.e("ArcheryScore_Debug", "❌ Exception en updateUser - ${e::class.simpleName}: ${e.message}", e)
            } finally {
                _isLoading.value = false
                Log.d("ArcheryScore_Debug", "🔚 updateUser finalizado - loading=false")
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        Log.d("ArcheryScore_Debug", "🔐 SettingsViewModel.setBiometricEnabled llamado: $enabled")
        viewModelScope.launch {
            try {
                preferencesManager.setBiometricEnabled(enabled)
                Log.d("ArcheryScore_Debug", "✅ Biometric setting guardado en PreferencesManager")
            } catch (e: Exception) {
                Log.e("ArcheryScore_Debug", "❌ Error al guardar biometric setting", e)
            }
        }
    }

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val success = repository.changePassword(newPassword)
                if (success) {
                    _changePasswordResult.value = Result.success(Unit)
                } else {
                    _changePasswordResult.value = Result.failure(Exception("Cambio fallido"))
                    _errorMessage.value = "Error al cambiar contraseña"
                }
            } catch (e: Exception) {
                _changePasswordResult.value = Result.failure(e)
                _errorMessage.value =
                    "Error al cambiar contraseña: ${e.localizedMessage ?: "desconocido"}"
                Log.e("ArcheryScore_Debug", "Exception changing password", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearLogoutFlag() {
        _logoutSuccess.value = false
    }

    fun clearChangePasswordResult() {
        _changePasswordResult.value = null
    }


    /**
     * Verifica si el usuario existe en FATARCO por su DNI
     */
    fun verifyFatarco(dni: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _fatarcoVerificationResult.value = null
            
            try {
                Log.d("ArcheryScore_Debug", "🔍 Verificando DNI en FATARCO: $dni")
                val result = fatarcoRepository.verifyUserByDni(dni)
                _fatarcoVerificationResult.value = result
                
                when (result) {
                    is FatarcoVerificationResult.Success -> {
                        Log.d("ArcheryScore_Debug", "✅ Verificación exitosa: ${result.data.nombre}")
                    }
                    is FatarcoVerificationResult.NotFound -> {
                        Log.w("ArcheryScore_Debug", "⚠️ DNI no encontrado en FATARCO")
                    }
                    is FatarcoVerificationResult.Error -> {
                        Log.e("ArcheryScore_Debug", "❌ Error en verificación: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al verificar con FATARCO: ${e.localizedMessage ?: "desconocido"}"
                Log.e("ArcheryScore_Debug", "❌ Exception en verifyFatarco", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearFatarcoVerification() {
        _fatarcoVerificationResult.value = null
    }
    fun clearError() {
        _errorMessage.value = null
    }
}