package com.example.presentation.dhikr

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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.azkar.AzkarCategory
import com.example.data.azkar.AzkarRepository
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
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        strings.tasbeehTab,
        strings.morningAzkar,
        strings.eveningAzkar,
        strings.afterPrayerAzkar,
        strings.sleepAzkar,
        strings.generalAzkar
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
                    else -> AzkarCategory.GENERAL
                }
                val list = remember(category) {
                    AzkarRepository.azkarItems.filter { it.category == category }
                }
                val completedInCat = list.count { item ->
                    (uiState.azkarProgressMap[item.id] ?: 0) >= item.repeatTarget
                }

                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.testTag("azkar_list_${category.name.lowercase()}")
                ) {
                    // Section completion banner
                    item(key = "cat_progress_${category.name}") {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) {
                                        "المُنجز: $completedInCat / ${list.size}"
                                    } else {
                                        "Completed: $completedInCat / ${list.size}"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = if (isArabic) NotoSansArabicFamily else RobotoMonoFamily,
                                    color = if (completedInCat == list.size && list.isNotEmpty()) EmeraldAligned else MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = { viewModel.resetAzkarCategory(list.map { it.id }) },
                                    modifier = Modifier.testTag("reset_azkar_category_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = strings.resetCounter
                                    )
                                }
                            }
                        }
                    }

                    items(list, key = { it.id }) { item ->
                        val count = uiState.azkarProgressMap[item.id] ?: 0
                        val done = count >= item.repeatTarget
                        val progress = (count.toFloat() / item.repeatTarget.toFloat()).coerceIn(0f, 1f)

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (done) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.tapAzkarItem(item.id, item.repeatTarget) }
                                .testTag("azkar_card_${item.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = item.arabicText,
                                    fontFamily = AmiriFamily,
                                    fontSize = 22.sp,
                                    lineHeight = 38.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (!isArabic) {
                                    Text(
                                        text = item.transliteration,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = item.translationEn,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = "$count / ${item.repeatTarget}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontFamily = RobotoMonoFamily,
                                                color = if (done) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
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
