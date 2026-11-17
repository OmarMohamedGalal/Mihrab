package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.i18n.AppLanguage
import com.example.domain.prayer.AsrMethod
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.HighLatitudeRule
import com.example.domain.prayer.PrayerOffsets
import com.example.domain.prayer.PrayerType
import com.example.ui.theme.AppColorPalette
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mihrab_prefs")

enum class AdhanSoundStyle(val displayNameEn: String, val displayNameAr: String) {
    MAQAM_HIJAZ("Default Phone Notification Sound", "نغمة إشعارات الهاتف الافتراضية"),
    MAKKAH_HARAM("Phone Alarm Sound", "نغمة منبه الهاتف"),
    MADINAH_TONE("Phone Ringtone Sound", "نغمة رنين الهاتف"),
    SILENT("Silent Notification Only", "إشعار صامت فقط")
}

fun defaultDynamicColorEnabled(sdkInt: Int = android.os.Build.VERSION.SDK_INT): Boolean =
    sdkInt >= android.os.Build.VERSION_CODES.S

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = defaultDynamicColorEnabled(),
    val amoledDark: Boolean = false,
    val palette: AppColorPalette = AppColorPalette.TWILIGHT_PLUM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val calculationMethod: CalculationMethod = CalculationMethod.MWL,
    val asrMethod: AsrMethod = AsrMethod.STANDARD,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
    val offsets: PrayerOffsets = PrayerOffsets(),
    val use24Hour: Boolean = false,
    val hijriOffsetDays: Int = 0,
    val latitude: Double = 21.422487,
    val longitude: Double = 39.826206,
    val cityName: String = "Makkah",
    val countryName: String = "Saudi Arabia",
    val timezoneId: String = "Asia/Riyadh",
    val isGpsLocation: Boolean = false,
    val hasExplicitLocation: Boolean = false,
    val notifyFajr: Boolean = true,
    val notifyDhuhr: Boolean = true,
    val notifyAsr: Boolean = true,
    val notifyMaghrib: Boolean = true,
    val notifyIsha: Boolean = true,
    val adhanStyle: AdhanSoundStyle = AdhanSoundStyle.MAQAM_HIJAZ,
    val vibrationEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 10,
    val quranFontSizeSp: Float = 28f,
    val quranNightMode: Boolean = false,
    val showTranslation: Boolean = true,
    val quranMushafMode: Boolean = true,
    val quranSimpleScript: Boolean = false,
    val lastReadSurah: Int = 1,
    val lastReadAyah: Int = 1,
    val lastReadPage: Int = 1,
    val selectedReciterId: String = "ar.husary",
    val tasbeehDailyGoal: Int = 100,
    val quranWidgetVerseIndex: Int = 0
) {
    fun isPrayerNotificationEnabled(type: PrayerType): Boolean = when (type) {
        PrayerType.FAJR -> notifyFajr
        PrayerType.DHUHR -> notifyDhuhr
        PrayerType.ASR -> notifyAsr
        PrayerType.MAGHRIB -> notifyMaghrib
        PrayerType.ISHA -> notifyIsha
        PrayerType.SUNRISE -> false
    }
}

