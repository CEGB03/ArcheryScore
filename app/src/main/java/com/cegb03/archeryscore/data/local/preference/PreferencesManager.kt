package com.cegb03.archeryscore.data.local.preference

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> 
            val value = preferences[NOTIFICATIONS_ENABLED] ?: true
            Log.d("DebugDev", "📍 notificationsEnabledFlow emitiendo: $value")
            value
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        Log.d("DebugDev", "📝 setNotificationsEnabled: $enabled")
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
            Log.d("DebugDev", "✅ Guardado notifications_enabled=$enabled en DataStore")
        }
    }

    val biometricEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> 
            val value = preferences[BIOMETRIC_ENABLED] ?: true
            Log.d("DebugDev", "📍 biometricEnabledFlow emitiendo: $value")
            value
        }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        Log.d("DebugDev", "📝 setBiometricEnabled: $enabled")
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED] = enabled
            Log.d("DebugDev", "✅ Guardado biometric_enabled=$enabled en DataStore")
        }
    }

    suspend fun clearAll() {
        Log.d("DebugDev", "🧹 clearAll() llamado")
        context.dataStore.edit { preferences ->
            preferences.clear()
            Log.d("DebugDev", "✅ Todas las preferencias eliminadas del DataStore")
        }
    }
}
