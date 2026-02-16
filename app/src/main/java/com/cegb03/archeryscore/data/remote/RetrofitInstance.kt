package com.cegb03.archeryscore.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.cegb03.archeryscore.util.AppContextProvider
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "https://web2iua-back.onrender.com/tecno/"

    // Interceptor que agrega el token a cada request si existe
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        val context = AppContextProvider.getContext()
        val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)

        token?.let {
            builder.header("Authorization", "Bearer $it")
        }

        val newRequest = builder.build()
        chain.proceed(newRequest)
    }

    // ✅ AGREGAR TIMEOUTS aquí
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)  // ✅ Timeout para conectar
        .readTimeout(10, TimeUnit.SECONDS)     // ✅ Timeout para leer respuesta
        .writeTimeout(10, TimeUnit.SECONDS)    // ✅ Timeout para enviar datos
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}