class UserPreferencesDataStore(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val AMOLED_DARK = booleanPreferencesKey("amoled_dark")
        val PALETTE = stringPreferencesKey("palette")
        val LANGUAGE = stringPreferencesKey("language")
        val CALC_METHOD = stringPreferencesKey("calc_method")
        val ASR_METHOD = stringPreferencesKey("asr_method")
        val HIGH_LAT = stringPreferencesKey("high_lat")
        val OFFSET_FAJR = intPreferencesKey("offset_fajr")
        val OFFSET_SUNRISE = intPreferencesKey("offset_sunrise")
        val OFFSET_DHUHR = intPreferencesKey("offset_dhuhr")
        val OFFSET_ASR = intPreferencesKey("offset_asr")
        val OFFSET_MAGHRIB = intPreferencesKey("offset_maghrib")
        val OFFSET_ISHA = intPreferencesKey("offset_isha")
        val USE_24H = booleanPreferencesKey("use_24h")
        val HIJRI_OFFSET = intPreferencesKey("hijri_offset")
        val LAT = doublePreferencesKey("lat")
        val LON = doublePreferencesKey("lon")
        val CITY = stringPreferencesKey("city")
        val COUNTRY = stringPreferencesKey("country")
        val TIMEZONE = stringPreferencesKey("timezone")
        val IS_GPS = booleanPreferencesKey("is_gps")
        val HAS_EXPLICIT_LOC = booleanPreferencesKey("has_explicit_loc")
        val NOTIFY_FAJR = booleanPreferencesKey("notify_fajr")
        val NOTIFY_DHUHR = booleanPreferencesKey("notify_dhuhr")
        val NOTIFY_ASR = booleanPreferencesKey("notify_asr")
        val NOTIFY_MAGHRIB = booleanPreferencesKey("notify_maghrib")
        val NOTIFY_ISHA = booleanPreferencesKey("notify_isha")
        val ADHAN_STYLE = stringPreferencesKey("adhan_style")
        val VIBRATION = booleanPreferencesKey("vibration")
        val REMINDER_MIN = intPreferencesKey("reminder_min")
        val QURAN_FONT = floatPreferencesKey("quran_font")
        val QURAN_NIGHT = booleanPreferencesKey("quran_night")
        val SHOW_TRANSLATION = booleanPreferencesKey("show_translation")
        val QURAN_MUSHAF_MODE = booleanPreferencesKey("quran_mushaf_mode")
        val QURAN_SIMPLE_SCRIPT = booleanPreferencesKey("quran_simple_script")
        val LAST_SURAH = intPreferencesKey("last_surah")
        val LAST_AYAH = intPreferencesKey("last_ayah")
        val LAST_PAGE = intPreferencesKey("last_read_page")
        val RECITER = stringPreferencesKey("reciter")
        val TASBEEH_GOAL = intPreferencesKey("tasbeeh_goal")
        val QURAN_WIDGET_VERSE_INDEX = intPreferencesKey("quran_widget_verse_index")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { p ->
        UserPreferences(
            themeMode = enumOrDefault(p[Keys.THEME_MODE], ThemeMode.SYSTEM),
            dynamicColor = p[Keys.DYNAMIC_COLOR] ?: defaultDynamicColorEnabled(),
            amoledDark = p[Keys.AMOLED_DARK] ?: false,
            palette = enumOrDefault(p[Keys.PALETTE], AppColorPalette.TWILIGHT_PLUM),
            language = enumOrDefault(p[Keys.LANGUAGE], AppLanguage.ENGLISH),
            calculationMethod = enumOrDefault(p[Keys.CALC_METHOD], CalculationMethod.MWL),
            asrMethod = enumOrDefault(p[Keys.ASR_METHOD], AsrMethod.STANDARD),
            highLatitudeRule = enumOrDefault(p[Keys.HIGH_LAT], HighLatitudeRule.MIDDLE_OF_NIGHT),
            offsets = PrayerOffsets(
                fajr = p[Keys.OFFSET_FAJR] ?: 0,
                sunrise = p[Keys.OFFSET_SUNRISE] ?: 0,
                dhuhr = p[Keys.OFFSET_DHUHR] ?: 0,
                asr = p[Keys.OFFSET_ASR] ?: 0,
                maghrib = p[Keys.OFFSET_MAGHRIB] ?: 0,
                isha = p[Keys.OFFSET_ISHA] ?: 0
            ),
            use24Hour = p[Keys.USE_24H] ?: false,
            hijriOffsetDays = p[Keys.HIJRI_OFFSET] ?: 0,
            latitude = p[Keys.LAT] ?: 21.422487,
            longitude = p[Keys.LON] ?: 39.826206,
            cityName = p[Keys.CITY] ?: "Makkah",
            countryName = p[Keys.COUNTRY] ?: "Saudi Arabia",
            timezoneId = p[Keys.TIMEZONE] ?: "Asia/Riyadh",
            isGpsLocation = p[Keys.IS_GPS] ?: false,
            hasExplicitLocation = p[Keys.HAS_EXPLICIT_LOC] ?: false,
            notifyFajr = p[Keys.NOTIFY_FAJR] ?: true,
            notifyDhuhr = p[Keys.NOTIFY_DHUHR] ?: true,
            notifyAsr = p[Keys.NOTIFY_ASR] ?: true,
            notifyMaghrib = p[Keys.NOTIFY_MAGHRIB] ?: true,
            notifyIsha = p[Keys.NOTIFY_ISHA] ?: true,
            adhanStyle = enumOrDefault(p[Keys.ADHAN_STYLE], AdhanSoundStyle.MAQAM_HIJAZ),
            vibrationEnabled = p[Keys.VIBRATION] ?: true,
            reminderMinutesBefore = p[Keys.REMINDER_MIN] ?: 10,
            quranFontSizeSp = p[Keys.QURAN_FONT] ?: 28f,
            quranNightMode = p[Keys.QURAN_NIGHT] ?: false,
            showTranslation = p[Keys.SHOW_TRANSLATION] ?: true,
            quranMushafMode = p[Keys.QURAN_MUSHAF_MODE] ?: true,
            quranSimpleScript = p[Keys.QURAN_SIMPLE_SCRIPT] ?: false,
            lastReadSurah = p[Keys.LAST_SURAH] ?: 1,
            lastReadAyah = p[Keys.LAST_AYAH] ?: 1,
            lastReadPage = (p[Keys.LAST_PAGE] ?: 1).coerceIn(1, 604),
            selectedReciterId = p[Keys.RECITER] ?: "ar.husary",
            tasbeehDailyGoal = p[Keys.TASBEEH_GOAL] ?: 100,
            quranWidgetVerseIndex = p[Keys.QURAN_WIDGET_VERSE_INDEX] ?: 0
        )
    }

    suspend fun updateThemeMode(mode: ThemeMode) = edit { it[Keys.THEME_MODE] = mode.name }
    suspend fun updateDynamicColor(enabled: Boolean) = edit { it[Keys.DYNAMIC_COLOR] = enabled }
    suspend fun updateAmoledDark(enabled: Boolean) = edit { it[Keys.AMOLED_DARK] = enabled }
    suspend fun updatePalette(palette: AppColorPalette) = edit { it[Keys.PALETTE] = palette.name }
    suspend fun updateLanguage(language: AppLanguage) = edit { it[Keys.LANGUAGE] = language.name }
    suspend fun updateCalculationMethod(method: CalculationMethod) = edit { it[Keys.CALC_METHOD] = method.name }
    suspend fun updateAsrMethod(method: AsrMethod) = edit { it[Keys.ASR_METHOD] = method.name }
    suspend fun updateHighLatitudeRule(rule: HighLatitudeRule) = edit { it[Keys.HIGH_LAT] = rule.name }
    suspend fun updateUse24Hour(use24h: Boolean) = edit { it[Keys.USE_24H] = use24h }
    suspend fun updateHijriOffset(days: Int) = edit { it[Keys.HIJRI_OFFSET] = days.coerceIn(-2, 2) }
    suspend fun updateAdhanStyle(style: AdhanSoundStyle) = edit { it[Keys.ADHAN_STYLE] = style.name }
    suspend fun updateVibration(enabled: Boolean) = edit { it[Keys.VIBRATION] = enabled }
    suspend fun updateReminderMinutes(minutes: Int) = edit { it[Keys.REMINDER_MIN] = minutes.coerceIn(0, 45) }
    suspend fun updateQuranFontSize(sizeSp: Float) = edit { it[Keys.QURAN_FONT] = sizeSp.coerceIn(20f, 44f) }
    suspend fun updateQuranNightMode(enabled: Boolean) = edit { it[Keys.QURAN_NIGHT] = enabled }
    suspend fun updateShowTranslation(show: Boolean) = edit { it[Keys.SHOW_TRANSLATION] = show }
    suspend fun updateQuranMushafMode(enabled: Boolean) = edit { it[Keys.QURAN_MUSHAF_MODE] = enabled }
    suspend fun updateQuranSimpleScript(simple: Boolean) = edit { it[Keys.QURAN_SIMPLE_SCRIPT] = simple }
    suspend fun updateReciter(reciterId: String) = edit { it[Keys.RECITER] = reciterId }
    suspend fun updateTasbeehGoal(goal: Int) = edit { it[Keys.TASBEEH_GOAL] = goal.coerceIn(10, 5000) }

    suspend fun cycleQuranWidgetVerse(verseCount: Int): Int {
        var nextIdx = 0
        edit { prefs ->
            val current = prefs[Keys.QURAN_WIDGET_VERSE_INDEX] ?: 0
            nextIdx = if (verseCount > 0) (current + 1) % verseCount else 0
            prefs[Keys.QURAN_WIDGET_VERSE_INDEX] = nextIdx
        }
        return nextIdx
    }

    suspend fun updateLastRead(surah: Int, ayah: Int) = edit {
        it[Keys.LAST_SURAH] = surah.coerceIn(1, 114)
        it[Keys.LAST_AYAH] = ayah.coerceAtLeast(1)
    }

    suspend fun updateLastReadPage(page: Int, surah: Int, ayah: Int = 1) = edit {
        it[Keys.LAST_PAGE] = page.coerceIn(1, 604)
        it[Keys.LAST_SURAH] = surah.coerceIn(1, 114)
        it[Keys.LAST_AYAH] = ayah.coerceAtLeast(1)
    }

    suspend fun updateOffset(type: PrayerType, minutes: Int) = edit {
        val clamped = minutes.coerceIn(-30, 30)
        when (type) {
            PrayerType.FAJR -> it[Keys.OFFSET_FAJR] = clamped
            PrayerType.SUNRISE -> it[Keys.OFFSET_SUNRISE] = clamped
            PrayerType.DHUHR -> it[Keys.OFFSET_DHUHR] = clamped
            PrayerType.ASR -> it[Keys.OFFSET_ASR] = clamped
            PrayerType.MAGHRIB -> it[Keys.OFFSET_MAGHRIB] = clamped
            PrayerType.ISHA -> it[Keys.OFFSET_ISHA] = clamped
        }
    }

    suspend fun updatePrayerNotification(type: PrayerType, enabled: Boolean) = edit {
        when (type) {
            PrayerType.FAJR -> it[Keys.NOTIFY_FAJR] = enabled
            PrayerType.DHUHR -> it[Keys.NOTIFY_DHUHR] = enabled
            PrayerType.ASR -> it[Keys.NOTIFY_ASR] = enabled
            PrayerType.MAGHRIB -> it[Keys.NOTIFY_MAGHRIB] = enabled
            PrayerType.ISHA -> it[Keys.NOTIFY_ISHA] = enabled
            PrayerType.SUNRISE -> Unit
        }
    }

    suspend fun updateLocation(
        latitude: Double,
        longitude: Double,
        cityName: String,
        countryName: String,
        timezoneId: String,
        isGps: Boolean
    ) = edit {
        it[Keys.LAT] = latitude
        it[Keys.LON] = longitude
        it[Keys.CITY] = cityName
        it[Keys.COUNTRY] = countryName
        it[Keys.TIMEZONE] = timezoneId
        it[Keys.IS_GPS] = isGps
        it[Keys.HAS_EXPLICIT_LOC] = true
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, default: T): T {
        return raw?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
    }
}
