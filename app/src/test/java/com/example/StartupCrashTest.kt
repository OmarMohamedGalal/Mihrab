package com.example

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.defaultDynamicColorEnabled
import com.example.sensors.QiblaSensorManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.SensorEventBuilder

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StartupCrashTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesAndNavigatesAllTabsWithoutCrashing() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("expressive_slider_bottom_bar").assertExists()
        composeRule.onNodeWithTag("next_prayer_hero_card").assertExists()

        composeRule.onNodeWithTag("nav_qibla").performClick()
        composeRule.waitForIdle()
        // The compass tab is always present, in both sensor-available and unavailable states.
        composeRule.onNodeWithTag("qibla_tab_compass").assertExists()

        composeRule.onNodeWithTag("nav_quran").performClick()
        composeRule.waitForIdle()
        // Test switching to Listen mode and opening the M3 Expressive Now Playing UI
        composeRule.onNodeWithTag("quran_mode_listen").assertExists().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("quran_listen_root").assertExists()
        composeRule.onNodeWithTag("quran_listen_tab_1").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reciter_item_ar.islamsobhi").assertExists().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("mini_player_expand_button").assertExists().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("quran_full_now_playing_screen").assertExists()
        composeRule.onNodeWithTag("expressive_wavy_scrubber").assertExists()
        composeRule.onNodeWithTag("now_playing_collapse_button").performClick()
        composeRule.waitForIdle()
        // Switch back to Read mode
        composeRule.onNodeWithTag("quran_mode_read").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("quran_tab_pages").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("mushaf_quick_page_604").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("mushaf_page_card_604").assertExists()
        // Reader opens in reading-first fullscreen; reveal chrome before using it.
        composeRule.onNodeWithTag("reader_metadata_overlay").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("surah_back_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("quran_tab_surahs").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("continue_reading_card").assertExists().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader_metadata_overlay").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tanzil_surah_picker").assertExists()
        composeRule.onNodeWithTag("medina_book_pager").assertExists()
        composeRule.onNodeWithTag("next_mushaf_page_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("surah_back_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_dhikr").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tasbeeh_list")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("tasbeeh_tap_button"))
        composeRule.onNodeWithTag("tasbeeh_tap_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dhikr_tab_1").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("azkar_list_morning").assertExists()
        composeRule.onNodeWithTag("dhikr_tab_4").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("azkar_list_sleep_and_daily").assertExists()
        composeRule.onNodeWithTag("dhikr_tab_5").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("azkar_list_general").assertExists()
        composeRule.onNodeWithTag("dhikr_tab_6").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("azkar_list_waking_up").assertExists()
        composeRule.onNodeWithTag("dhikr_tab_7").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("azkar_list_mosque_and_home").assertExists()

        composeRule.onNodeWithTag("nav_settings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("lang_ar_button"))
        composeRule.onNodeWithTag("lang_ar_button").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun dynamicColorDefaultsToTrueOnAndroid12Plus() {
        assertTrue(defaultDynamicColorEnabled())
        assertTrue(UserPreferences().dynamicColor)
    }

    @Test
    fun qiblaSensorManagerHandlesRotationEventsSafelyFromAppContext() = runBlocking {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val sm = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val shadowSm = Shadows.shadowOf(sm)
        val rotSensor = SensorEventBuilder.newBuilder()
        val sensor = org.robolectric.shadows.ShadowSensor.newInstance(Sensor.TYPE_ROTATION_VECTOR)
        shadowSm.addSensor(sensor)

        val qiblaManager = QiblaSensorManager(appContext)
        var received = false
        val job = launch {
            val state = qiblaManager.observeCompass(21.4225, 39.8262).first()
            received = state.sensorAvailable
        }
        kotlinx.coroutines.delay(50)
        val event: SensorEvent = rotSensor
            .setSensor(sensor)
            .setValues(floatArrayOf(0f, 0f, 0.38268f, 0.92388f))
            .build()
        shadowSm.sendSensorEventToListeners(event)
        job.join()
        assertTrue(received)
    }
}
