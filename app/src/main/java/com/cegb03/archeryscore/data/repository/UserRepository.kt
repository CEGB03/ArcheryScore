package com.cegb03.archeryscore.data.repository

import android.content.Context
import android.util.Log
import com.cegb03.archeryscore.data.local.AuthTokenProvider
import com.cegb03.archeryscore.data.model.UpdateUserRequest
import com.cegb03.archeryscore.data.model.User
import com.cegb03.archeryscore.data.model.LoginResponse
import com.cegb03.archeryscore.data.remote.ApiService
import com.cegb03.archeryscore.data.remote.PasswordChangeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException
import retrofit2.Response
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

@Singleton
class UserRepository @Inject constructor(
    private val api: ApiService,
    private val tokenProvider: AuthTokenProvider,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
    }

    suspend fun loginUser(email: String, password: String): Pair<Boolean, String?> {
        Log.i("ArcheryScore_Debug", "Iniciando login para email=$email")
        return try {
            val credentials = mapOf("email" to email, "password" to password)

            // ✅ Agregar timeout específico para login
            val response = try {
                withTimeout(15000) { // 15 segundos máximo
                    api.loginUser(credentials)
                }
            } catch (timeout: TimeoutCancellationException) {
                Log.e("ArcheryScore_Debug", "⏰ Timeout en login")
                return Pair(false, "El servidor no respondió. Intenta más tarde.")
            }

            if (response.isSuccessful) {
                val body = response.body()
                val token = body?.token
                if (!token.isNullOrBlank()) {
                    tokenProvider.saveToken(token)
                    // intentar obtener userId por email del backend
                    val user = try {
                        api.getUserByMail(email)
                    } catch (e: Exception) {
                        Log.w("ArcheryScore_Debug", "No se pudo obtener usuario por email", e)
                        null
                    }
                    val userId = user?.id
                    if (userId != null) {
                        tokenProvider.saveUserId(userId)
                        Log.i("ArcheryScore_Debug", "Login successful, token e id guardados (id=$userId)")
                        Pair(true, null)
                    } else {
                        Log.w("ArcheryScore_Debug", "Login exitoso pero no se obtuvo userId")
                        Pair(true, null) // aún permitimos login pero sin userId
                    }
                } else {
                    Log.w("ArcheryScore_Debug", "Login fallido: token no recibido")
                    Pair(false, "Token no recibido")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Pair(false, "Error del servidor: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "Exception durante login", e)

            // ✅ Mensajes de error más específicos
            val errorMessage = when {
                e is TimeoutCancellationException -> "Timeout: El servidor no respondió"
                e.message?.contains("socket", true) == true -> "Error de conexión. Verifica tu internet"
                e.message?.contains("failed to connect", true) == true -> "No se puede conectar al servidor"
                else -> "Error al iniciar sesión: ${e.localizedMessage ?: e.message}"
            }

            Pair(false, errorMessage)
        }
    }

    suspend fun registerUser(username: String, email: String, password: String): Pair<Boolean, String?> {
        return try {
            val user = User(username = username, email = email, password = password)
            val response = api.newUser(user)
            if (response.isSuccessful) {
                Pair(true, null)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Pair(false, "Error del servidor: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "Exception during register", e)
            Pair(false, "Error al registrar usuario: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun getCurrentUser(): User? {
        val userId = tokenProvider.getUserId()
        if (userId == null) {
            Log.i("ArcheryScore_Debug", "No hay userId, no se puede obtener el usuario")
            return null
        }

        Log.i("ArcheryScore_Debug", "Intentando obtener usuario por ID: $userId")
        return try {
            val user = api.getUserById(userId)
            Log.i("ArcheryScore_Debug", "Usuario obtenido: $user")
            user
        } catch (e: HttpException) {
            Log.e("ArcheryScore_Debug", "getCurrentUser failed: ${e.code()} ${e.response()?.errorBody()?.string()}", e)
            null
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "Exception fetching current user by id", e)
            null
        }
    }

    suspend fun updateUser(user: User): User? {
        val id = user.id ?: run {
            Log.w("ArcheryScore_Debug", "No se puede actualizar usuario sin id")
            return null
        }
        return try {
            // Enviar solo campos editables no vacíos (evita rechazos por campos vacíos)
            val request = UpdateUserRequest(
                username = user.username.takeIf { it.isNotBlank() },
                tel = user.tel.takeIf { it.isNotBlank() }
            )
            val response: Response<User> = api.updateUser(id.toString(), request)
            if (response.isSuccessful) {
                val updated = response.body()
                Log.d("ArcheryScore_Debug", "✅ Usuario actualizado en backend: $updated")
                updated
            } else {
                Log.w("ArcheryScore_Debug", "updateUser no fue exitoso: ${response.code()} ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "Exception updating user", e)
            null
        }
    }

    suspend fun changePassword(newPassword: String): Boolean {
        val userId = tokenProvider.getUserId()
        if (userId == null) {
            Log.w("ArcheryScore_Debug", "No hay userId para cambiar contraseña")
            return false
        }
        return try {
            val request = PasswordChangeRequest(password = newPassword)
            val response = api.updatedPassword(userId.toString(), request)
            if (!response.isSuccessful) {
                Log.w(
                    "ArcheryScore_Debug",
                    "changePassword no fue exitoso: ${response.code()} ${response.errorBody()?.string()}"
                )
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "Exception changing password", e)
            false
        }
    }

    fun logout() {
        tokenProvider.clearAll()
    }

    suspend fun clearUser() {
        // Lógica para borrar datos del usuario, por ejemplo, de una base de datos local
    }

    fun getToken(): String? = tokenProvider.getToken()
    fun getUserId(): Int? = tokenProvider.getUserId()

    suspend fun getUserById(id: Int?): User? {
        if (id == null) return null
        return try {
            api.getUserById(id)
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "Exception fetching user by id", e)
            null
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return try {
            api.getUserByMail(email)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createUserFromGoogle(username: String, email: String): User? {
        return try {
            Log.d("ArcheryScore_Debug", "🔍 Verificando si usuario Google existe: $email")

            // 1. PRIMERO verificar si el usuario YA EXISTE
            try {
                val existingUser = getUserByEmail(email)
                Log.d("ArcheryScore_Debug", "✅ Usuario EXISTE, no necesita registro: $email")

                // Devolver el usuario existente en lugar de crear uno nuevo
                return existingUser

            } catch (e: Exception) {
                if (e is HttpException && e.code() == 404) {
                    Log.d("ArcheryScore_Debug", "🆕 Usuario NUEVO, procediendo con registro: $email")

                    // 2. SOLO si no existe, CREARLO
                    val timestamp = System.currentTimeMillis()
                    val password = "Google${timestamp}Acc123"

                    val newUser = User(
                        username = username,
                        email = email,
                        password = password,
                        role = "client"
                    )

                    Log.d("ArcheryScore_Debug", "Intentando crear usuario Google: $newUser")

                    val response = api.newUser(newUser)
                    if (response.isSuccessful) {
                        Log.d("ArcheryScore_Debug", "✅ Usuario Google creado exitosamente")
                        // Obtener el usuario recién creado
                        api.getUserByMail(email)
                    } else {
                        Log.e("ArcheryScore_Debug", "❌ Error al crear usuario: ${response.errorBody()?.string()}")
                        null
                    }
                } else {
                    // Otro tipo de error
                    Log.e("ArcheryScore_Debug", "❌ Error al verificar usuario: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "❌ Excepción al crear usuario Google", e)
            null
        }
    }

    suspend fun saveGoogleSession(userId: Int): Boolean {
        return try {
            // Generar un token con formato similar al del backend
            val timestamp = System.currentTimeMillis()
            val token = "google_${userId}_${timestamp}"  // Token más significativo
            
            tokenProvider.saveUserId(userId)
            tokenProvider.saveToken(token)
            
            // Verificar que se guardó correctamente
            val savedToken = tokenProvider.getToken()
            val savedUserId = tokenProvider.getUserId()
            
            Log.d("ArcheryScore_Debug", "Google session saved - token: $savedToken, userId: $savedUserId")
            
            savedToken != null && savedUserId != null
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "Error al guardar sesión de Google", e)
            false
        }
    }

    suspend fun quickGoogleAuth(username: String, email: String): Result<User> {
        return try {
            Log.d("ArcheryScore_Debug", "⚡ Quick Google Auth para: $email")

            // Intentar obtener usuario existente con timeout
            val user = try {
                withTimeout(25000) { // ✅ 25 segundos timeout
                    api.getUserByMail(email).also {
                        Log.d("ArcheryScore_Debug", "✅ Usuario existente encontrado")
                    }
                }
            } catch (timeout: TimeoutCancellationException) {
                Log.e("ArcheryScore_Debug", "⏰ Timeout al verificar usuario existente")
                return Result.failure(Exception("El servidor no respondió a tiempo"))
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    // Usuario no existe - crear
                    Log.d("ArcheryScore_Debug", "🆕 Creando usuario rápido: $email")
                    val newUser = User(
                        username = username,
                        email = email,
                        password = "GoogleAuth123",
                        role = "client"
                    )

                    try {
                        withTimeout(25000) { // ✅ 25 segundos timeout
                            api.register(newUser)
                        }
                    } catch (timeout: TimeoutCancellationException) {
                        Log.e("ArcheryScore_Debug", "⏰ Timeout al crear usuario")
                        return Result.failure(Exception("Timeout al crear usuario"))
                    } catch (e: Exception) {
                        Log.e("ArcheryScore_Debug", "❌ Error al crear usuario: ${e.message}")
                        return Result.failure(e)
                    }
                } else {
                    // Otro error HTTP
                    Log.e("ArcheryScore_Debug", "❌ Error HTTP: ${e.code()} - ${e.message}")
                    return Result.failure(e)
                }
            }

            Result.success(user)
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "❌ Error en quickGoogleAuth: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun saveNotificationSetting(enabled: Boolean) {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, enabled).apply()
    }

    fun getNotificationSetting(): Boolean {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getBoolean(NOTIFICATIONS_ENABLED_KEY, true)
    }
}