package com.example.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.design.ExpressiveCookie4Shape
import com.example.core.design.ScallopedStarShape
import com.example.core.design.islamicStarWatermark
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalStrings
import com.example.core.util.FormattingUtils
import com.example.data.azkar.AzkarRepository
import com.example.data.weather.WeatherRepository
import com.example.domain.prayer.PrayerEntry
import com.example.domain.prayer.PrayerType
import com.example.presentation.MainUiState
import com.example.ui.theme.AmiriFamily
import com.example.ui.theme.EmeraldAligned
import com.example.ui.theme.RobotoMonoFamily
import com.example.ui.theme.RobotoSerifFamily
import kotlin.math.roundToInt

@Composable
fun HomeDashboardScreen(
    uiState: MainUiState,
    onOpenCityPicker: () -> Unit,
    onRequestGps: () -> Unit,
    onTogglePrayerDone: (PrayerType) -> Unit,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val isArabic = strings.language == AppLanguage.ARABIC
    val prefs = uiState.prefs
    val today = uiState.now.toLocalDate()
    val dailyVerse = AzkarRepository.verseForDate(today)
    val dailyHadith = AzkarRepository.hadithForDate(today)

    val greeting = if (isArabic) {
        when (uiState.now.hour) {
            in 4..11 -> strings.greetingMorning
            in 12..16 -> strings.greetingAfternoon
            in 17..20 -> strings.greetingEvening
            else -> strings.greetingNight
        }
    } else {
        "Prayer times"
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Editorial Greeting & Date + Location/Weather Chip Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.testTag("home_header_title")
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.hijriDate.formatted(isArabic),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = FormattingUtils.formatGregorianDate(today, isArabic),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Streak Pill Badge in expressive cookie shape
                        if (uiState.streakDays > 0 || uiState.completedPrayersToday.isNotEmpty()) {
                            Surface(
                                shape = ScallopedStarShape(lobes = 10, depthFraction = 0.08f),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalFireDepartment,
                                        contentDescription = strings.prayerTrackerTitle,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${uiState.completedPrayersToday.size}/5",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = RobotoMonoFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // Location & Weather Pill Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .clickable(onClick = onOpenCityPicker)
                                .testTag("location_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = strings.locationSettingsLabel,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (prefs.countryName.isNotBlank()) "${prefs.cityName}, ${prefs.countryName}" else prefs.cityName,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            }
                        }

                        uiState.weather?.let { w ->
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.WbCloudy,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${w.temperatureC.roundToInt()}°C • ${WeatherRepository.conditionLabel(w.weatherCode, isArabic)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = RobotoMonoFamily,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    if (!prefs.hasExplicitLocation) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onRequestGps)
                                .testTag("gps_prompt_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.locationPermissionHint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.detectGps,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Hero Next Prayer Expressive Card
            item {
                NextPrayerHeroCard(uiState = uiState)
            }

            // 3. Jumu'ah / Ramadan Special Banners
            if (uiState.isJumuah) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = ExpressiveCookie4Shape(),
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Mosque,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.jumuahReminderTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = strings.jumuahReminderBody,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.hijriDate.month == 9 || uiState.daysUntilRamadan in 1..45) {
                item {
                    val isRamadan = uiState.hijriDate.month == 9
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.NightsStay,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isRamadan) strings.ramadanActiveLabel else strings.ramadanCountdownLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Text(
                                text = if (isRamadan) {
                                    "${strings.imsak} ${FormattingUtils.formatPrayerTime(uiState.dailyPrayerTimes.imsak, prefs.use24Hour, isArabic)} • ${strings.iftar} ${FormattingUtils.formatPrayerTime(uiState.dailyPrayerTimes.maghrib, prefs.use24Hour, isArabic)}"
                                } else {
                                    "${uiState.daysUntilRamadan} ${strings.daysLeft}"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = RobotoMonoFamily,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 4. Daily Prayer Times List (5 obligatory + Sunrise separate) with M3 Expressive grouped shapes
            val allEntries = uiState.dailyPrayerTimes.allEntries
            itemsIndexed(allEntries, key = { _, item -> item.type.name }) { index, entry ->
                PrayerTimeRowCard(
                    entry = entry,
                    index = index,
                    totalCount = allEntries.size,
                    uiState = uiState,
                    onToggleDone = { onTogglePrayerDone(entry.type) }
                )
            }

            // 5. Hijri Calendar & Monthly Timetable Shortcut Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenCalendar)
                        .testTag("open_calendar_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = strings.calendarAndTimetable,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.calendarAndTimetable,
                                style = MaterialTheme.typography.titleMedium
                            )
                            val nextOccasion = uiState.upcomingOccasions.firstOrNull()
                            if (nextOccasion != null) {
                                val occName = if (isArabic) nextOccasion.occasion.titleAr else nextOccasion.occasion.titleEn
                                Text(
                                    text = "$occName • ${nextOccasion.daysUntil} ${strings.daysLeft}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 6. Daily Quranic Verse & Hadith Inspiration Cards
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = strings.dailyVerseTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = dailyVerse.arabicText,
                            fontFamily = AmiriFamily,
                            fontSize = 23.sp,
                            lineHeight = 38.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (!isArabic) {
                            Text(
                                text = dailyVerse.translationEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (isArabic) dailyVerse.referenceAr else dailyVerse.referenceEn,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = strings.dailyHadithTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = dailyHadith.arabicText,
                            fontFamily = AmiriFamily,
                            fontSize = 20.sp,
                            lineHeight = 34.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (!isArabic) {
                            Text(
                                text = dailyHadith.translationEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (isArabic) dailyHadith.sourceAr else dailyHadith.sourceEn,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NextPrayerHeroCard(uiState: MainUiState) {
    val strings = LocalStrings.current
    val isArabic = strings.language == AppLanguage.ARABIC
    val next = uiState.nextPrayerInfo
    val animatedProgress by animateFloatAsState(
        targetValue = next.progress.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "prayer_progress"
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.88f)
        )
    )

    Surface(
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("next_prayer_hero_card")
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .islamicStarWatermark(
                    color = MaterialTheme.colorScheme.primary,
                    alpha = 0.11f,
                    centerBiasX = 0.88f,
                    centerBiasY = 0.30f,
                    scaleFactor = 0.78f
                )
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = prayerIconFor(next.nextPrayer),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.nextPrayer,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Text(
                        text = FormattingUtils.formatPrayerTime(next.nextTime, uiState.prefs.use24Hour, isArabic),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = RobotoMonoFamily,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = strings.prayerName(next.nextPrayer),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = strings.timeRemaining,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                        Text(
                            text = FormattingUtils.formatCountdown(next.remaining),
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = RobotoMonoFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.testTag("countdown_timer")
                        )
                    }
                }

                // M3 Expressive slider-style progress indicator with pill handle and end-stop dot
                val activeTrackColor = MaterialTheme.colorScheme.primary
                val inactiveTrackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f)
                val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                ) {
                    val trackH = 10.dp.toPx()
                    val centerY = size.height / 2f
                    val topY = centerY - trackH / 2f
                    val radius = CornerRadius(trackH / 2f, trackH / 2f)
                    val gapPx = 5.dp.toPx()
                    val handleW = 4.dp.toPx()
                    val handleH = 18.dp.toPx()
                    val clamped = animatedProgress.coerceIn(0.03f, 0.98f)
                    val splitX = if (isRtl) size.width * (1f - clamped) else size.width * clamped

                    if (!isRtl) {
                        val activeW = (splitX - gapPx).coerceAtLeast(trackH)
                        drawRoundRect(
                            color = activeTrackColor,
                            topLeft = Offset(0f, topY),
                            size = Size(activeW, trackH),
                            cornerRadius = radius
                        )
                        val inactiveStart = (splitX + gapPx).coerceAtMost(size.width)
                        val inactiveW = (size.width - inactiveStart).coerceAtLeast(0f)
                        if (inactiveW > trackH) {
                            drawRoundRect(
                                color = inactiveTrackColor,
                                topLeft = Offset(inactiveStart, topY),
                                size = Size(inactiveW, trackH),
                                cornerRadius = radius
                            )
                        }
                        drawCircle(
                            color = activeTrackColor.copy(alpha = 0.65f),
                            radius = 2.5.dp.toPx(),
                            center = Offset(size.width - trackH / 2f, centerY)
                        )
                    } else {
                        val inactiveW = (splitX - gapPx).coerceAtLeast(0f)
                        if (inactiveW > trackH) {
                            drawRoundRect(
                                color = inactiveTrackColor,
                                topLeft = Offset(0f, topY),
                                size = Size(inactiveW, trackH),
                                cornerRadius = radius
                            )
                        }
                        val activeStart = (splitX + gapPx).coerceAtMost(size.width)
                        val activeW = (size.width - activeStart).coerceAtLeast(trackH)
                        drawRoundRect(
                            color = activeTrackColor,
                            topLeft = Offset(activeStart, topY),
                            size = Size(activeW, trackH),
                            cornerRadius = radius
                        )
                        drawCircle(
                            color = activeTrackColor.copy(alpha = 0.65f),
                            radius = 2.5.dp.toPx(),
                            center = Offset(trackH / 2f, centerY)
                        )
                    }

                    // Expressive vertical pill handle
                    drawRoundRect(
                        color = activeTrackColor,
                        topLeft = Offset(splitX - handleW / 2f, centerY - handleH / 2f),
                        size = Size(handleW, handleH),
                        cornerRadius = CornerRadius(handleW / 2f, handleW / 2f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${strings.imsak}: ${FormattingUtils.formatPrayerTime(uiState.dailyPrayerTimes.imsak, uiState.prefs.use24Hour, isArabic)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = RobotoMonoFamily,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${strings.midnight}: ${FormattingUtils.formatPrayerTime(uiState.dailyPrayerTimes.midnight, uiState.prefs.use24Hour, isArabic)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = RobotoMonoFamily,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerTimeRowCard(
    entry: PrayerEntry,
    index: Int,
    totalCount: Int,
    uiState: MainUiState,
    onToggleDone: () -> Unit
) {
    val strings = LocalStrings.current
    val isArabic = strings.language == AppLanguage.ARABIC
    val isNext = uiState.nextPrayerInfo.nextPrayer == entry.type &&
        entry.time.toLocalDate() == uiState.now.toLocalDate()
    val hasPassed = !entry.time.isAfter(uiState.now)
    val isCompleted = uiState.completedPrayersToday.contains(entry.type)

    val containerColor by animateColorAsState(
        targetValue = when {
            isNext -> MaterialTheme.colorScheme.primaryContainer
            !entry.type.isObligatory -> MaterialTheme.colorScheme.surfaceContainerLowest
            hasPassed -> MaterialTheme.colorScheme.surfaceContainerLow
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        label = "prayer_row_bg"
    )

    val cardShape = when {
        isNext -> RoundedCornerShape(28.dp)
        index == 0 -> RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
        index == totalCount - 1 -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 26.dp, bottomEnd = 26.dp)
        else -> RoundedCornerShape(14.dp)
    }

    Surface(
        shape = cardShape,
        color = containerColor,
        tonalElevation = if (isNext) 4.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_row_${entry.type.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = if (isNext) ExpressiveCookie4Shape() else CircleShape,
                color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = prayerIconFor(entry.type),
                        contentDescription = strings.prayerName(entry.type),
                        tint = if (isNext) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.prayerName(entry.type),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isNext) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    if (!entry.type.isObligatory) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (isArabic) "توقيت شروق" else "Solar",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isNext) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = strings.upcoming,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else if (hasPassed) {
                        Text(
                            text = strings.completed,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = FormattingUtils.formatPrayerTime(entry.time, uiState.prefs.use24Hour, isArabic),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = RobotoMonoFamily,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            if (entry.type.isObligatory) {
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    shape = CircleShape,
                    color = if (isCompleted) EmeraldAligned else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleDone)
                        .testTag("prayer_check_${entry.type.name.lowercase()}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = strings.prayedOnTime,
                            tint = if (isCompleted) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun prayerIconFor(type: PrayerType): ImageVector = when (type) {
    PrayerType.FAJR -> Icons.Filled.WbTwilight
    PrayerType.SUNRISE -> Icons.Filled.WbSunny
    PrayerType.DHUHR -> Icons.Filled.WbSunny
    PrayerType.ASR -> Icons.Filled.WbCloudy
    PrayerType.MAGHRIB -> Icons.Filled.WbTwilight
    PrayerType.ISHA -> Icons.Filled.Bedtime
}
