package com.example.navigation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.i18n.LocalStrings
import com.example.presentation.MainUiState
import com.example.presentation.MainViewModel
import com.example.presentation.calendar.HijriCalendarScreen
import com.example.presentation.components.CityPickerSheet
import com.example.presentation.dhikr.DhikrScreen
import com.example.presentation.dhikr.DhikrViewModel
import com.example.presentation.home.HomeDashboardScreen
import com.example.presentation.qibla.QiblaScreen
import com.example.presentation.quran.QuranScreen
import com.example.presentation.quran.QuranViewModel
import com.example.presentation.settings.SettingsScreen
import com.example.widgets.WidgetDestinations
import kotlinx.serialization.Serializable

@Serializable object PrayerRoute
@Serializable object QiblaRoute
@Serializable object QuranRoute
@Serializable object DhikrRoute
@Serializable object SettingsRoute
@Serializable object CalendarRoute

@Composable
fun AppNavigation(
    mainUiState: MainUiState,
    mainViewModel: MainViewModel,
    quranViewModel: QuranViewModel,
    dhikrViewModel: DhikrViewModel,
    widgetDestination: String? = null,
    widgetRequestNonce: Int = 0
) {
    val strings = LocalStrings.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination

    val quranUiState by quranViewModel.uiState.collectAsStateWithLifecycle()
    val dhikrUiState by dhikrViewModel.uiState.collectAsStateWithLifecycle()

    var showCityPicker by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            mainViewModel.detectGpsLocation()
        }
    }

    val navItems = listOf(
        SliderNavItem(PrayerRoute, strings.navPrayer, Icons.Filled.Mosque, Icons.Outlined.Mosque, "nav_prayer"),
        SliderNavItem(QiblaRoute, strings.navQibla, Icons.Filled.Explore, Icons.Outlined.Explore, "nav_qibla"),
        SliderNavItem(QuranRoute, strings.navQuran, Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook, "nav_quran"),
        SliderNavItem(DhikrRoute, strings.navDhikr, Icons.Filled.RadioButtonChecked, Icons.Outlined.RadioButtonChecked, "nav_dhikr"),
        SliderNavItem(SettingsRoute, strings.navSettings, Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
    )

    val isFullScreenSubRoute = currentDest?.hasRoute<CalendarRoute>() == true
    val selectedIndex = navItems.indexOfFirst { currentDest?.hasRoute(it.route::class) == true }
        .let { if (it >= 0) it else 0 }

    fun navigateTopLevel(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    var handledWidgetNonce by remember { mutableIntStateOf(0) }

    LaunchedEffect(widgetDestination, widgetRequestNonce, currentDest) {
        if (currentDest != null && widgetRequestNonce > handledWidgetNonce && !widgetDestination.isNullOrBlank()) {
            handledWidgetNonce = widgetRequestNonce
            when (widgetDestination) {
                WidgetDestinations.QIBLA -> navigateTopLevel(QiblaRoute)
                WidgetDestinations.PRAYER -> navigateTopLevel(PrayerRoute)
                WidgetDestinations.CALENDAR -> {
                    navController.navigate(CalendarRoute) {
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useRail && !isFullScreenSubRoute) {
                    ExpressiveSliderBottomBar(
                        items = navItems,
                        selectedIndex = selectedIndex,
                        onSelectIndex = { idx ->
                            navItems.getOrNull(idx)?.let { navigateTopLevel(it.route) }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail && !isFullScreenSubRoute) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        navItems.forEach { item ->
                            val selected = currentDest?.hasRoute(item.route::class) == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label, maxLines = 1) },
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = PrayerRoute,
                    modifier = Modifier.weight(1f)
                ) {
                    composable<PrayerRoute> {
                        HomeDashboardScreen(
                            uiState = mainUiState,
                            onOpenCityPicker = { showCityPicker = true },
                            onRequestGps = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            onTogglePrayerDone = { mainViewModel.togglePrayerCompleted(it) },
                            onOpenCalendar = { navController.navigate(CalendarRoute) }
                        )
                    }
                    composable<QiblaRoute> {
                        QiblaScreen(uiState = mainUiState)
                    }
                    composable<QuranRoute> {
                        QuranScreen(
                            uiState = quranUiState,
                            viewModel = quranViewModel
                        )
                    }
                    composable<DhikrRoute> {
                        DhikrScreen(
                            uiState = dhikrUiState,
                            viewModel = dhikrViewModel
                        )
                    }
                    composable<SettingsRoute> {
                        SettingsScreen(
                            uiState = mainUiState,
                            viewModel = mainViewModel,
                            onOpenCityPicker = { showCityPicker = true }
                        )
                    }
                    composable<CalendarRoute> {
                        HijriCalendarScreen(
                            uiState = mainUiState,
                            onCalculateDayPrayers = { mainViewModel.calculateDayPrayers(it) },
                            onCalculateMonthPrayers = { y, m -> mainViewModel.calculateMonthPrayers(y, m) },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    if (showCityPicker) {
        CityPickerSheet(
            isDetectingGps = mainUiState.isDetectingGps,
            gpsError = mainUiState.gpsMessage,
            onDetectGps = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onSelectCity = { city ->
                mainViewModel.clearGpsMessage()
                mainViewModel.selectCity(city)
            },
            onDismiss = {
                mainViewModel.clearGpsMessage()
                showCityPicker = false
            }
        )
    }
}
