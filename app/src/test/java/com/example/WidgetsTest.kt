package com.example

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.data.azkar.AzkarRepository
import com.example.data.preferences.UserPreferences
import com.example.domain.prayer.PrayerType
import com.example.widgets.HijriDateWidgetReceiver
import com.example.widgets.PrayerGlanceWidgetReceiver
import com.example.widgets.PrayerTimetableWidgetReceiver
import com.example.widgets.QiblaCircleWidgetReceiver
import com.example.widgets.QuranVerseWidgetReceiver
import com.example.widgets.WidgetDestinations
import com.example.widgets.WidgetShapeRenderer
import com.example.widgets.WidgetThemeResolver
import com.example.widgets.WidgetUpdater
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
}



