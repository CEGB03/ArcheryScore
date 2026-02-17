package com.cegb03.archeryscore.data.model

data class WeatherSnapshot(
    val windSpeed: Double?,
    val windSpeedUnit: String?,
    val windDirectionDegrees: Int?,
    val skyCondition: String?
)
