package com.example.presentation.dhikr

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.design.ExpressiveCookie4Shape
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalStrings
import com.example.data.azkar.AzkarRepository
import com.example.ui.theme.AmiriFamily
import com.example.ui.theme.NotoSansArabicFamily
import com.example.ui.theme.RobotoMonoFamily

@Composable
internal fun TasbeehCounterView(
    uiState: DhikrUiState,
    viewModel: DhikrViewModel,
    onOpenAddDialog: () -> Unit
) {
    val strings = LocalStrings.current
    val isArabic = strings.language == AppLanguage.ARABIC
    val haptic = LocalHapticFeedback.current
    var pulseTrigger by remember { mutableIntStateOf(0) }

    val scale by animateFloatAsState(
        targetValue = if (pulseTrigger % 2 == 0) 1f else 0.96f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tasbeeh_scale"
    )
    val rotation by animateFloatAsState(
        targetValue = (uiState.currentCount * 15f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tasbeeh_rotation"
    )

    val activePreset = remember(uiState.activeDhikrArabic) {
        AzkarRepository.tasbeehPresets.firstOrNull { it.arabicText == uiState.activeDhikrArabic }
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .testTag("tasbeeh_list")
    ) {
        // Daily goal progress card
        item {
            val goal = uiState.prefs.tasbeehDailyGoal.coerceAtLeast(1)
            val goalProgress = (uiState.todayTotal.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.todayTotal}: ${uiState.todayTotal}",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = RobotoMonoFamily
                        )
                        Text(
                            text = "${strings.dailyGoal}: $goal",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = RobotoMonoFamily,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { goalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        // Preset + Custom Dhikr horizontal chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AzkarRepository.tasbeehPresets.forEach { preset ->
                    FilterChip(
                        selected = uiState.activeDhikrArabic == preset.arabicText,
                        onClick = {
                            viewModel.selectPreset(preset.arabicText, preset.transliteration, preset.targetCount)
                        },
                        label = {
                            Text(
                                text = if (isArabic) preset.arabicText.take(22) else preset.transliteration,
                                fontFamily = if (isArabic) NotoSansArabicFamily else MaterialTheme.typography.labelLarge.fontFamily
                            )
                        }
                    )
                }
                uiState.customDhikrs.forEach { custom ->
                    FilterChip(
                        selected = uiState.activeDhikrArabic == custom.arabicText,
                        onClick = {
                            viewModel.selectPreset(custom.arabicText, custom.meaning, custom.targetCount)
                        },
                        label = { Text(custom.meaning.ifBlank { custom.arabicText.take(12) }) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { viewModel.deleteCustomDhikr(custom) }
                            )
                        }
                    )
                }
                FilledTonalButton(
                    onClick = onOpenAddDialog,
                    shape = CircleShape,
                    modifier = Modifier.testTag("add_custom_dhikr_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = strings.addCustomDhikr, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.addCustomDhikr)
                }
            }
        }

        // Active Dhikr Arabic & Meaning
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = uiState.activeDhikrArabic,
                    fontFamily = AmiriFamily,
                    fontSize = 28.sp,
                    lineHeight = 44.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                val subtitleText = activePreset?.let {
                    if (isArabic) it.translationEn else "${it.transliteration} — ${it.translationEn}"
                } ?: uiState.activeDhikrMeaning
                if (subtitleText.isNotBlank() && !isArabic) {
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Tactile M3 Expressive 4-sided Cookie Counter Button
        item {
            val targetProgress = (uiState.currentCount.toFloat() / uiState.activeTargetCount.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(230.dp)
            ) {
                CircularProgressIndicator(
                    progress = { targetProgress },
                    modifier = Modifier.size(226.dp),
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
                Surface(
                    shape = ExpressiveCookie4Shape(indentation = 0.13f),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .size(188.dp)
                        .scale(scale)
                        .rotate(rotation)
                        .clip(ExpressiveCookie4Shape(indentation = 0.13f))
                        .clickable {
                            pulseTrigger++
                            haptic.performHapticFeedback(
                                if (uiState.currentCount + 1 == uiState.activeTargetCount) {
                                    HapticFeedbackType.LongPress
                                } else {
                                    HapticFeedbackType.TextHandleMove
                                }
                            )
                            viewModel.incrementTasbeeh()
                        }
                        .testTag("tasbeeh_tap_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(-rotation)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.currentCount}",
                                style = MaterialTheme.typography.displayLarge,
                                fontFamily = RobotoMonoFamily,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.testTag("tasbeeh_count_value")
                            )
                            Text(
                                text = "/ ${uiState.activeTargetCount}",
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = RobotoMonoFamily,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Reset + Goal Controls
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { viewModel.resetTasbeeh() },
                    shape = CircleShape,
                    modifier = Modifier.testTag("tasbeeh_reset_button")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = strings.resetCounter)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.resetCounter)
                }

                listOf(100, 300, 1000).forEach { goalOption ->
                    FilterChip(
                        selected = uiState.prefs.tasbeehDailyGoal == goalOption,
                        onClick = { viewModel.setDailyGoal(goalOption) },
                        label = { Text("$goalOption", fontFamily = RobotoMonoFamily) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun AddCustomDhikrDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Int) -> Unit
) {
    val strings = LocalStrings.current
    var arabic by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("33") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addCustomDhikr) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = arabic,
                    onValueChange = { arabic = it },
                    label = { Text(strings.dhikrArabicHint) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = meaning,
                    onValueChange = { meaning = it },
                    label = { Text(strings.dhikrMeaningHint) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it.filter { ch -> ch.isDigit() } },
                    label = { Text(strings.targetCountHint) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val count = target.toIntOrNull()?.coerceIn(1, 10000) ?: 33
                    onSave(arabic, meaning, count)
                },
                enabled = arabic.isNotBlank()
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
