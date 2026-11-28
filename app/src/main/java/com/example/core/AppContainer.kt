package com.example.core

import android.content.Context
import androidx.room.Room
import com.example.data.azkar.AzkarRepository
import com.example.data.local.AppDatabase
import com.example.data.prayer.PrayerTrackerRepository
import com.example.data.preferences.UserPreferencesDataStore
import com.example.data.quran.BundledMedinaMushafLoader
import com.example.data.quran.QuranComLineRepository
import com.example.data.quran.QuranComTafsirApi
import com.example.data.quran.QuranComV4Api
import com.example.data.quran.QuranFlashPageRepository
import com.example.data.quran.QuranRemoteApi
import com.example.data.quran.QuranRepository
import com.example.data.quran.QuranTafseerMirrorApi
import com.example.data.quran.QuranTafsirRepository
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

    private val quranComV4Retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.quran.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private val quranComV4Api: QuranComV4Api by lazy {
        quranComV4Retrofit.create(QuranComV4Api::class.java)
    }

    private val quranComTafsirApi: QuranComTafsirApi by lazy {
        quranComV4Retrofit.create(QuranComTafsirApi::class.java)
    }

    private val quranTafseerMirrorApi: QuranTafseerMirrorApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.quran-tafseer.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(QuranTafseerMirrorApi::class.java)
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

    val bundledMedinaMushafLoader: BundledMedinaMushafLoader by lazy {
        BundledMedinaMushafLoader(appContext)
    }

    val quranComLineRepository: QuranComLineRepository by lazy {
        QuranComLineRepository(appContext, quranComV4Api, moshi, bundledMedinaMushafLoader)
    }

    val quranTafsirRepository: QuranTafsirRepository by lazy {
        QuranTafsirRepository(appContext, quranComTafsirApi, quranTafseerMirrorApi)
    }

    val quranFlashPageRepository: QuranFlashPageRepository by lazy {
        QuranFlashPageRepository(appContext, okHttpClient)
    }

    val quranRepository: QuranRepository by lazy {
        QuranRepository(
            quranDao = database.quranDao(),
            bookmarkDao = database.bookmarkDao(),
            remoteApi = quranApi,
            quranComLineRepository = quranComLineRepository,
            bundledLoader = bundledMedinaMushafLoader
        )
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
