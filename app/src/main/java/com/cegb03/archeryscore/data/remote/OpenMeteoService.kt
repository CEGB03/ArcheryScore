package com.cegb03.archeryscore.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoService {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): OpenMeteoResponse
}

data class OpenMeteoResponse(
    val current_weather: OpenMeteoCurrentWeather?,
    val current_weather_units: OpenMeteoCurrentWeatherUnits?
)

data class OpenMeteoCurrentWeather(
    val temperature: Double?,
    val windspeed: Double?,
    val winddirection: Double?,
    val weathercode: Int?,
    val time: String?
)

data class OpenMeteoCurrentWeatherUnits(
    val windspeed: String?
)
