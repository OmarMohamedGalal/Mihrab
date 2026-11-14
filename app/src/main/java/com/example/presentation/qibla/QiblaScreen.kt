package com.example.presentation.qibla

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.design.ScallopedStarShape
import com.example.core.i18n.LocalStrings
import com.example.core.util.FormattingUtils
import com.example.domain.qibla.QiblaCalculator
import com.example.presentation.MainUiState
import com.example.sensors.CompassAccuracy
import com.example.ui.theme.EmeraldAligned
import com.example.ui.theme.RobotoMonoFamily
import com.example.ui.theme.RobotoSerifFamily
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QiblaScreen(
    uiState: MainUiState,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedMode by remember { mutableIntStateOf(0) } // 0 = Compass, 1 = Map
    var showCalibrationDialog by remember { mutableStateOf(false) }

    val compass = uiState.compassState
    val qiblaBearing = uiState.qiblaBearing
    val heading = compass.headingDegrees.toDouble()
    val signedDiff = QiblaCalculator.shortestSignedAngleDiff(heading, qiblaBearing)
    val isAligned = compass.sensorAvailable && abs(signedDiff) <= 4.0

    // Trigger subtle haptic feedback once whenever alignment is achieved
    var wasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !wasAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        wasAligned = isAligned
    }

    // Continuous shortest-path rotation tracking to eliminate 0/360 spin jumps
    var continuousNeedleAngle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(signedDiff) {
        val target = signedDiff.toFloat()
        val delta = ((target - continuousNeedleAngle + 540f) % 360f) - 180f
        continuousNeedleAngle += delta
    }

    var continuousDialRotation by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(heading) {
        val target = (-heading).toFloat()
        val delta = ((target - continuousDialRotation + 540f) % 360f) - 180f
        continuousDialRotation += delta
    }

    val animatedNeedle by animateFloatAsState(
        targetValue = continuousNeedleAngle,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "qibla_needle"
    )
    val animatedDial by animateFloatAsState(
        targetValue = continuousDialRotation,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "compass_dial"
    )

    val ringColor by animateColorAsState(
        targetValue = if (isAligned) EmeraldAligned else MaterialTheme.colorScheme.primary,
        label = "qibla_ring_color"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.qiblaTitle,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = "${uiState.prefs.cityName} • ${strings.qiblaSubtitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedMode == 0,
                        onClick = { selectedMode = 0 },
                        icon = { Icon(Icons.Filled.Explore, contentDescription = null) },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                        modifier = Modifier.testTag("qibla_tab_compass")
                    ) {
                        Text(strings.compassView)
                    }
                    SegmentedButton(
                        selected = selectedMode == 1,
                        onClick = { selectedMode = 1 },
                        icon = { Icon(Icons.Filled.Map, contentDescription = null) },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                        modifier = Modifier.testTag("qibla_tab_map")
                    ) {
                        Text(strings.mapView)
                    }
                }
            }

            if (selectedMode == 0) {
                if (!compass.sensorAvailable) {
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.WarningAmber,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = strings.sensorUnavailable,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // Alignment Status Pill & Sensor Accuracy
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusText = when {
                            !compass.sensorAvailable -> strings.qiblaDirectionLabel + " ${FormattingUtils.formatDegrees(qiblaBearing)}"
                            isAligned -> strings.facingQibla
                            signedDiff > 0 -> "${strings.turnRight} (${abs(signedDiff).roundToInt()}°)"
                            else -> "${strings.turnLeft} (${abs(signedDiff).roundToInt()}°)"
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (isAligned) EmeraldAligned else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.testTag("qibla_status_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isAligned) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isAligned) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = { showCalibrationDialog = true },
                            shape = CircleShape,
                            modifier = Modifier.testTag("calibrate_compass_button")
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.calibrateCompass)
                        }
                    }
                }

                // Large Expressive Compass Dial
                item {
                    val primaryCol = MaterialTheme.colorScheme.primary
                    val secondaryCol = MaterialTheme.colorScheme.secondary
                    val surfaceCol = MaterialTheme.colorScheme.surfaceContainerHigh
                    val onSurfaceCol = MaterialTheme.colorScheme.onSurface
                    val outlineCol = MaterialTheme.colorScheme.outlineVariant

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.84f)
                            .aspectRatio(1f)
                            .testTag("qibla_compass_dial"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val radius = size.minDimension / 2f - 14f

                            // Outer expressive track
                            drawCircle(
                                color = surfaceCol,
                                radius = radius,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = ringColor,
                                radius = radius,
                                center = Offset(cx, cy),
                                style = Stroke(width = if (isAligned) 12f else 7f)
                            )

                            // Rotating compass rose (True North & tick marks)
                            rotate(degrees = animatedDial, pivot = Offset(cx, cy)) {
                                for (deg in 0 until 360 step 10) {
                                    val isMajor = deg % 30 == 0
                                    val rad = Math.toRadians(deg.toDouble() - 90.0)
                                    val innerR = radius * if (isMajor) 0.80f else 0.87f
                                    val outerR = radius * 0.93f
                                    drawLine(
                                        color = if (deg == 0) Color(0xFFE05252) else outlineCol,
                                        start = Offset(cx + innerR * cos(rad).toFloat(), cy + innerR * sin(rad).toFloat()),
                                        end = Offset(cx + outerR * cos(rad).toFloat(), cy + outerR * sin(rad).toFloat()),
                                        strokeWidth = if (deg == 0) 7f else if (isMajor) 4f else 2f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            // Rotating Qibla Pointer & Kaaba emblem
                            rotate(degrees = animatedNeedle, pivot = Offset(cx, cy)) {
                                val pointerPath = Path().apply {
                                    moveTo(cx, cy - radius * 0.72f)
                                    lineTo(cx - 22f, cy - 10f)
                                    lineTo(cx, cy + 28f)
                                    lineTo(cx + 22f, cy - 10f)
                                    close()
                                }
                                drawPath(
                                    path = pointerPath,
                                    color = if (isAligned) EmeraldAligned else primaryCol
                                )

                                // Kaaba golden-rimmed square indicator at tip
                                val kaabaCenterY = cy - radius * 0.77f
                                drawCircle(
                                    color = if (isAligned) EmeraldAligned else secondaryCol,
                                    radius = 22f,
                                    center = Offset(cx, kaabaCenterY)
                                )
                                drawRect(
                                    color = Color(0xFF1A1510),
                                    topLeft = Offset(cx - 10f, kaabaCenterY - 10f),
                                    size = androidx.compose.ui.geometry.Size(20f, 20f)
                                )
                                drawLine(
                                    color = Color(0xFFE7BD65),
                                    start = Offset(cx - 10f, kaabaCenterY - 3f),
                                    end = Offset(cx + 10f, kaabaCenterY - 3f),
                                    strokeWidth = 3f
                                )
                            }

                            // Center hub
                            drawCircle(
                                color = onSurfaceCol.copy(alpha = 0.12f),
                                radius = 24f,
                                center = Offset(cx, cy)
                            )
                        }

                        Surface(
                            shape = ScallopedStarShape(lobes = 8, depthFraction = 0.1f),
                            color = if (isAligned) EmeraldAligned else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "🕋",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Sensor accuracy badge
                item {
                    val accText = when (compass.accuracy) {
                        CompassAccuracy.HIGH -> strings.sensorAccuracyHigh
                        CompassAccuracy.MEDIUM -> strings.sensorAccuracyMedium
                        CompassAccuracy.LOW, CompassAccuracy.UNRELIABLE -> strings.sensorAccuracyLow
                    }
                    Text(
                        text = accText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (compass.accuracy == CompassAccuracy.LOW || compass.accuracy == CompassAccuracy.UNRELIABLE) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            } else {
                // Great-Circle Geodesic Map Canvas View
                item {
                    GreatCircleMapCard(
                        userLat = uiState.prefs.latitude,
                        userLon = uiState.prefs.longitude,
                        cityName = uiState.prefs.cityName,
                        qiblaBearing = qiblaBearing
                    )
                }
            }

            // Metrics Card: Qibla Bearing, Current Heading, Distance to Kaaba
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricColumn(
                            label = strings.qiblaDirectionLabel,
                            value = FormattingUtils.formatDegrees(qiblaBearing)
                        )
                        MetricColumn(
                            label = strings.headingLabel,
                            value = if (compass.sensorAvailable) FormattingUtils.formatDegrees(heading) else "—"
                        )
                        MetricColumn(
                            label = strings.distanceToKaabaLabel,
                            value = FormattingUtils.formatDistanceKm(uiState.distanceToKaabaKm, strings.kmUnit)
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val uri = Uri.parse("geo:${QiblaCalculator.KAABA_LAT},${QiblaCalculator.KAABA_LON}?q=${QiblaCalculator.KAABA_LAT},${QiblaCalculator.KAABA_LON}(Kaaba)")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        runCatching { context.startActivity(intent) }
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("open_kaaba_maps_button")
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.openInMaps)
                }
            }
        }
    }

    if (showCalibrationDialog) {
        AlertDialog(
            onDismissRequest = { showCalibrationDialog = false },
            title = { Text(strings.calibrateCompass) },
            text = { Text(strings.calibrationInstructions) },
            confirmButton = {
                TextButton(onClick = { showCalibrationDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = RobotoMonoFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun GreatCircleMapCard(
    userLat: Double,
    userLon: Double,
    cityName: String,
    qiblaBearing: Double
) {
    val points = remember(userLat, userLon) {
        QiblaCalculator.greatCirclePoints(userLat, userLon, steps = 48)
    }
    val primaryCol = MaterialTheme.colorScheme.primary
    val tertiaryCol = MaterialTheme.colorScheme.tertiary
    val gridCol = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Equirectangular map grid
                for (i in 1..5) {
                    val y = h * i / 6f
                    drawLine(gridCol, Offset(0f, y), Offset(w, y), strokeWidth = 1.5f)
                }
                for (i in 1..7) {
                    val x = w * i / 8f
                    drawLine(gridCol, Offset(x, 0f), Offset(x, h), strokeWidth = 1.5f)
                }

                fun project(lat: Double, lon: Double): Offset {
                    val x = ((lon + 180.0) / 360.0 * w).toFloat()
                    val y = ((90.0 - lat) / 180.0 * h).toFloat()
                    return Offset(x, y)
                }

                val path = Path()
                points.forEachIndexed { idx, (lat, lon) ->
                    val pt = project(lat, lon)
                    if (idx == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                }

                drawPath(
                    path = path,
                    color = primaryCol,
                    style = Stroke(
                        width = 6f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                    )
                )

                val userPt = project(userLat, userLon)
                val kaabaPt = project(QiblaCalculator.KAABA_LAT, QiblaCalculator.KAABA_LON)

                drawCircle(color = tertiaryCol, radius = 12f, center = userPt)
                drawCircle(color = EmeraldAligned, radius = 14f, center = kaabaPt)
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.9f),
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    text = "$cityName → Makkah (${FormattingUtils.formatDegrees(qiblaBearing)})",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = RobotoMonoFamily,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
