package com.cegb03.archeryscore.data.repository

import android.content.Context
import android.util.Log
import com.cegb03.archeryscore.data.model.User
import com.cegb03.archeryscore.data.remote.supabase.repository.SupabaseAuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val supabaseAuthRepo: SupabaseAuthRepository, // 🆕 NUEVO: Supabase
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
    }

    // ============================================================================
    // 🆕 NUEVOS MÉTODOS CON SUPABASE
    // ============================================================================
    
    /**
     * Login con Supabase (método principal)
     */
    suspend fun loginUser(email: String, password: String): Pair<Boolean, String?> {
        Log.i("ArcheryScore_Debug", "🔑 Iniciando login Supabase para email=$email")
        return try {
            val result = supabaseAuthRepo.signIn(email, password)

            if (result.isSuccess) {
                val profile = result.getOrNull()
                Log.i("ArcheryScore_Debug", "✅ Login exitoso con Supabase: ${profile?.username}")
                Pair(true, null)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                Log.e("ArcheryScore_Debug", "❌ Error en login Supabase: $error")
                Pair(false, error)
            }
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "❌ Excepción en login Supabase: ${e.message}", e)
            Pair(false, e.localizedMessage ?: "Error al iniciar sesión")
        }
    }
    
    /**
     * Registro con Supabase (método principal)
     */
    suspend fun registerUser(username: String, email: String, password: String): Pair<Boolean, String?> {
        Log.i("ArcheryScore_Debug", "🆕 Iniciando registro Supabase para email=$email")
        return try {
            val result = supabaseAuthRepo.signUp(
                email = email,
                password = password,
                username = username
            )

            if (result.isSuccess) {
                val profile = result.getOrNull()
                Log.i("ArcheryScore_Debug", "✅ Registro exitoso con Supabase: ${profile?.username}")
                Pair(true, null)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                Log.e("ArcheryScore_Debug", "❌ Error en registro Supabase: $error")
                Pair(false, error)
            }
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "❌ Excepción en registro Supabase: ${e.message}", e)
            Pair(false, e.localizedMessage ?: "Error al registrar")
        }
    }
    
    /**
     * Obtener usuario actual con Supabase
     */
    suspend fun getCurrentUser(): User? {
        return try {
            val profile = supabaseAuthRepo.getCurrentUserProfile()

            if (profile != null) {
                User(
                    id = null,
                    username = profile.username,
                    email = profile.email,
                    password = "",
                    tel = profile.tel ?: "",
                    role = profile.role.firstOrNull() ?: "arquero"
                )
            } else {
                Log.w("ArcheryScore_Debug", "No hay perfil en Supabase")
                null
            }
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "Error al obtener perfil de Supabase: ${e.message}", e)
            null
        }
    }
    
    /**
     * Actualizar usuario con Supabase
     */
    suspend fun updateUser(user: User): User? {
        return try {
            val result = supabaseAuthRepo.updateProfile(
                username = user.username.takeIf { it.isNotBlank() },
                tel = user.tel.takeIf { it.isNotBlank() }
            )

            if (result.isSuccess) {
                val profile = result.getOrNull()
                Log.d("ArcheryScore_Debug", "✅ Usuario actualizado en Supabase: ${profile?.username}")

                if (profile != null) {
                    User(
                        id = null,
                        username = profile.username,
                        email = profile.email,
                        password = "",
                        tel = profile.tel ?: "",
                        role = profile.role.firstOrNull() ?: "arquero"
                    )
                } else {
                    null
                }
            } else {
                Log.w("ArcheryScore_Debug", "Error al actualizar en Supabase: ${result.exceptionOrNull()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "Excepción al actualizar en Supabase", e)
            null
        }
    }
    
    /**
     * Cerrar sesión (Supabase + limpiar tokens locales)
     */
    suspend fun logout() {
        try {
            supabaseAuthRepo.signOut()
            Log.d("ArcheryScore_Debug", "✅ Sesión cerrada en Supabase")
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "Error al cerrar sesión en Supabase", e)
        }
    }

    fun isAuthenticated(): Boolean = supabaseAuthRepo.isAuthenticated()

    suspend fun changePassword(newPassword: String): Boolean {
        return try {
            val result = supabaseAuthRepo.updatePassword(newPassword)
            if (result.isSuccess) {
                true
            } else {
                Log.w("ArcheryScore_Debug", "changePassword no fue exitoso: ${result.exceptionOrNull()?.message}")
                false
            }
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "Exception changing password", e)
            false
        }
    }

    suspend fun clearUser() {
        // Lógica para borrar datos del usuario, por ejemplo, de una base de datos local
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