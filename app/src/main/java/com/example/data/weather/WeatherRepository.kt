package com.example.data.weather

import com.example.data.local.WeatherCacheEntity
import com.example.data.local.WeatherDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.abs

class WeatherRepository(
    private val weatherDao: WeatherDao,
    private val weatherApi: WeatherApi
) {
    fun observeWeather(): Flow<WeatherCacheEntity?> = weatherDao.observeCache()

    suspend fun refreshWeatherIfNeeded(latitude: Double, longitude: Double) =
        withContext(Dispatchers.IO) {
            val existing = weatherDao.getCache()
            val now = System.currentTimeMillis()
            val locationChanged = existing == null ||
                abs(existing.latitude - latitude) > 0.1 ||
                abs(existing.longitude - longitude) > 0.1
            val stale = existing == null || (now - existing.fetchedAtEpochMs) > 30 * 60 * 1000L

            if (!locationChanged && !stale) return@withContext

            runCatching {
                val res = weatherApi.getCurrentWeather(latitude, longitude)
                val current = res.current ?: return@runCatching
                weatherDao.saveCache(
                    WeatherCacheEntity(
                        id = 1,
                        latitude = latitude,
                        longitude = longitude,
                        temperatureC = current.temperature2m,
                        weatherCode = current.weatherCode,
                        humidityPercent = current.humidity,
                        windSpeedKmh = current.windSpeed,
                        fetchedAtEpochMs = now
                    )
                )
            }
        }

    companion object {
        fun conditionLabel(code: Int, isArabic: Boolean): String = when (code) {
            0 -> if (isArabic) "صافٍ" else "Clear"
            1, 2, 3 -> if (isArabic) "غائم جزئياً" else "Partly Cloudy"
            45, 48 -> if (isArabic) "ضباب" else "Foggy"
            51, 53, 55, 61, 63, 65 -> if (isArabic) "ماطر" else "Rain"
            71, 73, 75 -> if (isArabic) "ثلوج" else "Snow"
            80, 81, 82 -> if (isArabic) "زخات مطر" else "Showers"
            95, 96, 99 -> if (isArabic) "عاصفة رعدية" else "Thunderstorm"
            else -> if (isArabic) "معتدل" else "Fair"
        }
    }
}
