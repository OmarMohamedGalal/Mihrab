package com.example.presentation.dhikr

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.azkar.AzkarCategory
import com.example.data.azkar.AzkarRepository
import com.example.data.quran.QuranScriptFormatter
import com.example.ui.theme.AmiriFamily
import com.example.ui.theme.EmeraldAligned
import com.example.ui.theme.NotoSansArabicFamily
import com.example.ui.theme.RobotoMonoFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrScreen(
    uiState: DhikrUiState,
    viewModel: DhikrViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val isArabic = strings.language == AppLanguage.ARABIC
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        strings.tasbeehTab,
        strings.morningAzkar,
        strings.eveningAzkar,
        strings.afterPrayerAzkar,
        strings.sleepAzkar,
        strings.generalAzkar,
        strings.wakingAzkar,
        strings.mosqueHomeAzkar
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
        ) {
            Text(
                text = strings.dhikrTitle,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelLarge) },
                        modifier = Modifier.testTag("dhikr_tab_$index")
                    )
                }
            }

            if (selectedTab == 0) {
                TasbeehCounterView(
                    uiState = uiState,
                    viewModel = viewModel,
                    onOpenAddDialog = { showAddDialog = true }
                )
            } else {
                val category = when (selectedTab) {
                    1 -> AzkarCategory.MORNING
                    2 -> AzkarCategory.EVENING
                    3 -> AzkarCategory.AFTER_PRAYER
                    4 -> AzkarCategory.SLEEP_AND_DAILY
                    5 -> AzkarCategory.GENERAL
                    6 -> AzkarCategory.WAKING_UP
                    else -> AzkarCategory.MOSQUE_AND_HOME
                }
                val list = remember(category) {
                    AzkarRepository.azkarItems.filter { it.category == category }
                }
                val completedInCat = list.count { item ->
                    (uiState.azkarProgressMap[item.id] ?: 0) >= item.repeatTarget
                }
                val catFraction = if (list.isEmpty()) 0f else {
                    (completedInCat.toFloat() / list.size.toFloat()).coerceIn(0f, 1f)
                }

                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.testTag("azkar_list_${category.name.lowercase()}")
                ) {
                    // IslamBook-inspired Section Completion Header Card
                    item(key = "cat_progress_${category.name}") {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tabs[selectedTab],
                                            style = MaterialTheme.typography.titleMedium,
                                            fontFamily = NotoSansArabicFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isArabic) {
                                                "المُنجز: ${QuranScriptFormatter.toArabicIndic(completedInCat)} من ${QuranScriptFormatter.toArabicIndic(list.size)} ذكر"
                                            } else {
                                                "Completed: $completedInCat / ${list.size}"
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontFamily = if (isArabic) NotoSansArabicFamily else RobotoMonoFamily,
                                            color = if (completedInCat == list.size && list.isNotEmpty()) EmeraldAligned else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.resetAzkarCategory(list.map { it.id }) },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("reset_azkar_category_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = strings.resetCounter
                                        )
                                    }
                                }
                                LinearProgressIndicator(
                                    progress = { catFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }

                    items(list, key = { it.id }) { item ->
                        val count = uiState.azkarProgressMap[item.id] ?: 0
                        val remaining = (item.repeatTarget - count).coerceAtLeast(0)
                        val done = remaining == 0
                        val progress = (count.toFloat() / item.repeatTarget.toFloat()).coerceIn(0f, 1f)
                        val headerTitle = if (isArabic) item.titleAr else item.titleEn

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (done) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                }
                            ),
                            border = BorderStroke(
                                width = 0.8.dp,
                                color = if (done) {
                                    EmeraldAligned.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!done) {
                                        if (remaining == 1) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        viewModel.tapAzkarItem(item.id, item.repeatTarget)
                                    }
                                }
                                .testTag("azkar_card_${item.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Top bar: IslamBook Dhikr Title + Countdown Pill
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (headerTitle.isNotBlank()) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Text(
                                                text = headerTitle,
                                                fontFamily = if (isArabic) NotoSansArabicFamily else MaterialTheme.typography.labelMedium.fontFamily,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        shape = CircleShape,
                                        color = if (done) EmeraldAligned else MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (done) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = if (isArabic) {
                                                    if (done) {
                                                        "أُنجز (${QuranScriptFormatter.toArabicIndic(item.repeatTarget)})"
                                                    } else {
                                                        "المتبقي: ${QuranScriptFormatter.toArabicIndic(remaining)}"
                                                    }
                                                } else {
                                                    "$count / ${item.repeatTarget}"
                                                },
                                                style = MaterialTheme.typography.labelMedium,
                                                fontFamily = if (isArabic) NotoSansArabicFamily else RobotoMonoFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = if (done) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }

                                // Complete Arabic Text
                                Text(
                                    text = item.arabicText,
                                    fontFamily = AmiriFamily,
                                    fontSize = 22.sp,
                                    lineHeight = 39.sp,
                                    textAlign = TextAlign.Start,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // IslamBook Virtue / Reward Box (Arabic) or Transliteration/Translation (English)
                                if (isArabic) {
                                    if (item.fadlAr.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "الفضل: ${item.fadlAr}",
                                                fontFamily = NotoSansArabicFamily,
                                                fontSize = 13.sp,
                                                lineHeight = 21.sp,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                } else {
                                    if (item.transliteration.isNotBlank()) {
                                        Text(
                                            text = item.transliteration,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (item.translationEn.isNotBlank()) {
                                        Text(
                                            text = item.translationEn,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Reference + Copy / Share / Reset actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isArabic) item.referenceAr else item.referenceEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = if (isArabic) NotoSansArabicFamily else MaterialTheme.typography.labelSmall.fontFamily,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (count > 0) {
                                            IconButton(
                                                onClick = { viewModel.resetAzkarItem(item.id) },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.RestartAlt,
                                                    contentDescription = strings.resetCounter,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                val sharePayload = buildString {
                                                    append(item.arabicText)
                                                    if (isArabic && item.fadlAr.isNotBlank()) {
                                                        append("\n\nالفضل: ${item.fadlAr}")
                                                    }
                                                }
                                                clipboard?.setPrimaryClip(ClipData.newPlainText("dhikr", sharePayload))
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.ContentCopy,
                                                contentDescription = if (isArabic) "نسخ الذكر" else "Copy Dhikr",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                val shareText = buildString {
                                                    if (headerTitle.isNotBlank()) append("$headerTitle\n\n")
                                                    append(item.arabicText)
                                                    if (isArabic && item.fadlAr.isNotBlank()) {
                                                        append("\n\nالفضل: ${item.fadlAr}")
                                                    }
                                                    append("\n(${if (isArabic) item.referenceAr else item.referenceEn})")
                                                }
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                }
                                                context.startActivity(
                                                    Intent.createChooser(
                                                        intent,
                                                        if (isArabic) "مشاركة الذكر" else "Share Dhikr"
                                                    )
                                                )
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Share,
                                                contentDescription = if (isArabic) "مشاركة الذكر" else "Share Dhikr",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = if (done) EmeraldAligned else MaterialTheme.colorScheme.primary,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomDhikrDialog(
            onDismiss = { showAddDialog = false },
            onSave = { ar, meaning, target ->
                viewModel.addCustomDhikr(ar, meaning, target)
                showAddDialog = false
            }
        )
    }
}
