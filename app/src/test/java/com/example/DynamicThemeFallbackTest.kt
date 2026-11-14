package com.example

import com.example.data.preferences.UserPreferences
import com.example.data.preferences.defaultDynamicColorEnabled
import com.example.ui.theme.AppColorPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicThemeFallbackTest {

    @Test
    fun belowAndroid12DisablesDynamicColorAndUsesTwilightPlumDefault() {
        assertFalse(defaultDynamicColorEnabled(sdkInt = 30))
        assertFalse(defaultDynamicColorEnabled(sdkInt = 24))
        val fallbackPrefs = UserPreferences(dynamicColor = defaultDynamicColorEnabled(sdkInt = 30))
        assertFalse(fallbackPrefs.dynamicColor)
        assertEquals(AppColorPalette.TWILIGHT_PLUM, fallbackPrefs.palette)
    }

    @Test
    fun android12AndAboveEnablesDynamicColorByDefault() {
        assertTrue(defaultDynamicColorEnabled(sdkInt = 31))
        assertTrue(defaultDynamicColorEnabled(sdkInt = 35))
        assertTrue(defaultDynamicColorEnabled(sdkInt = 36))
    }
}
