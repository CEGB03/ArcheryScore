package com.cegb03.archeryscore.data.remote.supabase.repository

import android.util.Log
import com.cegb03.archeryscore.data.remote.supabase.SupabaseClient
import com.cegb03.archeryscore.data.model.FatarcoArcherData
import com.cegb03.archeryscore.data.remote.supabase.models.FatarcoProfile
import com.cegb03.archeryscore.data.remote.supabase.models.UpdateProfileRequest
import com.cegb03.archeryscore.data.remote.supabase.models.UserProfile
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para autenticación y gestión de perfiles de usuario en Supabase
 * 
 * Usa Supabase Auth (GoTrue) para autenticación y Postgrest para perfiles
 */
@Singleton
class SupabaseAuthRepository @Inject constructor() {
    
    private val client = SupabaseClient.client
    private val auth = client.auth
    private val TAG = "ArcheryScore_Debug"
    
    // ============================================================================
    // AUTENTICACIÓN
    // ============================================================================
    
    /**
     * Registrar nuevo usuario con email y contraseña
     * Crea usuario en auth.users Y perfil en user_profiles
     * 
     * @param email Email del usuario
     * @param password Contraseña (mínimo 6 caracteres)
     * @param username Nombre de usuario para el perfil
     * @param tel Teléfono opcional
     * @param documento DNI/CI para verificación FATARCO (opcional)
     * @param roles Lista de roles del usuario (por defecto solo 'arquero')
     * @return Pair<Boolean, String?> - (éxito, mensaje de error si hay)
     */
    suspend fun signUp(
        email: String,
        password: String,
        username: String,
        tel: String? = null,
        documento: String? = null,
        roles: List<String> = listOf("arquero")
    ): Result<UserProfile> {
        return try {
            Log.d(TAG, "🆕 Iniciando registro para: $email")
            
            // 1. Crear usuario en Supabase Auth
            try {
                auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
            } catch (signUpException: Exception) {
                // Si hay error en signUp (como rate limit 429), lanzar inmediatamente
                val errorMsg = signUpException.message ?: "Error en signup"
                Log.e(TAG, "❌ Error HTTP en signUpWith: $errorMsg")
                throw Exception(errorMsg)  // Re-lanzar para que el catch externo lo maneje
            }
            
            // 2. Obtener el ID del usuario recién creado
            val userId = auth.currentUserOrNull()?.id
            
            if (userId == null) {
                Log.e(TAG, "❌ Error: No se pudo obtener userId después del registro")
                // Este caso ocurre cuando email confirmation está habilitada
                throw Exception("email rate limit exceeded - No se pudo crear el usuario")
            }
            
            if (auth.currentUserOrNull() == null) {
                Log.w(TAG, "⚠️ Registro creado pero sin sesión activa (email confirmation habilitada)")
                throw Exception("Registro creado, pero falta confirmar el email")
            }
            
            Log.d(TAG, "✅ Usuario creado en Auth con ID: $userId")
            
            // 3. Crear perfil en user_profiles
            val profile = UserProfile(
                id = userId,
                username = username,
                email = email,
                tel = tel,
                documento = documento,
                role = roles
            )
            
            Log.d(TAG, "💾 Insertando perfil en user_profiles...")
            try {
                client.from("user_profiles").insert(profile)
                Log.d(TAG, "✅ Perfil insertado correctamente")
                
                // Pequeno delay para que Supabase procese la insercion
                kotlinx.coroutines.delay(500)
                
                // Verificar que realmente se insertó
                Log.d(TAG, "🔍 Verificando inserción del perfil...")
                val insertedProfile = getProfile(userId)
                if (insertedProfile != null) {
                    Log.d(TAG, "✅ Verificación exitosa: perfil encontrado en BD")
                } else {
                    Log.w(TAG, "⚠️ Perfil no encontrado después de insertar (posible problema RLS)")
                }
            } catch (insertException: Exception) {
                Log.e(TAG, "❌ Error al insertar perfil: ${insertException.message}", insertException)
                // Continuar de todos modos, el usuario ya fue creado en Auth
            }
            
            Log.d(TAG, "✅ Registro completado exitosamente para: $email")
            
            Result.success(profile)
            
        } catch (e: Exception) {
            // Capturar el mensaje real del error HTTP (incluye rate limit)
            val errorMsg = e.message ?: "Error desconocido"
            Log.e(TAG, "❌ Error en signUp: $errorMsg", e)
            
            // Pasar el mensaje original para que mapAuthError pueda detectar "rate limit"
            Result.failure(Exception(errorMsg))
        }
    }
    
