package com.cegb03.archeryscore.data.remote.supabase

import android.util.Log
import com.cegb03.archeryscore.BuildConfig

/**
 * Cliente Singleton de Supabase para la app de Archery Score
 * 
 * Las credenciales se obtienen desde local.properties (GITIGNORED) mediante BuildConfig:
 * - SUPABASE_URL: URL del proyecto
 * - SUPABASE_ANON_KEY: Anon/Public key para cliente
 * 
 * No exponemos credenciales en el código fuente.
 * 
 * ⚠️ IMPLEMENTACIÓN TEMPORAL:
 * La integración de Supabase-kt está siendo investigada debido a problemas de disponibilidad 
 * en repositorios Maven. Actualmente usa Firebase Auth y Retrofit para el backend.
 * 
 * PRÓXIMOS PASOS:
 * 1. Resolver la disponibilidad de supabase-kt en un repositorio Maven público
 * 2. Migrar gradualmente de Firebase Auth a Supabase GoTrue
 * 3. Reemplazar Retrofit con Supabase Postgrest para queries
 */
object SupabaseClient {
    
    private const val TAG = "ArcheryScore_Debug"
    
    /**
     * Credenciales de Supabase desde BuildConfig
     */
    val supabaseUrl: String
        get() = BuildConfig.SUPABASE_URL
    
    val supabaseKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY
    
    init {
        Log.d(TAG, "SupabaseClient inicializado")
        Log.d(TAG, "URL: ${BuildConfig.SUPABASE_URL}")
    }
    
    /**
     * Placeholder para obtener el ID del usuario
     * TODO: Implementar con Supabase GoTrue cuando esté disponible
     */
    fun currentUserId(): String? {
        // Temporalmente retorna null - será implementado con Supabase GoTrue
        return null
    }
    
    /**
     * Placeholder para verificar autenticación
     * TODO: Implementar con Supabase GoTrue cuando esté disponible
     */
    fun isAuthenticated(): Boolean {
        // Temporalmente retorna false - será implementado con Supabase GoTrue
        return false
    }
    
    /**
     * Placeholder para obtener email del usuario
     * TODO: Implementar con Supabase GoTrue cuando esté disponible
     */
    fun currentUserEmail(): String? {
        // Temporalmente retorna null - será implementado con Supabase GoTrue
        return null
    }
}
