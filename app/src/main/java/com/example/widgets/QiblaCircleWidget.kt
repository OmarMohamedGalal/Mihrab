package com.example.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MihrabApplication
import com.example.core.i18n.AppLanguage
import com.example.core.util.FormattingUtils
import com.example.domain.qibla.QiblaCalculator
import kotlinx.coroutines.flow.first
import kotlin.math.min
import kotlin.math.roundToInt

class QiblaCircleWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as MihrabApplication
        val prefsFlow = app.container.preferencesDataStore.preferencesFlow
        val initialPrefs = prefsFlow.first()
        val clickAction = WidgetDestinations.openAppAction(context, WidgetDestinations.QIBLA)

        provideContent {
            val prefs by prefsFlow.collectAsState(initial = initialPrefs)
            val isArabic = prefs.language == AppLanguage.ARABIC
            val bearing = QiblaCalculator.calculateQiblaBearing(prefs.latitude, prefs.longitude)
            val distanceKm = QiblaCalculator.distanceToKaabaKm(prefs.latitude, prefs.longitude)
            val distanceLabel = FormattingUtils.formatDistanceKm(distanceKm, if (isArabic) "كم" else "km")
            val title = if (isArabic) "القبلة" else "Qibla"
            val palette = WidgetThemeResolver.resolve(context, prefs)
            val size = LocalSize.current
            val minSideDp = min(size.width.value, size.height.value)
            val dialSizePx = (minSideDp * 2.6f).toInt().coerceIn(160, 560)
            val isWide = size.width.value > size.height.value * 1.45f && size.width >= 180.dp
            val isTiny = minSideDp < 95f

            val dialBitmap = WidgetShapeRenderer.createQiblaCircleBitmap(
                sizePx = dialSizePx,
                qiblaBearingDeg = bearing,
                colors = palette
            )

            GlanceTheme {
                QiblaCircleContent(
                    dialBitmap = dialBitmap,
                    bearingDeg = bearing.roundToInt(),
                    title = title,
                    city = prefs.cityName,
                    distanceLabel = distanceLabel,
                    minSideDp = minSideDp,
                    isWide = isWide,
                    isTiny = isTiny,
                    palette = palette,
                    clickAction = clickAction
                )
            }
        }
    }

    @Composable
    private fun QiblaCircleContent(
        dialBitmap: android.graphics.Bitmap,
        bearingDeg: Int,
        title: String,
        city: String,
        distanceLabel: String,
        minSideDp: Float,
        isWide: Boolean,
        isTiny: Boolean,
        palette: ResolvedWidgetPalette,
        clickAction: Action
    ) {
        if (isWide) {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(palette.surfaceContainerColor))
                    .cornerRadius(99.dp)
                    .clickable(clickAction)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dialDp = (minSideDp - 12f).coerceAtLeast(44f).dp
                Image(
                    provider = ImageProvider(dialBitmap),
                    contentDescription = "$title $bearingDeg°",
                    contentScale = ContentScale.Fit,
                    modifier = GlanceModifier.size(dialDp)
                )
                Spacer(modifier = GlanceModifier.width(12.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "$title • $bearingDeg°",
                        style = TextStyle(
                            color = ColorProvider(palette.onPrimaryContainerColor),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "$city • $distanceLabel",
                        style = TextStyle(
                            color = ColorProvider(palette.onSurfaceVariantColor),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        } else {
            val dialDp = minSideDp.coerceAtLeast(52f).dp
            Box(
                contentAlignment = Alignment.Center,
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(clickAction)
            ) {
                Image(
                    provider = ImageProvider(dialBitmap),
                    contentDescription = "$title $bearingDeg°",
                    contentScale = ContentScale.Fit,
                    modifier = GlanceModifier.size(dialDp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.padding(top = if (isTiny) 14.dp else 22.dp)
                ) {
                    Box(
                        modifier = GlanceModifier
                            .background(ColorProvider(palette.primaryColor))
                            .cornerRadius(12.dp)
                            .padding(horizontal = if (isTiny) 6.dp else 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isTiny) "$bearingDeg°" else "$title $bearingDeg°",
                            style = TextStyle(
                                color = ColorProvider(palette.onPrimaryColor),
                                fontSize = if (isTiny) 10.sp else 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

class QiblaCircleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QiblaCircleWidget()
}
