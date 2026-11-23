package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.presentation.quran.AyahActionSheet
import com.example.presentation.quran.DigitalMadinahPage
import com.example.presentation.quran.QuranReadingTheme
import com.example.presentation.quran.rememberMushafPalette
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/**
 * Repair-verification renders: dense digital mushaf pages (offline), Arabic UI + dark
 * reading theme under the original reader chrome, and the expanded azkar categories.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h720dp")
class MihrabReaderVerificationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun capture(name: String) {
        composeRule.waitForIdle()
        val view = composeRule.activity.window.decorView
        val bmp = Bitmap.createBitmap(view.width.coerceAtLeast(1), view.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bmp))
        FileOutputStream(File("/downloads/screenshots/$name.png")).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun renderDensePage604() {
        composeRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                DigitalMadinahPage(
                    pageNumber = 604,
                    palette = rememberMushafPalette(QuranReadingTheme.KSU_MUSHAF),
                    onAyahClick = {}
                )
            }
        }
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()
        capture("m-mushaf-p604")
    }

    @Test
    fun renderBaqarahPage2() {
        composeRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                DigitalMadinahPage(
                    pageNumber = 2,
                    palette = rememberMushafPalette(QuranReadingTheme.KSU_MUSHAF),
                    onAyahClick = {}
                )
            }
        }
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()
        capture("m-mushaf-p2")
    }

    @Test
    fun renderSelectedAyahState() {
        // Fatiha ayah 2 sits on page 1; resolve its library id at runtime.
        val ayah = io.github.sherifshabans.mushaf.Quran
            .ayah(composeRule.activity, 1, 2)!!
        composeRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                DigitalMadinahPage(
                    pageNumber = 1,
                    palette = rememberMushafPalette(QuranReadingTheme.KSU_MUSHAF),
                    selectedAyahId = ayah.id,
                    onAyahClick = {}
                )
            }
        }
        composeRule.waitForIdle()
        Thread.sleep(1500)
        composeRule.waitForIdle()
        capture("m-mushaf-p1-selected")
    }

    @Test
    fun renderAyahActionSheet() {
        composeRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(rememberMushafPalette(QuranReadingTheme.KSU_MUSHAF).innerPageBg),
                contentAlignment = Alignment.BottomCenter
            ) {
                AyahActionSheet(
                    reference = "سورة الفاتحة • 1:2",
                    palette = rememberMushafPalette(QuranReadingTheme.KSU_MUSHAF),
                    isArabic = true,
                    showToolsOption = true,
                    onCopy = {},
                    onShare = {},
                    onTafsir = {},
                    onShowTools = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.waitForIdle()
        capture("m-ayah-action-sheet")
    }

}
