package com.example.presentation.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.preferences.AdhanSoundStyle
import com.example.domain.prayer.AsrMethod
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.HighLatitudeRule
import com.example.domain.prayer.PrayerType
import com.example.notifications.PrayerAlarmScheduler
import com.example.presentation.MainUiState
import com.example.presentation.MainViewModel
import com.example.ui.theme.AppColorPalette
import com.example.ui.theme.NotoSansArabicFamily
import com.example.ui.theme.RobotoMonoFamily
import com.example.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    onOpenCityPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val isArabic = strings.language == AppLanguage.ARABIC
    val context = LocalContext.current
    val prefs = uiState.prefs

    var showMethodDialog by remember { mutableStateOf(false) }
    var showAsrDialog by remember { mutableStateOf(false) }
    var showHighLatDialog by remember { mutableStateOf(false) }
    var showOffsetsDialog by remember { mutableStateOf(false) }
    var showAdhanDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showCalibrationDialog by remember { mutableStateOf(false) }

    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .testTag("settings_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = strings.settingsTitle,
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            // 1. Appearance Section
            item {
                SettingsSectionCard(title = strings.appearanceSection, icon = Icons.Filled.Palette) {
                    Text(
                        text = strings.themeModeLabel,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val modes = listOf(
                            ThemeMode.SYSTEM to strings.themeSystem,
                            ThemeMode.LIGHT to strings.themeLight,
                            ThemeMode.DARK to strings.themeDark
                        )
                        modes.forEachIndexed { idx, (mode, label) ->
                            SegmentedButton(
                                selected = prefs.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(idx, modes.size),
                                modifier = Modifier.testTag("theme_mode_${mode.name.lowercase()}")
                            ) {
                                Text(label)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsSwitchRow(
                        title = strings.amoledDarkLabel,
                        checked = prefs.amoledDark,
                        onCheckedChange = { viewModel.setAmoledDark(it) },
                        testTag = "amoled_switch"
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        SettingsSwitchRow(
                            title = strings.dynamicColorsLabel,
                            subtitle = if (isArabic) {
                                "يتبع ألوان الجهاز تلقائياً في أندرويد ١٢ فأحدث"
                            } else {
                                "Follows device Material You colors on Android 12+"
                            },
                            checked = prefs.dynamicColor,
                            onCheckedChange = { viewModel.setDynamicColor(it) },
                            testTag = "dynamic_color_switch"
                        )
                    } else {
                        Text(
                            text = if (isArabic) {
                                "تُستخدم السمة الافتراضية على الأجهزة الأقدم من أندرويد ١٢"
                            } else {
                                "Using default expressive theme below Android 12"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    if (!prefs.dynamicColor || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.colorPaletteLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val palettes = listOf(
                                AppColorPalette.TWILIGHT_PLUM to (if (isArabic) "بنفسجي الغسق" else "Twilight Plum"),
                                AppColorPalette.OASIS_EMERALD to (if (isArabic) "زمرد الواحة" else "Oasis Emerald"),
                                AppColorPalette.MAKKAH_GOLD to (if (isArabic) "ذهبي مكة" else "Makkah Gold")
                            )
                            palettes.forEach { (pal, name) ->
                                FilterChip(
                                    selected = prefs.palette == pal,
                                    onClick = { viewModel.setPalette(pal) },
                                    label = { Text(name) },
                                    modifier = Modifier.testTag("palette_${pal.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            // 2. Prayer Calculation Section
            item {
                SettingsSectionCard(title = strings.prayerSettingsSection, icon = Icons.Filled.Tune) {
                    SettingsActionRow(
                        title = strings.locationSettingsLabel,
                        subtitle = "${prefs.cityName}, ${prefs.countryName} (${prefs.timezoneId})",
                        icon = Icons.Filled.LocationOn,
                        onClick = onOpenCityPicker,
                        testTag = "settings_location_row"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingsActionRow(
                        title = strings.calculationMethodLabel,
                        subtitle = if (isArabic) prefs.calculationMethod.displayNameAr else prefs.calculationMethod.displayNameEn,
                        onClick = { showMethodDialog = true },
                        testTag = "settings_method_row"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingsActionRow(
                        title = strings.asrMethodLabel,
                        subtitle = if (isArabic) prefs.asrMethod.displayNameAr else prefs.asrMethod.displayNameEn,
                        onClick = { showAsrDialog = true },
                        testTag = "settings_asr_row"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingsActionRow(
                        title = strings.highLatitudeLabel,
                        subtitle = if (isArabic) prefs.highLatitudeRule.displayNameAr else prefs.highLatitudeRule.displayNameEn,
                        onClick = { showHighLatDialog = true },
                        testTag = "settings_high_lat_row"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingsActionRow(
                        title = strings.manualAdjustmentsLabel,
                        subtitle = "Fajr ${prefs.offsets.fajr}m • Dhuhr ${prefs.offsets.dhuhr}m • Asr ${prefs.offsets.asr}m • Maghrib ${prefs.offsets.maghrib}m • Isha ${prefs.offsets.isha}m",
                        onClick = { showOffsetsDialog = true },
                        testTag = "settings_offsets_row"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(strings.timeFormatLabel, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = !prefs.use24Hour,
                            onClick = { viewModel.setUse24Hour(false) },
                            shape = SegmentedButtonDefaults.itemShape(0, 2)
                        ) { Text(strings.format12h) }
                        SegmentedButton(
                            selected = prefs.use24Hour,
                            onClick = { viewModel.setUse24Hour(true) },
                            shape = SegmentedButtonDefaults.itemShape(1, 2)
                        ) { Text(strings.format24h) }
                    }
                }
            }

            // 3. Notifications & Adhan Section
            item {
                SettingsSectionCard(title = strings.notificationsSection, icon = Icons.Filled.NotificationsActive) {
                    if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        FilledTonalButton(
                            onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .testTag("request_notif_permission_button")
                        ) {
                            Text(strings.enableNotifications)
                        }
                    }

                    if (!PrayerAlarmScheduler.canScheduleExactAlarms(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    runCatching { context.startActivity(intent) }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.exactAlarmWarning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    SettingsActionRow(
                        title = strings.adhanSoundLabel,
                        subtitle = if (isArabic) prefs.adhanStyle.displayNameAr else prefs.adhanStyle.displayNameEn,
                        onClick = { showAdhanDialog = true },
                        testTag = "settings_adhan_style_row"
                    )

                    FilledTonalButton(
                        onClick = { viewModel.toggleAdhanPreview() },
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("preview_adhan_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isAdhanPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = strings.previewAdhan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (uiState.isAdhanPlaying) "Stop" else strings.previewAdhan)
                    }

                    SettingsSwitchRow(
                        title = strings.vibrationLabel,
                        checked = prefs.vibrationEnabled,
                        onCheckedChange = { viewModel.setVibrationEnabled(it) },
                        testTag = "vibration_switch"
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${strings.prePrayerReminderLabel}: ${prefs.reminderMinutesBefore} ${strings.minutesBefore}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0, 5, 10, 15, 20, 30).forEach { min ->
                            FilterChip(
                                selected = prefs.reminderMinutesBefore == min,
                                onClick = { viewModel.setReminderMinutes(min) },
                                label = { Text("${min}m", fontFamily = RobotoMonoFamily) }
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    PrayerType.obligatoryPrayers.forEach { prayer ->
                        SettingsSwitchRow(
                            title = strings.prayerName(prayer),
                            checked = prefs.isPrayerNotificationEnabled(prayer),
                            onCheckedChange = { viewModel.setPrayerNotification(prayer, it) },
                            testTag = "notify_${prayer.name.lowercase()}_switch"
                        )
                    }
                }
            }

            // 4. Qibla & Sensors
            item {
                SettingsSectionCard(title = strings.qiblaSection, icon = Icons.Filled.Explore) {
                    val status = when {
                        !uiState.compassState.sensorAvailable -> strings.sensorUnavailable
                        uiState.compassState.usingRotationVector -> "Rotation Vector Active (${uiState.compassState.accuracy})"
                        else -> "Accelerometer + Magnetometer (${uiState.compassState.accuracy})"
                    }
                    SettingsActionRow(
                        title = strings.sensorDiagnosticsLabel,
                        subtitle = status,
                        onClick = { showCalibrationDialog = true },
                        testTag = "settings_sensor_diag"
                    )
                }
            }

            // 5. General, Localization & Hijri Adjustment
            item {
                SettingsSectionCard(title = strings.generalSection, icon = Icons.Filled.Language) {
                    Text(strings.languageLabel, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = prefs.language == AppLanguage.ENGLISH,
                            onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                            modifier = Modifier.testTag("lang_en_button")
                        ) { Text("English") }
                        SegmentedButton(
                            selected = prefs.language == AppLanguage.ARABIC,
                            onClick = { viewModel.setLanguage(AppLanguage.ARABIC) },
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                            modifier = Modifier.testTag("lang_ar_button")
                        ) { Text("العربية", fontFamily = NotoSansArabicFamily) }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${strings.hijriAdjustmentLabel}: ${if (prefs.hijriOffsetDays > 0) "+${prefs.hijriOffsetDays}" else prefs.hijriOffsetDays}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (-2..2).forEach { offset ->
                            FilterChip(
                                selected = prefs.hijriOffsetDays == offset,
                                onClick = { viewModel.setHijriOffset(offset) },
                                label = { Text(if (offset > 0) "+$offset" else "$offset", fontFamily = RobotoMonoFamily) }
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                    SettingsActionRow(
                        title = strings.privacyPolicyLabel,
                        subtitle = "Local-first architecture • Zero tracking",
                        icon = Icons.Filled.PrivacyTip,
                        onClick = { showPrivacyDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingsActionRow(
                        title = strings.aboutAppLabel,
                        subtitle = "Mihrab v2.0 • Material 3 Expressive",
                        icon = Icons.Filled.Info,
                        onClick = { showAboutDialog = true }
                    )
                }
            }
        }
    }

    if (showMethodDialog) {
        OptionPickerDialog(
            title = strings.calculationMethodLabel,
            options = CalculationMethod.entries,
            selected = prefs.calculationMethod,
            label = { if (isArabic) it.displayNameAr else it.displayNameEn },
            onSelect = {
                viewModel.setCalculationMethod(it)
                showMethodDialog = false
            },
            onDismiss = { showMethodDialog = false }
        )
    }

    if (showAsrDialog) {
        OptionPickerDialog(
            title = strings.asrMethodLabel,
            options = AsrMethod.entries,
            selected = prefs.asrMethod,
            label = { if (isArabic) it.displayNameAr else it.displayNameEn },
            onSelect = {
                viewModel.setAsrMethod(it)
                showAsrDialog = false
            },
            onDismiss = { showAsrDialog = false }
        )
    }

    if (showHighLatDialog) {
        OptionPickerDialog(
            title = strings.highLatitudeLabel,
            options = HighLatitudeRule.entries,
            selected = prefs.highLatitudeRule,
            label = { if (isArabic) it.displayNameAr else it.displayNameEn },
            onSelect = {
                viewModel.setHighLatitudeRule(it)
                showHighLatDialog = false
            },
            onDismiss = { showHighLatDialog = false }
        )
    }

    if (showAdhanDialog) {
        OptionPickerDialog(
            title = strings.adhanSoundLabel,
            options = AdhanSoundStyle.entries,
            selected = prefs.adhanStyle,
            label = { if (isArabic) it.displayNameAr else it.displayNameEn },
            onSelect = {
                viewModel.setAdhanStyle(it)
                showAdhanDialog = false
            },
            onDismiss = { showAdhanDialog = false }
        )
    }

    if (showOffsetsDialog) {
        AlertDialog(
            onDismissRequest = { showOffsetsDialog = false },
            title = { Text(strings.manualAdjustmentsLabel) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrayerType.entries.forEach { prayer ->
                        val offset = prefs.offsets.offsetFor(prayer)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(strings.prayerName(prayer), style = MaterialTheme.typography.titleSmall)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { viewModel.setOffset(prayer, offset - 1) }) { Text("-1") }
                                Text(
                                    text = "${if (offset > 0) "+$offset" else offset}m",
                                    fontFamily = RobotoMonoFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { viewModel.setOffset(prayer, offset + 1) }) { Text("+1") }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOffsetsDialog = false }) { Text("Done") }
            }
        )
    }

    if (showCalibrationDialog) {
        AlertDialog(
            onDismissRequest = { showCalibrationDialog = false },
            title = { Text(strings.calibrateCompass) },
            text = { Text(strings.calibrationInstructions) },
            confirmButton = { TextButton(onClick = { showCalibrationDialog = false }) { Text("OK") } }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(strings.privacyPolicyLabel) },
            text = {
                Text("All prayer calculations, Qibla compass readings, Hijri dates, Tasbeeh counts, and cached Quran verses run locally on your device. Network access is used strictly when requesting Open-Meteo weather, downloading Quran Surahs from AlQuran Cloud, or streaming recitations.")
            },
            confirmButton = { TextButton(onClick = { showPrivacyDialog = false }) { Text("OK") } }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(strings.aboutAppLabel) },
            text = {
                Text("Mihrab — Islamic Prayer Times, Qibla Compass, Quran, Dhikr & Hijri Calendar built with Material 3 Expressive Design and Jean Meeus astronomical calculations.")
            },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("OK") } }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    testTag: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .let { if (testTag != null) it.testTag(testTag) else it }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun <T> OptionPickerDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(modifier = Modifier.height(320.dp)) {
                items(options) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = item == selected, onClick = { onSelect(item) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label(item), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
