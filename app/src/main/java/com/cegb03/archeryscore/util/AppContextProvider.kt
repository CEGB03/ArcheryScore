package com.cegb03.archeryscore.util

import android.app.Application
import android.content.Context
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppContextProvider : Application() {
    
    override fun onCreate() {
        super.onCreate()
        Log.d("ArcheryScore_Debug", "🚀 AppContextProvider.onCreate() - Iniciando aplicación")
    }
    
    init {
        Log.d("ArcheryScore_Debug", "🔧 AppContextProvider.init - Configurando instancia")
        instance = this
    }

    companion object {
        private lateinit var instance: AppContextProvider

        fun getContext(): Context {
            return instance.applicationContext
        }
    }
}
