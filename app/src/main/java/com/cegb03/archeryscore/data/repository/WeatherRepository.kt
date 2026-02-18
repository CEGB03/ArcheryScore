package com.cegb03.archeryscore.data.repository

import android.util.Log
import com.cegb03.archeryscore.data.model.WeatherSnapshot
import com.cegb03.archeryscore.data.remote.OpenMeteoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor() {
    private val service: OpenMeteoService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoService::class.java)
    }

    suspend fun getCurrentWeather(lat: Double, lon: Double): WeatherSnapshot? {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.getCurrentWeather(latitude = lat, longitude = lon)
                val weather = response.current_weather
                if (weather == null) {
                    null
                } else {
                    WeatherSnapshot(
                        windSpeed = weather.windspeed,
                        windSpeedUnit = response.current_weather_units?.windspeed,
                        windDirectionDegrees = weather.winddirection?.toInt(),
                        skyCondition = mapWeatherCodeToSky(weather.weathercode)
                    )
                }
            } catch (e: Exception) {
                Log.e("ArcheryScore_Debug", "🌦️ WeatherRepository - error", e)
                null
            }
        }
    }

    private fun mapWeatherCodeToSky(code: Int?): String? {
        return when (code) {
            0 -> "Soleado"
            1, 2 -> "Parcialmente nublado"
            3 -> "Nublado"
            45, 48 -> "Niebla"
            51, 53, 55, 56, 57 -> "Llovizna"
            61, 63, 65, 66, 67, 80, 81, 82 -> "Lluvia"
            71, 73, 75, 77 -> "Nieve"
            85, 86 -> "Nieve"
            95, 96, 99 -> "Tormenta"
            else -> null
        }
    }
}