    /**
     * Iniciar sesión con email y contraseña
     * 
     * @param email Email del usuario
     * @param password Contraseña
     * @return Pair<Boolean, String?> - (éxito, mensaje de error si hay)
     */
    suspend fun signIn(email: String, password: String): Result<UserProfile> {
        return try {
            Log.d(TAG, "🔑 Iniciando sesión para: $email")
            
            // 1. Autenticar con Supabase Auth
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            // 2. Obtener perfil del usuario
            val userId = auth.currentUserOrNull()?.id
            
            if (userId == null) {
                Log.e(TAG, "❌ Error: No se pudo obtener userId después del login")
                return Result.failure(Exception("Error al obtener sesión"))
            }
            
            Log.d(TAG, "✅ Autenticación exitosa, obteniendo perfil...")
            
            // 3. Obtener perfil desde user_profiles
            val profile = getProfile(userId)
            
            if (profile != null) {
                Log.d(TAG, "✅ Login completado para: ${profile.username}")
                Result.success(profile)
            } else {
                Log.e(TAG, "❌ Error: Usuario autenticado pero sin perfil")
                Result.failure(Exception("Perfil no encontrado"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en signIn: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Cerrar sesión del usuario actual
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            Log.d(TAG, "👋 Cerrando sesión...")
            auth.signOut()
            Log.d(TAG, "✅ Sesión cerrada exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al cerrar sesión: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Verificar si hay un usuario autenticado
     */
    fun isAuthenticated(): Boolean {
        val authenticated = auth.currentUserOrNull() != null
        Log.d(TAG, "🔍 Usuario autenticado: $authenticated")
        return authenticated
    }
    
    /**
     * Obtener el ID del usuario actual
     */
    fun getCurrentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }
    
    /**
     * Obtener el email del usuario actual
     */
    fun getCurrentUserEmail(): String? {
        return auth.currentUserOrNull()?.email
    }
    
    // ============================================================================
    // GESTIÓN DE PERFILES
    // ============================================================================
    
    /**
     * Obtener perfil de usuario por ID
     * 
     * @param userId ID del usuario (UUID)
     * @return UserProfile o null si no existe
     */
    suspend fun getProfile(userId: String): UserProfile? {
        return try {
            Log.d(TAG, "📋 Obteniendo perfil para userId: $userId")
            
            val results = client.from("user_profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<UserProfile>()
            
            val profile = results.firstOrNull()
            if (profile != null) {
                Log.d(TAG, "✅ Perfil obtenido: ${profile.username}")
            } else {
                Log.w(TAG, "⚠️ No se encontró perfil para userId: $userId")
            }
            
            profile
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener perfil: ${e.message}", e)
            null
        }
    }
    
    /**
     * Obtener perfil del usuario actual
     */
    suspend fun getCurrentUserProfile(): UserProfile? {
        val userId = getCurrentUserId() ?: return null
        return getProfile(userId)
    }
    
    /**
     * Actualizar perfil del usuario actual
     * 
     * @param username Nuevo nombre de usuario (opcional)
     * @param tel Nuevo teléfono (opcional)
     * @param role Nuevo rol (opcional)
     * @return UserProfile actualizado o null si falla
     */
    suspend fun updateProfile(
        username: String? = null,
        tel: String? = null,
        documento: String? = null,
        club: String? = null,
        fechaNacimiento: String? = null,
        roles: List<String>? = null
    ): Result<UserProfile> {
        return try {
            val userId = getCurrentUserId()
            
            if (userId == null) {
                Log.e(TAG, "❌ Error: No hay usuario autenticado")
                return Result.failure(Exception("No hay usuario autenticado"))
            }
            
            Log.d(TAG, "📝 Actualizando perfil para userId: $userId")
            
            // Construir objeto de actualización solo con campos no nulos
            val updateRequest = UpdateProfileRequest(
                username = username,
                tel = tel,
                documento = documento,
                club = club,
                fechaNacimiento = fechaNacimiento,
                role = roles
            )
            
            // Actualizar en Supabase
            client.from("user_profiles")
                .update(updateRequest) {
                    filter {
                        eq("id", userId)
                    }
                }
            
            // Obtener perfil actualizado
            val updatedProfile = getProfile(userId)
            
            if (updatedProfile != null) {
                Log.d(TAG, "✅ Perfil actualizado exitosamente")
                Result.success(updatedProfile)
            } else {
                Log.e(TAG, "❌ Error: No se pudo obtener perfil actualizado")
                Result.failure(Exception("Error al actualizar perfil"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al actualizar perfil: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Guardar datos completos de FATARCO en tabla dedicada.
     */
    suspend fun upsertFatarcoProfile(data: FatarcoArcherData): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.e(TAG, "❌ Error: No hay usuario autenticado para FATARCO")
                return Result.failure(Exception("No hay usuario autenticado"))
            }

            val profile = FatarcoProfile(
                userId = userId,
                dni = data.dni,
                nombre = data.nombre,
                fechaNacimiento = data.fechaNacimiento,
                club = data.club,
                roles = data.estados.distinct()
            )

            client.from("fatarco_profiles").upsert(profile)
            Log.d(TAG, "✅ FATARCO profile guardado/actualizado")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error guardando FATARCO profile: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Buscar usuarios por email (para compartir entrenamientos)
     * 
     * @param email Email a buscar
     * @return Lista de perfiles que coinciden
     */
    suspend fun searchUsersByEmail(email: String): List<UserProfile> {
        return try {
            Log.d(TAG, "🔍 Buscando usuarios por email: $email")
            
            val results = client.from("user_profiles")
                .select {
                    filter {
                        ilike("email", "%$email%")
                    }
                }
                .decodeList<UserProfile>()
            
            Log.d(TAG, "✅ Encontrados ${results.size} usuarios")
            results
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al buscar usuarios: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Buscar usuarios por nombre de usuario
     * 
     * @param username Nombre de usuario a buscar
     * @return Lista de perfiles que coinciden
     */
    suspend fun searchUsersByUsername(username: String): List<UserProfile> {
        return try {
            Log.d(TAG, "🔍 Buscando usuarios por username: $username")
            
            val results = client.from("user_profiles")
                .select {
                    filter {
                        ilike("username", "%$username%")
                    }
                }
                .decodeList<UserProfile>()
            
            Log.d(TAG, "✅ Encontrados ${results.size} usuarios")
            results
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al buscar usuarios: ${e.message}", e)
            emptyList()
        }
    }
    
    // ============================================================================
    // OBSERVADORES
    // ============================================================================
    
    /**
     * Flow que emite cambios en el estado de autenticación
     * 
     * @return Flow<Boolean> - true si está autenticado, false si no
     */
    fun observeAuthState(): Flow<Boolean> = flow {
        // Emitir estado actual
        emit(isAuthenticated())
        
        // TODO: Implementar listener de cambios de auth cuando Supabase-kt lo soporte
        // Por ahora, solo emite el estado actual una vez
    }
    
    /**
     * Flow que emite el perfil del usuario actual
     * 
     * @return Flow<UserProfile?> - perfil o null si no está autenticado
     */
    fun observeCurrentUserProfile(): Flow<UserProfile?> = flow {
        val profile = getCurrentUserProfile()
        emit(profile)
        
        // TODO: Implementar listener de cambios en el perfil
    }
    
    // ============================================================================
    // UTILIDADES
    // ============================================================================
    
    /**
     * Cambiar contraseña del usuario actual
     * 
     * @param newPassword Nueva contraseña
     * @return Result indicando éxito o error
     */
    suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            Log.d(TAG, "🔐 Actualizando contraseña...")
            
            auth.updateUser {
                password = newPassword
            }
            
            Log.d(TAG, "✅ Contraseña actualizada exitosamente")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al actualizar contraseña: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Solicitar recuperación de contraseña por email
     * 
     * @param email Email del usuario
     * @return Result indicando éxito o error
     */
    suspend fun requestPasswordReset(email: String): Result<Unit> {
        return try {
            Log.d(TAG, "📧 Enviando email de recuperación a: $email")
            
            auth.resetPasswordForEmail(email)
            
            Log.d(TAG, "✅ Email de recuperación enviado")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al solicitar recuperación: ${e.message}", e)
            Result.failure(e)
        }
    }
}
