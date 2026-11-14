package com.example.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.AppContainer
import com.example.core.i18n.AppLanguage
import com.example.data.cities.CityOption
import com.example.data.local.WeatherCacheEntity
import com.example.data.preferences.AdhanSoundStyle
import com.example.data.preferences.UserPreferences
import com.example.domain.calendar.HijriCalendarEngine
import com.example.domain.calendar.HijriDate
import com.example.domain.calendar.UpcomingOccasion
import com.example.domain.prayer.AsrMethod
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.DailyPrayerTimes
import com.example.domain.prayer.HighLatitudeRule
import com.example.domain.prayer.NextPrayerInfo
import com.example.domain.prayer.PrayerTimeCalculator
import com.example.domain.prayer.PrayerType
import com.example.domain.qibla.QiblaCalculator
import com.example.notifications.PrayerAlarmScheduler
import com.example.sensors.CompassSensorState
import com.example.ui.theme.AppColorPalette
import com.example.ui.theme.ThemeMode
import com.example.widgets.PrayerGlanceWidget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

data class MainUiState(
    val prefs: UserPreferences = UserPreferences(),
    val now: ZonedDateTime = ZonedDateTime.now(),
    val dailyPrayerTimes: DailyPrayerTimes,
    val nextPrayerInfo: NextPrayerInfo,
    val hijriDate: HijriDate,
    val isJumuah: Boolean,
    val daysUntilRamadan: Long,
    val upcomingOccasions: List<UpcomingOccasion>,
    val completedPrayersToday: Set<PrayerType> = emptySet(),
    val streakDays: Int = 0,
    val weather: WeatherCacheEntity? = null,
    val compassState: CompassSensorState = CompassSensorState(),
    val qiblaBearing: Double = 0.0,
    val distanceToKaabaKm: Double = 0.0,
    val isAdhanPlaying: Boolean = false,
    val isDetectingGps: Boolean = false,
    val gpsMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val app: Application,
    private val container: AppContainer
) : AndroidViewModel(app) {

    private val isDetectingGps = MutableStateFlow(false)
    private val gpsMessage = MutableStateFlow<String?>(null)

    private val tickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000L)
        }
    }

    private val prefsFlow = container.preferencesDataStore.preferencesFlow

    private val compassFlow = prefsFlow
        .map { it.latitude to it.longitude }
        .distinctUntilChanged()
        .flatMapLatest { (lat, lon) ->
            container.qiblaSensorManager.observeCompass(lat, lon)
        }

    private val completedTodayFlow = prefsFlow
        .map { runCatching { ZoneId.of(it.timezoneId) }.getOrElse { ZoneId.systemDefault() } }
        .distinctUntilChanged()
        .flatMapLatest { zoneId ->
            container.prayerTrackerRepository.observeCompletedForDate(LocalDate.now(zoneId))
        }

    private val initialState: MainUiState = buildUiState(
        prefs = UserPreferences(),
        completedToday = emptySet(),
        streakDays = 0,
        weather = null,
        compass = CompassSensorState(),
        isAdhanPlaying = false,
        detectingGps = false,
        gpsMsg = null
    )

    val uiState: StateFlow<MainUiState> = combine(
        prefsFlow,
        tickerFlow,
        completedTodayFlow,
        container.prayerTrackerRepository.observeStreakDays(),
        container.weatherRepository.observeWeather(),
        compassFlow,
        container.adhanAudioManager.isPlaying,
        isDetectingGps,
        gpsMessage
    ) { values ->
        val prefs = values[0] as UserPreferences
        val completed = values[2] as Set<PrayerType>
        val streak = values[3] as Int
        val weather = values[4] as WeatherCacheEntity?
        val compass = values[5] as CompassSensorState
        val adhanPlaying = values[6] as Boolean
        val detecting = values[7] as Boolean
        val gpsMsg = values[8] as String?

        buildUiState(
            prefs = prefs,
            completedToday = completed,
            streakDays = streak,
            weather = weather,
            compass = compass,
            isAdhanPlaying = adhanPlaying,
            detectingGps = detecting,
            gpsMsg = gpsMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = initialState
    )

    init {
        // Reschedule alarms, update Glance widget, and refresh weather whenever key prefs change
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            prefsFlow.collect { prefs ->
                runCatching { PrayerAlarmScheduler.scheduleUpcomingPrayers(app, prefs) }
                runCatching { PrayerGlanceWidget.updateAllWidgets(app) }
                runCatching { container.weatherRepository.refreshWeatherIfNeeded(prefs.latitude, prefs.longitude) }
            }
        }
    }

    private fun buildUiState(
        prefs: UserPreferences,
        completedToday: Set<PrayerType>,
        streakDays: Int,
        weather: WeatherCacheEntity?,
        compass: CompassSensorState,
        isAdhanPlaying: Boolean,
        detectingGps: Boolean,
        gpsMsg: String?
    ): MainUiState {
        val zoneId = runCatching { ZoneId.of(prefs.timezoneId) }.getOrElse { ZoneId.systemDefault() }
        val now = ZonedDateTime.now(zoneId)
        val today = now.toLocalDate()
        val params = PrayerTimeCalculator.Params(
            latitude = prefs.latitude,
            longitude = prefs.longitude,
            method = prefs.calculationMethod,
            asrMethod = prefs.asrMethod,
            highLatitudeRule = prefs.highLatitudeRule,
            offsets = prefs.offsets,
            hijriAdjustmentDays = prefs.hijriOffsetDays
        )
        val todayTimes = PrayerTimeCalculator.calculate(today, zoneId, params)
        val tomorrowTimes = PrayerTimeCalculator.calculate(today.plusDays(1), zoneId, params)
        val yesterdayTimes = PrayerTimeCalculator.calculate(today.minusDays(1), zoneId, params)
        val next = PrayerTimeCalculator.resolveNextPrayer(now, todayTimes, tomorrowTimes, yesterdayTimes)

        val hijri = HijriCalendarEngine.fromGregorian(today, prefs.hijriOffsetDays)
        val isJumuah = HijriCalendarEngine.isJumuah(today)
        val daysRamadan = HijriCalendarEngine.daysUntilRamadan(today, prefs.hijriOffsetDays)
        val occasions = HijriCalendarEngine.upcomingOccasions(today, prefs.hijriOffsetDays)

        val qiblaBearing = QiblaCalculator.calculateQiblaBearing(prefs.latitude, prefs.longitude)
        val distanceKm = QiblaCalculator.distanceToKaabaKm(prefs.latitude, prefs.longitude)

        return MainUiState(
            prefs = prefs,
            now = now,
            dailyPrayerTimes = todayTimes,
            nextPrayerInfo = next,
            hijriDate = hijri,
            isJumuah = isJumuah,
            daysUntilRamadan = daysRamadan,
            upcomingOccasions = occasions,
            completedPrayersToday = completedToday,
            streakDays = streakDays,
            weather = weather,
            compassState = compass,
            qiblaBearing = qiblaBearing,
            distanceToKaabaKm = distanceKm,
            isAdhanPlaying = isAdhanPlaying,
            isDetectingGps = detectingGps,
            gpsMessage = gpsMsg
        )
    }

    fun calculateDayPrayers(date: LocalDate): DailyPrayerTimes {
        val prefs = uiState.value.prefs
        val zoneId = runCatching { ZoneId.of(prefs.timezoneId) }.getOrElse { ZoneId.systemDefault() }
        return PrayerTimeCalculator.calculate(
            date = date,
            zoneId = zoneId,
            params = PrayerTimeCalculator.Params(
                latitude = prefs.latitude,
                longitude = prefs.longitude,
                method = prefs.calculationMethod,
                asrMethod = prefs.asrMethod,
                highLatitudeRule = prefs.highLatitudeRule,
                offsets = prefs.offsets,
                hijriAdjustmentDays = prefs.hijriOffsetDays
            )
        )
    }

    fun calculateMonthPrayers(year: Int, month: Int): List<DailyPrayerTimes> {
        val prefs = uiState.value.prefs
        val zoneId = runCatching { ZoneId.of(prefs.timezoneId) }.getOrElse { ZoneId.systemDefault() }
        return PrayerTimeCalculator.calculateMonth(
            year = year,
            month = month,
            zoneId = zoneId,
            params = PrayerTimeCalculator.Params(
                latitude = prefs.latitude,
                longitude = prefs.longitude,
                method = prefs.calculationMethod,
                asrMethod = prefs.asrMethod,
                highLatitudeRule = prefs.highLatitudeRule,
                offsets = prefs.offsets,
                hijriAdjustmentDays = prefs.hijriOffsetDays
            )
        )
    }

    fun detectGpsLocation() {
        viewModelScope.launch {
            isDetectingGps.value = true
            gpsMessage.value = null
            val result = container.locationService.detectCurrentLocation()
            result.onSuccess { resolved ->
                container.preferencesDataStore.updateLocation(
                    latitude = resolved.latitude,
                    longitude = resolved.longitude,
                    cityName = resolved.cityName,
                    countryName = resolved.countryName,
                    timezoneId = resolved.timezoneId,
                    isGps = true
                )
            }.onFailure { err ->
                gpsMessage.value = err.localizedMessage ?: "GPS unavailable"
            }
            isDetectingGps.value = false
        }
    }

    fun clearGpsMessage() {
        gpsMessage.value = null
    }

    fun selectCity(city: CityOption, applySuggestedMethod: Boolean = true) {
        viewModelScope.launch {
            val isAr = uiState.value.prefs.language == AppLanguage.ARABIC
            container.preferencesDataStore.updateLocation(
                latitude = city.latitude,
                longitude = city.longitude,
                cityName = if (isAr) city.nameAr else city.nameEn,
                countryName = if (isAr) city.countryAr else city.countryEn,
                timezoneId = city.timezoneId,
                isGps = false
            )
            if (applySuggestedMethod) {
                container.preferencesDataStore.updateCalculationMethod(city.suggestedMethod)
            }
        }
    }

    fun togglePrayerCompleted(type: PrayerType) {
        viewModelScope.launch {
            val today = uiState.value.now.toLocalDate()
            val alreadyDone = uiState.value.completedPrayersToday.contains(type)
            container.prayerTrackerRepository.togglePrayerCompleted(today, type, !alreadyDone)
        }
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        container.preferencesDataStore.updateThemeMode(mode)
    }

    fun setAmoledDark(enabled: Boolean) = viewModelScope.launch {
        container.preferencesDataStore.updateAmoledDark(enabled)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        container.preferencesDataStore.updateDynamicColor(enabled)
    }

    fun setPalette(palette: AppColorPalette) = viewModelScope.launch {
        container.preferencesDataStore.updatePalette(palette)
    }

    fun setLanguage(language: AppLanguage) = viewModelScope.launch {
        container.preferencesDataStore.updateLanguage(language)
    }

    fun setCalculationMethod(method: CalculationMethod) = viewModelScope.launch {
        container.preferencesDataStore.updateCalculationMethod(method)
    }

    fun setAsrMethod(method: AsrMethod) = viewModelScope.launch {
        container.preferencesDataStore.updateAsrMethod(method)
    }

    fun setHighLatitudeRule(rule: HighLatitudeRule) = viewModelScope.launch {
        container.preferencesDataStore.updateHighLatitudeRule(rule)
    }

    fun setOffset(type: PrayerType, minutes: Int) = viewModelScope.launch {
        container.preferencesDataStore.updateOffset(type, minutes)
    }

    fun setUse24Hour(use24h: Boolean) = viewModelScope.launch {
        container.preferencesDataStore.updateUse24Hour(use24h)
    }

    fun setHijriOffset(days: Int) = viewModelScope.launch {
        container.preferencesDataStore.updateHijriOffset(days)
    }

    fun setPrayerNotification(type: PrayerType, enabled: Boolean) = viewModelScope.launch {
        container.preferencesDataStore.updatePrayerNotification(type, enabled)
    }

    fun setAdhanStyle(style: AdhanSoundStyle) = viewModelScope.launch {
        container.preferencesDataStore.updateAdhanStyle(style)
    }

    fun toggleAdhanPreview() {
        if (uiState.value.isAdhanPlaying) {
            container.adhanAudioManager.stop()
        } else {
            val prefs = uiState.value.prefs
            val style = prefs.adhanStyle
            val playStyle = if (style == AdhanSoundStyle.SILENT) AdhanSoundStyle.MAQAM_HIJAZ else style
            container.adhanAudioManager.play(playStyle)
            val isArabic = prefs.language == com.example.core.i18n.AppLanguage.ARABIC
            val nextInfo = uiState.value.nextPrayerInfo
            val nextName = (if (isArabic) com.example.core.i18n.ArabicStrings else com.example.core.i18n.EnglishStrings)
                .prayerName(nextInfo.nextPrayer)
            val nextTimeFormatted = com.example.core.util.FormattingUtils.formatPrayerTime(
                nextInfo.nextTime,
                prefs.use24Hour,
                isArabic
            )
            com.example.notifications.PrayerNotificationHelper.showPrayerNotification(
                context = app,
                notificationId = 9901,
                title = if (isArabic) "حان الآن موعد صلاة $nextName" else "$nextName Prayer Time",
                body = "${prefs.cityName} • $nextTimeFormatted",
                isSilent = style == AdhanSoundStyle.SILENT,
                isReminder = false,
                vibrate = prefs.vibrationEnabled,
                soundStyle = style
            )
            com.example.notifications.PrayerNotificationHelper.updateLockScreenPrayerNotification(app, prefs)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) = viewModelScope.launch {
        container.preferencesDataStore.updateVibration(enabled)
    }

    fun setReminderMinutes(minutes: Int) = viewModelScope.launch {
        container.preferencesDataStore.updateReminderMinutes(minutes)
    }

    class Factory(
        private val app: Application,
        private val container: AppContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(app, container) as T
        }
    }
}
