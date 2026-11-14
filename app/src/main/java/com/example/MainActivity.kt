package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.ProvideAppLocalization
import com.example.navigation.AppNavigation
import com.example.presentation.MainViewModel
import com.example.presentation.dhikr.DhikrViewModel
import com.example.presentation.quran.QuranViewModel
import com.example.ui.theme.MihrabTheme
import com.example.widgets.WidgetDestinations

class MainActivity : ComponentActivity() {

    private val appContainer by lazy { (application as MihrabApplication).container }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application, appContainer)
    }

    private val quranViewModel: QuranViewModel by viewModels {
        QuranViewModel.Factory(appContainer)
    }

    private val dhikrViewModel: DhikrViewModel by viewModels {
        DhikrViewModel.Factory(appContainer)
    }

    private val requestedWidgetDestination = mutableStateOf<String?>(null)
    private val requestedWidgetSurahNumber = mutableIntStateOf(0)
    private val widgetRequestNonce = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            @Suppress("DEPRECATION")
            window.navigationBarDividerColor = android.graphics.Color.TRANSPARENT
        }
        consumeWidgetIntent(intent)

        setContent {
            val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val prefs = mainUiState.prefs
            val widgetTarget by requestedWidgetDestination
            val widgetSurah by requestedWidgetSurahNumber
            val requestNonce by widgetRequestNonce

            MihrabTheme(
                themeMode = prefs.themeMode,
                dynamicColor = prefs.dynamicColor,
                amoledDark = prefs.amoledDark,
                palette = prefs.palette,
                isArabic = prefs.language == AppLanguage.ARABIC
            ) {
                ProvideAppLocalization(language = prefs.language) {
                    AppNavigation(
                        mainUiState = mainUiState,
                        mainViewModel = mainViewModel,
                        quranViewModel = quranViewModel,
                        dhikrViewModel = dhikrViewModel,
                        widgetDestination = widgetTarget,
                        widgetSurahNumber = widgetSurah,
                        widgetRequestNonce = requestNonce
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeWidgetIntent(intent)
    }

    private fun consumeWidgetIntent(intent: Intent?) {
        val dest = intent?.getStringExtra(WidgetDestinations.EXTRA_WIDGET_DESTINATION)
        if (!dest.isNullOrBlank()) {
            requestedWidgetDestination.value = dest
            val surah = intent.getIntExtra(WidgetDestinations.EXTRA_WIDGET_SURAH_NUMBER, 0)
            if (surah > 0) {
                requestedWidgetSurahNumber.intValue = surah
            }
            widgetRequestNonce.intValue += 1
        }
    }
}
