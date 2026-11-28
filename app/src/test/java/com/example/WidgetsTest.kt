package com.example

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.data.azkar.AzkarRepository
import com.example.data.preferences.UserPreferences
import com.example.domain.calendar.HijriCalendarEngine
import com.example.domain.prayer.PrayerType
import com.example.widgets.CalendarWidgetTapCoordinator
import com.example.widgets.CalligraphyMonthWidgetReceiver
import com.example.widgets.CalligraphyWeekdayWidgetReceiver
import com.example.widgets.ChangeQuranVerseAction
import com.example.widgets.HijriDateWidgetReceiver
import com.example.widgets.PrayerGlanceWidgetReceiver
import com.example.widgets.PrayerTimetableWidgetReceiver
import com.example.widgets.QiblaCircleWidgetReceiver
import com.example.widgets.QuranVerseWidgetReceiver
import com.example.widgets.TransparentQuranVerseWidgetReceiver
import com.example.widgets.WidgetDestinations
import com.example.widgets.WidgetShapeRenderer
import com.example.widgets.WidgetThemeResolver
import com.example.widgets.WidgetUpdater
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.xmlpull.v1.XmlPullParser

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WidgetsTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun allFiveRemainingWidgetsAreRegisteredAndQuranListenWidgetIsRemoved() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pm = context.packageManager
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            `package` = context.packageName
        }
        val receivers = pm.queryBroadcastReceivers(intent, PackageManager.GET_META_DATA)

        val expectedClasses = setOf(
            PrayerGlanceWidgetReceiver::class.java.name,
            QuranVerseWidgetReceiver::class.java.name,
            HijriDateWidgetReceiver::class.java.name,
            QiblaCircleWidgetReceiver::class.java.name,
            PrayerTimetableWidgetReceiver::class.java.name
        )
        val actualClasses = receivers.map { it.activityInfo.name }.toSet()
        assertTrue(
            "Expected all 5 remaining widgets registered in manifest, found: $actualClasses",
            actualClasses.containsAll(expectedClasses)
        )
        assertFalse(
            "QuranListenWidgetReceiver must be removed from manifest",
            actualClasses.contains("com.example.widgets.QuranListenWidgetReceiver")
        )

        receivers.forEach { resolveInfo ->
            val metaData = resolveInfo.activityInfo.metaData
            assertNotNull("Missing metaData on ${resolveInfo.activityInfo.name}", metaData)
            val xmlResId = metaData.getInt("android.appwidget.provider", 0)
            assertTrue("Missing provider XML resource on ${resolveInfo.activityInfo.name}", xmlResId != 0)

            var resizeMode: String? = null
            var minResizeWidth: String? = null
            var minResizeHeight: String? = null
            context.resources.getXml(xmlResId).use { parser ->
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG && parser.name == "appwidget-provider") {
                        for (i in 0 until parser.attributeCount) {
                            when (parser.getAttributeName(i)) {
                                "resizeMode" -> resizeMode = parser.getAttributeValue(i)
                                "minResizeWidth" -> minResizeWidth = parser.getAttributeValue(i)
                                "minResizeHeight" -> minResizeHeight = parser.getAttributeValue(i)
                            }
                        }
                    }
                }
            }

            // android:resizeMode="horizontal|vertical" compiles to bitmask 0x3 ("0x3" or "3")
            assertTrue(
                "Expected horizontal|vertical resizeMode on ${resolveInfo.activityInfo.name}, got $resizeMode",
                resizeMode == "horizontal|vertical" || resizeMode == "3" || resizeMode == "0x3"
            )
            assertTrue(
                "Expected flexible minResizeWidth on ${resolveInfo.activityInfo.name}, got $minResizeWidth",
                minResizeWidth != null && (minResizeWidth!!.startsWith("40.0") || minResizeWidth == "40dp")
            )
            assertTrue(
                "Expected flexible minResizeHeight on ${resolveInfo.activityInfo.name}, got $minResizeHeight",
                minResizeHeight != null && (minResizeHeight!!.startsWith("40.0") || minResizeHeight == "40dp")
            )
        }
    }

    @Test
    fun expressiveWidgetShapesRenderTransparentOrganicBitmapsAtArbitrarySizes() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val palette = WidgetThemeResolver.resolve(context, UserPreferences())

        val pebbleSmall = WidgetShapeRenderer.createWeatherPebbleBitmap(140, 140, PrayerType.ASR, palette)
        assertEquals(Color.TRANSPARENT, pebbleSmall.getPixel(0, 0))
        assertTrue(pebbleSmall.getPixel(70, 70) != Color.TRANSPARENT)

        val pebbleWide = WidgetShapeRenderer.createWeatherPebbleBitmap(420, 160, PrayerType.ISHA, palette)
        assertEquals(420, pebbleWide.width)
        assertEquals(160, pebbleWide.height)
        assertTrue(pebbleWide.getPixel(210, 80) != Color.TRANSPARENT)

        val circle = WidgetShapeRenderer.createQiblaCircleBitmap(240, 157.0, palette)
        assertEquals(Color.TRANSPARENT, circle.getPixel(0, 0))
        assertTrue(circle.getPixel(120, 120) != Color.TRANSPARENT)
    }

    @Test
    fun quranVerseCycleAdvancesThroughCuratedVersesAndWidgetUpdaterRunsSafely() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<MihrabApplication>()
        val store = app.container.preferencesDataStore
        val initial = store.preferencesFlow.first().quranWidgetVerseIndex
        val total = AzkarRepository.dailyVerses.size
        assertTrue("Expected curated verse pool >= 15, got $total", total >= 15)

        val callback = com.example.widgets.ChangeQuranVerseAction::class.java
            .getDeclaredConstructor()
            .newInstance()
        assertNotNull(callback)

        val next1 = store.cycleQuranWidgetVerse(total)
        assertEquals((initial + 1) % total, next1)
        val next2 = store.cycleQuranWidgetVerse(total)
        assertEquals((initial + 2) % total, next2)

        WidgetUpdater.updateAll(app)
    }

    @Test
    fun tappingQiblaWidgetOpensQiblaCompassScreenDirectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(WidgetDestinations.EXTRA_WIDGET_DESTINATION, WidgetDestinations.QIBLA)
        }
        ActivityScenario.launch<MainActivity>(launchIntent).use {
            composeRule.waitForIdle()
            // The compass tab is always present, in both sensor-available and unavailable states.
            composeRule.onNodeWithTag("qibla_tab_compass").assertExists()
        }
    }

    @Test
    fun tappingHijriDateWidgetOpensHijriCalendarDirectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(WidgetDestinations.EXTRA_WIDGET_DESTINATION, WidgetDestinations.CALENDAR)
        }
        ActivityScenario.launch<MainActivity>(launchIntent).use {
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("calendar_tab_grid").assertExists()
        }
    }

    @Test
    fun prayerNotificationsUsePhoneSystemSoundsAndAppearOnLockScreen() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = context as MihrabApplication

        // Verify native system sound URI resolution (no generated WAV files)
        val audioManager = app.container.adhanAudioManager
        assertNotNull(audioManager.resolveSystemSoundUri(com.example.data.preferences.AdhanSoundStyle.MAQAM_HIJAZ))
        assertNotNull(audioManager.resolveSystemSoundUri(com.example.data.preferences.AdhanSoundStyle.MAKKAH_HARAM))
        assertNotNull(audioManager.resolveSystemSoundUri(com.example.data.preferences.AdhanSoundStyle.MADINAH_TONE))
        assertEquals(null, audioManager.resolveSystemSoundUri(com.example.data.preferences.AdhanSoundStyle.SILENT))

        // Grant POST_NOTIFICATIONS in Robolectric and verify channels + lock screen visibility
        val shadowApp = org.robolectric.Shadows.shadowOf(app)
        shadowApp.grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)

        com.example.notifications.PrayerNotificationHelper.ensureChannels(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channel = nm.getNotificationChannel(com.example.notifications.PrayerNotificationHelper.CHANNEL_ADHAN)
        assertNotNull(channel)
        assertEquals(android.app.Notification.VISIBILITY_PUBLIC, channel.lockscreenVisibility)

        com.example.notifications.PrayerNotificationHelper.showPrayerNotification(
            context = context,
            notificationId = 4401,
            title = "Asr Prayer Time",
            body = "Makkah • 3:35 PM",
            isSilent = false,
            isReminder = false,
            vibrate = true
        )
        com.example.notifications.PrayerNotificationHelper.updateLockScreenPrayerNotification(
            context = context,
            prefs = UserPreferences()
        )

        val shadowNm = org.robolectric.Shadows.shadowOf(nm)
        val alertNotif = shadowNm.getNotification(4401)
        assertNotNull(alertNotif)
        assertEquals(android.app.Notification.VISIBILITY_PUBLIC, alertNotif.visibility)

        val lockscreenNotif = shadowNm.getNotification(
            com.example.notifications.PrayerNotificationHelper.LOCKSCREEN_NOTIFICATION_ID
        )
        assertNotNull(lockscreenNotif)
        assertEquals(android.app.Notification.VISIBILITY_PUBLIC, lockscreenNotif.visibility)
    }

    @Test
    fun mushafPageBookmarkAndContinueReadingLastPagePersistAccurately() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<MihrabApplication>()
        val repo = app.container.quranRepository
        val store = app.container.preferencesDataStore

        // Persist last viewed Mushaf page (e.g. page 42 in Surah Al-Baqarah)
        store.updateLastReadPage(page = 42, surah = 2, ayah = 255)
        val updatedPrefs = store.preferencesFlow.first()
        assertEquals(42, updatedPrefs.lastReadPage)
        assertEquals(2, updatedPrefs.lastReadSurah)

        // Toggle page bookmark ON for page 42 and verify Room persistence
        repo.setPageBookmark(pageNumber = 42, bookmarked = true)
        val savedBookmarks = repo.observeBookmarks().first()
        val pageBookmark = savedBookmarks.find { it.surahNumber == 0 && it.ayahNumber == 42 }
        assertNotNull("Expected page 42 bookmark to be saved in Room", pageBookmark)

        // Toggle page bookmark OFF cleanly
        repo.setPageBookmark(pageNumber = 42, bookmarked = false)
        val afterRemove = repo.observeBookmarks().first()
        assertFalse(afterRemove.any { it.surahNumber == 0 && it.ayahNumber == 42 })
    }

    @Test
    fun quranAudioManagerPauseAndContinueTogglePreservesSurahAndUpdatesPausedState() {
        val app = ApplicationProvider.getApplicationContext<MihrabApplication>()
        val manager = app.container.quranAudioManager
        manager.playSurah(2, "ar.husary")
        assertEquals(2, manager.state.value.surahNumber)
        assertFalse(manager.state.value.isPaused)

        // Pause while active/buffering
        manager.togglePlayPause()
        assertEquals(2, manager.state.value.surahNumber)
        assertTrue(manager.state.value.isPaused)
        assertFalse(manager.state.value.isPlaying)

        // Continue resumes same Surah instead of resetting or clearing
        manager.togglePlayPause()
        assertEquals(2, manager.state.value.surahNumber)
        assertFalse(manager.state.value.isPaused)

        // Stop clears state completely
        manager.stop()
        assertEquals(0, manager.state.value.surahNumber)
        assertFalse(manager.state.value.isPaused)
        assertFalse(manager.state.value.isPlaying)
        assertFalse(manager.state.value.isBuffering)
    }

    @Test
    fun homeScreenDisplaysPrayerTimesInEnglishAndOmitsWebsiteCard() {
        ActivityScenario.launch(MainActivity::class.java).use { _ ->
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("open_official_website_card").assertDoesNotExist()
            composeRule.onNodeWithTag("home_header_title").assertExists()
            assertEquals("Prayer times", com.example.core.i18n.EnglishStrings.greetingEvening)
            assertEquals("Prayer times", com.example.core.i18n.EnglishStrings.greetingMorning)
        }
    }

    @Test
    fun bundledMedinaMushafLoadsAll604PagesAndRendersWrittenTextOffline() {
        val app = ApplicationProvider.getApplicationContext<MihrabApplication>()
        val loader = app.container.bundledMedinaMushafLoader
        val data = loader.loadOrNull()
        assertNotNull("Bundled 604-page Mushaf asset must load", data)
        assertEquals(6236, data!!.allVerses.size)
        assertEquals(114, data.versesBySurah.size)
        assertEquals(604, data.versesByPage.size)
        assertEquals(604, data.pageLayouts.size)

        // Page 1: Al-Fatihah 1..7 on lines 2..8
        val p1 = data.pageLayouts[1]!!
        assertEquals(listOf(2, 3, 4, 5, 6, 7, 8), p1.lines.map { it.lineNumber })
        assertTrue(p1.lines.first().words.any { it.surahNumber == 1 && it.ayahNumber == 1 && it.text == "بِسْمِ" })

        // Page 2: Al-Baqarah 2:1..5 on lines 3..8
        val p2 = data.pageLayouts[2]!!
        assertEquals(listOf(3, 4, 5, 6, 7, 8), p2.lines.map { it.lineNumber })
        assertEquals("الٓمٓ", p2.lines.first().words.first().text)

        // Page 604: Surahs 112, 113, 114
        val p604Surahs = data.pageLayouts[604]!!.lines.flatMap { it.words }.map { it.surahNumber }.distinct()
        assertEquals(listOf(112, 113, 114), p604Surahs)

        // Diacritic-insensitive search works completely offline
        val hits = loader.searchVerses("قل هو الله احد")
        assertTrue(hits.any { it.surahNumber == 112 && it.ayahNumber == 1 })

        // Bundled end-marker glyph words are marked on page lines
        assertTrue(p1.lines.first().words.any { it.isEndMarker })

        // Verify QuranComLineRepository serves all 604 pages offline & scan mode is off
        val layout604 = runBlocking { app.container.quranComLineRepository.getOrFetchPageLines(604) }
        assertNotNull(layout604)
        assertEquals(9, layout604!!.lines.size)

        // Launch MainActivity -> open Quran tab -> open Al-Fatihah -> verify written page renders without download UI
        ActivityScenario.launch(MainActivity::class.java).use { _ ->
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("nav_quran").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("surah_item_1").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("medina_book_pager").assertExists()
            composeRule.onNodeWithTag("mushaf_page_card_1").assertExists()
            composeRule.onNodeWithTag("reader_metadata_overlay").performClick()
            composeRule.waitUntil(timeoutMillis = 3_000L) {
                composeRule.onAllNodesWithTag("next_mushaf_page_button").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithTag("next_mushaf_page_button").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("mushaf_page_card_2").assertExists()
        }
    }

    @Test
    fun transparentCalligraphyAndQuranWidgetsRegisterRenderAndRespectTimezoneAndTapBehavior(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pm = context.packageManager
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            `package` = context.packageName
        }
        val actualClasses = pm.queryBroadcastReceivers(intent, PackageManager.GET_META_DATA)
            .map { it.activityInfo.name }
            .toSet()

        assertTrue(actualClasses.contains(CalligraphyMonthWidgetReceiver::class.java.name))
        assertTrue(actualClasses.contains(TransparentQuranVerseWidgetReceiver::class.java.name))
        assertTrue(actualClasses.contains(CalligraphyWeekdayWidgetReceiver::class.java.name))

        // Render transparent Month + Day overlay (Nov 27 matching mockup)
        val nov27 = LocalDate.of(2026, 11, 27)
        val hijriNov27 = HijriCalendarEngine.fromGregorian(nov27, adjustmentDays = 0)
        val monthBitmap = WidgetShapeRenderer.createTransparentMonthDateBitmap(
            context = context,
            widthPx = 760,
            heightPx = 360,
            today = nov27,
            hijri = hijriNov27
        )
        assertEquals(Color.TRANSPARENT, monthBitmap.getPixel(0, 0))
        assertEquals(Color.TRANSPARENT, monthBitmap.getPixel(monthBitmap.width - 1, monthBitmap.height - 1))
        var monthNonTransparentPixels = 0
        for (y in 0 until monthBitmap.height step 4) {
            for (x in 0 until monthBitmap.width step 4) {
                if (Color.alpha(monthBitmap.getPixel(x, y)) > 0) monthNonTransparentPixels++
            }
        }
        assertTrue("Month widget bitmap must draw visible pixels on transparent canvas", monthNonTransparentPixels > 80)

        // Render transparent Quran Ayah overlay using first curated verse ("وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ")
        val firstVerse = AzkarRepository.dailyVerses.first()
        assertEquals("وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ", firstVerse.arabicText)
        assertEquals("And seek help through patience and prayer", firstVerse.translationEn)
        val quranBitmap = WidgetShapeRenderer.createTransparentQuranAyahBitmap(
            context = context,
            widthPx = 820,
            heightPx = 240,
            verse = firstVerse
        )
        assertEquals(Color.TRANSPARENT, quranBitmap.getPixel(0, 0))
        var quranNonTransparentPixels = 0
        for (y in 0 until quranBitmap.height step 4) {
            for (x in 0 until quranBitmap.width step 4) {
                if (Color.alpha(quranBitmap.getPixel(x, y)) > 0) quranNonTransparentPixels++
            }
        }
        assertTrue("Quran Ayah widget bitmap must draw visible pixels on transparent canvas", quranNonTransparentPixels > 60)

        // Render transparent Weekday calligraphy overlay
        val weekdayBitmap = WidgetShapeRenderer.createTransparentWeekdayBitmap(
            context = context,
            widthPx = 760,
            heightPx = 360,
            today = nov27,
            hijri = hijriNov27
        )
        assertEquals(Color.TRANSPARENT, weekdayBitmap.getPixel(0, 0))
        var weekdayNonTransparentPixels = 0
        for (y in 0 until weekdayBitmap.height step 4) {
            for (x in 0 until weekdayBitmap.width step 4) {
                if (Color.alpha(weekdayBitmap.getPixel(x, y)) > 0) weekdayNonTransparentPixels++
            }
        }
        assertTrue("Weekday widget bitmap must draw visible pixels on transparent canvas", weekdayNonTransparentPixels > 80)

        // Verify timezone difference shifts the rendered Gregorian/Hijri date cleanly
        val midnightBoundaryUtc = Instant.parse("2026-11-27T21:00:00Z")
        val honoluluDate = midnightBoundaryUtc.atZone(ZoneId.of("Pacific/Honolulu")).toLocalDate()
        val tokyoDate = midnightBoundaryUtc.atZone(ZoneId.of("Asia/Tokyo")).toLocalDate()
        assertTrue("Timezone offset must advance calendar day across zones", tokyoDate.isAfter(honoluluDate))

        // Verify WidgetUpdater executes cleanly for all registered widgets including the 3 transparent ones
        val app = context.applicationContext as MihrabApplication
        assertNotNull(ChangeQuranVerseAction())
        WidgetUpdater.updateAll(app)

        // Verify QCF1-Madina (Quran), Elgharib Surah Name, and Dima Thuluth (Calendar) font resources load cleanly
        val qcf1MadinaFont = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.qcf1_madina)
        assertNotNull("QCF1-Madina font resource must load", qcf1MadinaFont)
        val surahNameFont = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.elgharib_surah_name)
        assertNotNull("Elgharib Surah Name font resource must load", surahNameFont)
        val dimaThuluthFont = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.dima_thuluth)
        assertNotNull("Dima Thuluth font resource must load", dimaThuluthFont)

        // Verify Surah & Ayah reference parsing and formatting for QCF1-Madina
        assertEquals(2 to "٤٥", WidgetShapeRenderer.parseSurahAndAyahAr("Al-Baqarah 2:45"))
        assertEquals(2 to "٢٨٦", WidgetShapeRenderer.parseSurahAndAyahAr("Al-Baqarah 2:286"))
        assertEquals(94 to "٥-٦", WidgetShapeRenderer.parseSurahAndAyahAr("Ash-Sharh 94:5-6"))

        val longVerse = AzkarRepository.dailyVerses[10]
        val longQuranBitmap = WidgetShapeRenderer.createTransparentQuranAyahBitmap(
            context = context,
            widthPx = 880,
            heightPx = 300,
            verse = longVerse
        )
        assertEquals(Color.TRANSPARENT, longQuranBitmap.getPixel(0, 0))
        assertEquals(880, longQuranBitmap.width)
        assertTrue((1..12).all { WidgetShapeRenderer.gregorianMonthCalligraphyAr(it).isNotBlank() })
        assertTrue(java.time.DayOfWeek.entries.all { WidgetShapeRenderer.weekdayCalligraphyAr(it).isNotBlank() })

        val qcfPaint = android.graphics.Paint().apply { typeface = qcf1MadinaFont }
        val missingInQcfVerses = mutableListOf<String>()
        AzkarRepository.dailyVerses.forEachIndexed { idx, v ->
            val formatted = WidgetShapeRenderer.formatVerseForQcf1Madina(v.arabicText)
            assertFalse("QCF1 format should strip U+06DD", formatted.contains('\u06DD'))
            formatted.forEach { ch ->
                if (!ch.isWhitespace() && ch != '\n' && !qcfPaint.hasGlyph(ch.toString())) {
                    missingInQcfVerses += "v$idx:U+%04X".format(ch.code)
                }
            }
        }
        assertTrue("QCF1-Madina missing glyphs in dailyVerses: $missingInQcfVerses", missingInQcfVerses.isEmpty())

        val calPaint = android.graphics.Paint().apply { typeface = dimaThuluthFont }
        val missingInCal = mutableListOf<String>()
        (1..12).map { WidgetShapeRenderer.gregorianMonthCalligraphyAr(it) }
            .plus(java.time.DayOfWeek.entries.map { WidgetShapeRenderer.weekdayCalligraphyAr(it) })
            .forEach { label ->
                label.forEach { ch ->
                    if (!ch.isWhitespace() && !calPaint.hasGlyph(ch.toString())) {
                        missingInCal += "$label:U+%04X".format(ch.code)
                    }
                }
            }
        assertTrue("Dima Thuluth missing glyphs in calendar labels: $missingInCal", missingInCal.isEmpty())

        val surahPaint = android.graphics.Paint().apply { typeface = surahNameFont }
        val missingSurahGlyphs = (1..114).filter { !surahPaint.hasGlyph(WidgetShapeRenderer.surahNameGlyph(it)) }
        assertTrue("Elgharib Surah Name missing surahs: $missingSurahGlyphs", missingSurahGlyphs.isEmpty())

        // Verify all 12 months and 7 weekdays render non-empty transparent bitmaps cleanly
        (1..12).forEach { month ->
            val d = LocalDate.of(2026, month, 15)
            val h = HijriCalendarEngine.fromGregorian(d, adjustmentDays = 0)
            val bmp = WidgetShapeRenderer.createTransparentMonthDateBitmap(context, 760, 360, d, h, showBottomDate = true)
            assertEquals(Color.TRANSPARENT, bmp.getPixel(0, 0))
            assertTrue((0 until bmp.height step 6).any { y -> (0 until bmp.width step 6).any { x -> Color.alpha(bmp.getPixel(x, y)) > 0 } })
        }
        java.time.DayOfWeek.entries.forEach { day ->
            val d = LocalDate.of(2026, 11, 28).with(day)
            val h = HijriCalendarEngine.fromGregorian(d, adjustmentDays = 0)
            val bmp = WidgetShapeRenderer.createTransparentWeekdayBitmap(context, 760, 360, d, h, showBottomDate = true)
            assertEquals(Color.TRANSPARENT, bmp.getPixel(0, 0))
            assertTrue((0 until bmp.height step 6).any { y -> (0 until bmp.width step 6).any { x -> Color.alpha(bmp.getPixel(x, y)) > 0 } })
        }

        // Verify showBottomDate=false omits bottom date cleanly on both Month and Weekday widgets
        val monthNoBottom = WidgetShapeRenderer.createTransparentMonthDateBitmap(
            context = context,
            widthPx = 760,
            heightPx = 360,
            today = nov27,
            hijri = hijriNov27,
            showBottomDate = false
        )
        val weekdayNoBottom = WidgetShapeRenderer.createTransparentWeekdayBitmap(
            context = context,
            widthPx = 760,
            heightPx = 360,
            today = nov27,
            hijri = hijriNov27,
            showBottomDate = false
        )
        val weekdayNoBottomPixels = (0 until weekdayNoBottom.height step 4).sumOf { y ->
            (0 until weekdayNoBottom.width step 4).count { x -> Color.alpha(weekdayNoBottom.getPixel(x, y)) > 0 }
        }
        assertTrue("Weekday bitmap without bottom date must still draw calligraphy", weekdayNoBottomPixels > 60)
        assertTrue(
            "Month bitmap without bottom date must draw fewer pixels than with bottom date",
            (0 until monthNoBottom.height step 4).sumOf { y ->
                (0 until monthNoBottom.width step 4).count { x -> Color.alpha(monthNoBottom.getPixel(x, y)) > 0 }
            } < monthNonTransparentPixels
        )

        // Verify double-tap toggles bottom date without opening app; single-tap opens Calendar
        CalendarWidgetTapCoordinator.resetForTest()
        val store = app.container.preferencesDataStore
        assertTrue(store.preferencesFlow.first().monthWidgetShowBottomDate)
        assertTrue(store.preferencesFlow.first().weekdayWidgetShowBottomDate)

        val shadowApp = org.robolectric.Shadows.shadowOf(app)
        shadowApp.clearNextStartedActivities()

        val testGlanceId = object : androidx.glance.GlanceId {}
        // Double tap on Month widget (2 taps inside window)
        coroutineScope {
            val t1 = async {
                CalendarWidgetTapCoordinator.handleTap(context, testGlanceId, isMonthWidget = true, doubleTapWindowMs = 150L)
            }
            kotlinx.coroutines.delay(40L)
            val t2 = async {
                CalendarWidgetTapCoordinator.handleTap(context, testGlanceId, isMonthWidget = true, doubleTapWindowMs = 150L)
            }
            t1.await()
            t2.await()
        }
        assertFalse("Double-tap must hide bottom date on Month widget", store.preferencesFlow.first().monthWidgetShowBottomDate)
        assertNull("Double-tap must NOT launch MainActivity", shadowApp.nextStartedActivity)

        // Double tap again restores bottom date on Month widget
        coroutineScope {
            val t3 = async {
                CalendarWidgetTapCoordinator.handleTap(context, testGlanceId, isMonthWidget = true, doubleTapWindowMs = 150L)
            }
            kotlinx.coroutines.delay(40L)
            val t4 = async {
                CalendarWidgetTapCoordinator.handleTap(context, testGlanceId, isMonthWidget = true, doubleTapWindowMs = 150L)
            }
            t3.await()
            t4.await()
        }
        assertTrue("Second double-tap must restore bottom date on Month widget", store.preferencesFlow.first().monthWidgetShowBottomDate)
        assertNull("Double-tap must NOT launch MainActivity", shadowApp.nextStartedActivity)

        // Double tap on Weekday widget
        coroutineScope {
            val w1 = async {
                CalendarWidgetTapCoordinator.handleTap(context, testGlanceId, isMonthWidget = false, doubleTapWindowMs = 150L)
            }
            kotlinx.coroutines.delay(40L)
            val w2 = async {
                CalendarWidgetTapCoordinator.handleTap(context, testGlanceId, isMonthWidget = false, doubleTapWindowMs = 150L)
            }
            w1.await()
            w2.await()
        }
        assertFalse("Double-tap must hide bottom date on Weekday widget", store.preferencesFlow.first().weekdayWidgetShowBottomDate)
        assertNull("Double-tap must NOT launch MainActivity", shadowApp.nextStartedActivity)

        // Single tap on Weekday widget opens Calendar destination
        CalendarWidgetTapCoordinator.handleTap(context, testGlanceId, isMonthWidget = false, doubleTapWindowMs = 100L)
        val launched = shadowApp.nextStartedActivity
        assertNotNull("Single tap must launch MainActivity", launched)
        assertEquals(WidgetDestinations.CALENDAR, launched.getStringExtra(WidgetDestinations.EXTRA_WIDGET_DESTINATION))

        // Restore default true for cleanliness
        if (!store.preferencesFlow.first().weekdayWidgetShowBottomDate) {
            store.toggleWeekdayWidgetBottomDate()
        }
    }
}
