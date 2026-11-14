package com.example.data.weather

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val current: CurrentWeatherDto?
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @param:Json(name = "temperature_2m") val temperature2m: Double,
    @param:Json(name = "weather_code") val weatherCode: Int,
    @param:Json(name = "relative_humidity_2m") val humidity: Int,
    @param:Json(name = "wind_speed_10m") val windSpeed: Double
)

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,weather_code,relative_humidity_2m,wind_speed_10m"
    ): OpenMeteoResponse
}
