package com.cegb03.archeryscore.data.remote.supabase

import android.util.Log
import com.cegb03.archeryscore.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Cliente Singleton de Supabase para la app de Archery Score
 * 
 * Las credenciales se obtienen desde local.properties (GITIGNORED) mediante BuildConfig:
 * - SUPABASE_URL: URL del proyecto
 * - SUPABASE_ANON_KEY: Anon/Public key para cliente
 * 
 * No exponemos credenciales en el código fuente.
 */
object SupabaseClient {
    
    private const val TAG = "ArcheryScore_Debug"
    
    /**
     * Cliente Supabase inicializado con módulos necesarios
     */
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Auth)
            install(Realtime)
        }.also {
            Log.d(TAG, "✅ SupabaseClient inicializado")
            Log.d(TAG, "URL: ${BuildConfig.SUPABASE_URL}")
        }
    }
    
    /**
     * Obtiene el ID del usuario autenticado
     */
    fun currentUserId(): String? = client.auth.currentUserOrNull()?.id
    
    /**
     * Verifica si hay un usuario autenticado
     */
    fun isAuthenticated(): Boolean = client.auth.currentUserOrNull() != null
    
    /**
     * Obtiene el email del usuario autenticado
     */
    fun currentUserEmail(): String? = client.auth.currentUserOrNull()?.email
}
