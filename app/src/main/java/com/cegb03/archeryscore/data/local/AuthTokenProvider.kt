package com.cegb03.archeryscore.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val TOKEN_KEY = "auth_token"
        private const val USER_ID_KEY = "user_id"
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }

    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun clearToken() {
        prefs.edit().remove(TOKEN_KEY).apply()
    }

    fun getUserId(): Int? {
        return prefs.getInt(USER_ID_KEY, -1).takeIf { it != -1 }
    }

    fun saveUserId(id: Int) {
        prefs.edit().putInt(USER_ID_KEY, id).apply()
    }

    fun clearUserId() {
        prefs.edit().remove(USER_ID_KEY).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
