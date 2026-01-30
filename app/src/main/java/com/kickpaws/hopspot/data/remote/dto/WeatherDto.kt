package com.kickpaws.hopspot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WeatherDto(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("current_weather")
    val currentWeather: CurrentWeatherDto
)

data class CurrentWeatherDto(
    val temperature: Double,
    val windspeed: Double,
    val winddirection: Int,
    val weathercode: Int,
    val time: String
)