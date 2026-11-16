package com.example.core

import android.content.Context
import androidx.room.Room
import com.example.data.azkar.AzkarRepository
import com.example.data.local.AppDatabase
import com.example.data.prayer.PrayerTrackerRepository
import com.example.data.preferences.UserPreferencesDataStore
import com.example.data.quran.QuranRemoteApi
import com.example.data.quran.QuranRepository
import com.example.data.weather.WeatherApi
import com.example.data.weather.WeatherRepository
import com.example.sensors.QiblaSensorManager
import com.example.services.AdhanAudioManager
import com.example.services.LocationService
import com.example.services.QuranAudioManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    val appContext: Context = context.applicationContext

    val database: AppDatabase by lazy {
        Room.databaseBuilder(appContext, AppDatabase::class.java, "mihrab_db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val quranApi: QuranRemoteApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.alquran.cloud/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(QuranRemoteApi::class.java)
    }

    private val weatherApi: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WeatherApi::class.java)
    }

    val preferencesDataStore: UserPreferencesDataStore by lazy {
        UserPreferencesDataStore(appContext)
    }

    val quranRepository: QuranRepository by lazy {
        QuranRepository(database.quranDao(), database.bookmarkDao(), quranApi)
    }

    val azkarRepository: AzkarRepository by lazy {
        AzkarRepository(database.dhikrDao())
    }

    val weatherRepository: WeatherRepository by lazy {
        WeatherRepository(database.weatherDao(), weatherApi)
    }

    val prayerTrackerRepository: PrayerTrackerRepository by lazy {
        PrayerTrackerRepository(database.prayerLogDao())
    }

    val qiblaSensorManager: QiblaSensorManager by lazy {
        QiblaSensorManager(appContext)
    }

    val locationService: LocationService by lazy {
        LocationService(appContext)
    }

    val adhanAudioManager: AdhanAudioManager by lazy {
        AdhanAudioManager(appContext)
    }

    val quranAudioManager: QuranAudioManager by lazy {
        QuranAudioManager(appContext, okHttpClient)
    }
}
