package com.example.presentation.calendar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalStrings
import com.example.core.util.FormattingUtils
import com.example.domain.calendar.HijriCalendarEngine
import com.example.domain.prayer.DailyPrayerTimes
import com.example.presentation.MainUiState
import com.example.ui.theme.RobotoMonoFamily
import com.example.ui.theme.RobotoSerifFamily
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijriCalendarScreen(
    uiState: MainUiState,
    onCalculateDayPrayers: (LocalDate) -> DailyPrayerTimes,
    onCalculateMonthPrayers: (Int, Int) -> List<DailyPrayerTimes>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val strings = LocalStrings.current
    val isArabic = strings.language == AppLanguage.ARABIC
    val today = uiState.now.toLocalDate()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Calendar, 1 = Monthly Timetable
    var displayedMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDate by remember { mutableStateOf(today) }

    val selectedDayPrayers = remember(selectedDate, uiState.prefs) {
        onCalculateDayPrayers(selectedDate)
    }
    val selectedHijri = remember(selectedDate, uiState.prefs.hijriOffsetDays) {
        HijriCalendarEngine.fromGregorian(selectedDate, uiState.prefs.hijriOffsetDays)
    }
    val monthTimetable = remember(displayedMonth, uiState.prefs, selectedTab) {
        if (selectedTab == 1) onCalculateMonthPrayers(displayedMonth.year, displayedMonth.monthValue)
        else emptyList()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {
                Text(
                    text = strings.hijriCalendarTitle,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("calendar_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (isArabic) "رجوع" else "Back")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        displayedMonth = YearMonth.from(today)
                        selectedDate = today
                    },
                    modifier = Modifier.testTag("calendar_today_button")
                ) {
                    Text(strings.todayButton)
                }
            }
        )

        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                    modifier = Modifier.testTag("calendar_tab_grid")
                ) {
                    Text(strings.calendarGridTab)
                }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                    modifier = Modifier.testTag("calendar_tab_timetable")
                ) {
                    Text(strings.monthlyTimetableTab)
                }
            }

            // Month navigation header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = if (isArabic) "الشهر السابق" else "Previous month")
                }
                val locale = if (isArabic) Locale("ar") else Locale.ENGLISH
                val gMonthTitle = "${displayedMonth.month.getDisplayName(TextStyle.FULL, locale)} ${displayedMonth.year}"
                val midHijri = HijriCalendarEngine.fromGregorian(displayedMonth.atDay(15), uiState.prefs.hijriOffsetDays)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = gMonthTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${HijriCalendarEngine.monthName(midHijri.month, isArabic)} ${midHijri.year}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = if (isArabic) "الشهر التالي" else "Next month")
                }
            }

            if (selectedTab == 0) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Calendar Grid Card
                    item {
                        CalendarMonthGrid(
                            yearMonth = displayedMonth,
                            selectedDate = selectedDate,
                            today = today,
                            hijriOffset = uiState.prefs.hijriOffsetDays,
                            isArabic = isArabic,
                            onSelectDate = { selectedDate = it }
                        )
                    }

                    // Selected Day Prayers Card
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = selectedHijri.formatted(isArabic),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = FormattingUtils.formatGregorianDate(selectedDate, isArabic),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    selectedDayPrayers.obligatoryEntries.forEach { entry ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = strings.prayerName(entry.type),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = FormattingUtils.formatPrayerTime(entry.time, uiState.prefs.use24Hour, isArabic),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontFamily = RobotoMonoFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Islamic Occasions
                    item {
                        Text(
                            text = strings.islamicOccasionsTitle,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    items(uiState.upcomingOccasions, key = { it.occasion.titleEn }) { occ ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isArabic) occ.occasion.titleAr else occ.occasion.titleEn,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${occ.occasion.hijriDay} ${HijriCalendarEngine.monthName(occ.occasion.hijriMonth, isArabic)} • ${FormattingUtils.formatGregorianDate(occ.gregorianDate, isArabic)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = if (occ.daysUntil == 0L) strings.todayButton else "${occ.daysUntil} ${strings.daysLeft}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = RobotoMonoFamily,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Monthly Timetable
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Day", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.7f))
                            Text(strings.fajr, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(strings.dhuhr, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(strings.asr, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(strings.maghrib, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(strings.isha, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }

                    items(monthTimetable, key = { it.date.toString() }) { day ->
                        val isTodayRow = day.date == today
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isTodayRow) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${day.date.dayOfMonth}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = RobotoMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.7f)
                                )
                                listOf(day.fajr, day.dhuhr, day.asr, day.maghrib, day.isha).forEach { t ->
                                    Text(
                                        text = FormattingUtils.formatPrayerTime(t, true, isArabic),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = RobotoMonoFamily,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    hijriOffset: Int,
    isArabic: Boolean,
    onSelectDate: (LocalDate) -> Unit
) {
    val firstDay = yearMonth.atDay(1)
    val startOffset = firstDay.dayOfWeek.value % 7 // Sunday = 0
    val daysInMonth = yearMonth.lengthOfMonth()
    val locale = if (isArabic) Locale("ar") else Locale.ENGLISH
    val dayHeaders = listOf(
        DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
    )

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                dayHeaders.forEach { dow ->
                    Text(
                        text = dow.getDisplayName(TextStyle.SHORT, locale),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (dow == DayOfWeek.FRIDAY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7
            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        val dayNum = cellIndex - startOffset + 1
                        if (dayNum in 1..daysInMonth) {
                            val date = yearMonth.atDay(dayNum)
                            val hijri = HijriCalendarEngine.fromGregorian(date, hijriOffset)
                            val isSelected = date == selectedDate
                            val isToday = date == today
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> androidx.compose.ui.graphics.Color.Transparent
                                        }
                                    )
                                    .clickable { onSelectDate(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNum",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontFamily = RobotoMonoFamily,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    Text(
                                        text = "${hijri.day}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = RobotoMonoFamily,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
