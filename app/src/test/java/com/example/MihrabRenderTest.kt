package com.example

import android.graphics.Bitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Canvas
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/**
 * Real-pixel renders of Mihrab's main screens under Robolectric native graphics.
 * Evidence for the repair review: the app is rendered as-is (starter-seeded
 * Quran data only, no network), one PNG per screen, light theme.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h720dp")
class MihrabRenderTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun capture(name: String) {
        composeRule.waitForIdle()
        val activity = composeRule.activity
        val dialog = org.robolectric.shadows.ShadowDialog.getLatestDialog()
        val view = if (dialog?.isShowing == true) dialog.window!!.decorView else activity.window.decorView
        val w = view.width.coerceAtLeast(1)
        val h = view.height.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        view.draw(canvas)
        val out = File("/downloads/screenshots/$name.png")
        FileOutputStream(out).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun renderHome() {
        composeRule.waitForIdle()
        capture("m-home")
        composeRule.onNodeWithTag("home_list")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("daily_tab_hadith"))
        composeRule.onNodeWithTag("daily_tab_hadith").performClick()
        composeRule.waitForIdle()
        capture("m-home-hadith")
    }

    @Test
    fun renderQuranReader() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_quran").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("continue_reading_card").performClick()
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()
        capture("m-quran-reader")
    }

    @Test
    fun renderQuranReaderToolsVisible() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_quran").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("continue_reading_card").performClick()
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()
        // Reveal the toolbars via the quiet metadata strip, then capture the chrome.
        composeRule.onNodeWithTag("reader_metadata_overlay").performClick()
        composeRule.waitForIdle()
        capture("m-quran-reader-tools")
    }

    @Test
    fun renderQuranReaderSettingsSheet() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_quran").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("continue_reading_card").performClick()
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader_metadata_overlay").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader_settings_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reader_settings_sheet").assertExists()
        capture("m-quran-reader-settings")
    }

    @Test
    fun renderQuran() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_quran").performClick()
        composeRule.waitForIdle()
        capture("m-quran")
    }

    @Test
    fun renderQibla() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_qibla").performClick()
        composeRule.waitForIdle()
        capture("m-qibla")
    }

    @Test
    fun renderDhikr() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_dhikr").performClick()
        composeRule.waitForIdle()
        capture("m-dhikr")
    }

    @Test
    fun renderSettings() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_settings").performClick()
        composeRule.waitForIdle()
        capture("m-settings")
    }

    @Test
    fun renderReaderArabicDark() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_settings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("theme_mode_dark"))
        composeRule.onNodeWithTag("theme_mode_dark").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("lang_ar_button"))
        composeRule.onNodeWithTag("lang_ar_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_quran").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("continue_reading_card").performClick()
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()
        capture("m-quran-reader-ar-dark")
    }

    @Test
    fun renderAzkarMorningExpanded() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_dhikr").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Morning").performClick()
        composeRule.waitForIdle()
        capture("m-dhikr-morning")
    }

    @Test
    fun renderReaderDensePage604InApp() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_quran").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("quran_tab_pages").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("mushaf_quick_page_604").performScrollTo().performClick()
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()
        capture("m-reader-p604-app")
    }

    @Test
    fun renderArabicUsabilityAndVerifyShortcuts() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_settings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("theme_mode_light").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("lang_ar_button"))
        composeRule.onNodeWithTag("lang_ar_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_prayer").performClick()
        composeRule.waitForIdle()
        capture("m-home-ar")
        composeRule.onNodeWithTag("home_list")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("prayer_row_maghrib"))
        composeRule.waitForIdle()
        capture("m-home-ar-prayers")
        composeRule.onNodeWithTag("home_list")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("home_date_calendar"))
        composeRule.onNodeWithTag("home_date_calendar").performClick()
        composeRule.waitForIdle()
        capture("m-calendar-ar")
        composeRule.onNodeWithTag("calendar_today_button").performClick()
        composeRule.onNodeWithTag("calendar_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_dhikr").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dhikr_categories_button").performClick()
        composeRule.waitForIdle()
        capture("m-dhikr-categories-ar")
        composeRule.onNodeWithTag("dhikr_category_6").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("azkar_list_waking_up").assertExists()
        composeRule.onNodeWithTag("nav_qibla").performClick()
        composeRule.waitForIdle()
        capture("m-qibla-ar")
        composeRule.onNodeWithTag("nav_settings").performClick()
        composeRule.waitForIdle()
        capture("m-settings-ar")
        composeRule.onNodeWithTag("settings_list").performScrollToNode(androidx.compose.ui.test.hasTestTag("settings_location_row"))
        composeRule.onNodeWithTag("settings_location_row").assertExists()
    }

    @Test
    fun renderRealAyahSelection() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_quran").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("continue_reading_card").performClick()
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.onNodeWithTag("medina_book_pager").performTouchInput { click(Offset(180f, 150f)) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ayah_action_strip").assertExists()
        capture("m-reader-real-selection")
    }
}
