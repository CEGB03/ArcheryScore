package com.cegb03.archeryscore.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object FatarcoRetrofitInstance {

    private const val BASE_URL = "https://arquerosonline.com.ar/"

    private val loggingInterceptor = Interceptor { chain ->
        val request = chain.request()
        Log.d("ArcheryScore_Debug", "🔗 Request: ${request.url}")
        
        val response = chain.proceed(request)
        val body = response.body?.string()
        
        Log.d("ArcheryScore_Debug", "📥 Response code: ${response.code}")
        Log.d("ArcheryScore_Debug", "📦 Response body size: ${body?.length} bytes")
        if (body != null && body.length < 1000) {
            Log.d("ArcheryScore_Debug", "📦 Response body: $body")
        }
        
        // Reconstruir el response porque ya leímos el body
        response.newBuilder()
            .body(okhttp3.ResponseBody.create(response.body?.contentType(), body ?: ""))
            .build()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36")
                .header("Referer", "https://arquerosonline.com.ar/arqueros")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: FatarcoService by lazy {
        retrofit.create(FatarcoService::class.java)
    }
}
