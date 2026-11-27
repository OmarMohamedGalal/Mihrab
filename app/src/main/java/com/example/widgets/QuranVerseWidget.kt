package com.example.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MihrabApplication
import com.example.core.i18n.AppLanguage
import com.example.data.azkar.AzkarRepository
import com.example.data.azkar.DailyVerseItem
import kotlinx.coroutines.flow.first

internal val GLANCE_VERSE_INDEX_KEY = intPreferencesKey("glance_verse_index")

/** Every Quran widget cycles together: tapping any of them moves all of them to the next ayah. */
internal fun quranWidgets(): List<GlanceAppWidget> = listOf(QuranVerseWidget(), QuranAyahWidget())

class ChangeQuranVerseAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val app = context.applicationContext as MihrabApplication
        val total = AzkarRepository.dailyVerses.size
        val nextIndex = app.container.preferencesDataStore.cycleQuranWidgetVerse(total)
        val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)

        quranWidgets().forEach { widget ->
            val ids = manager.getGlanceIds(widget::class.java)
            ids.forEach { id ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { state ->
                    state.toMutablePreferences().apply {
                        this[GLANCE_VERSE_INDEX_KEY] = nextIndex
                    }
                }
            }
            widget.updateAll(context)
        }
    }
}

class QuranVerseWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as MihrabApplication
        val prefsFlow = app.container.preferencesDataStore.preferencesFlow
        val initialPrefs = prefsFlow.first()

        provideContent {
            val prefs by prefsFlow.collectAsState(initial = initialPrefs)
            val glancePrefs = currentState<Preferences>()
            val verses = AzkarRepository.dailyVerses
            val rawIndex = glancePrefs[GLANCE_VERSE_INDEX_KEY] ?: prefs.quranWidgetVerseIndex
            val index = ((rawIndex % verses.size) + verses.size) % verses.size
            val verse = verses[index]
            val isArabic = prefs.language == AppLanguage.ARABIC
            val palette = WidgetThemeResolver.resolve(context, prefs)
            val size = LocalSize.current
            val isTall = size.height >= 110.dp
            val isNarrow = size.width < 185.dp

            GlanceTheme {
                QuranVerseContent(
                    verse = verse,
                    isArabic = isArabic,
                    isTall = isTall,
                    isNarrow = isNarrow,
                    palette = palette
                )
            }
        }
    }

    @Composable
    private fun QuranVerseContent(
        verse: DailyVerseItem,
        isArabic: Boolean,
        isTall: Boolean,
        isNarrow: Boolean,
        palette: ResolvedWidgetPalette
    ) {
        val tapAction = actionRunCallback<ChangeQuranVerseAction>()
        Box(
            contentAlignment = Alignment.Center,
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(palette.surfaceContainerColor))
                .cornerRadius(28.dp)
                .clickable(tapAction)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left expressive squircle badge matching the attached 1x4 pill
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier
                        .size(if (isTall) 46.dp else 40.dp)
                        .background(ColorProvider(palette.primaryColor))
                        .cornerRadius(14.dp)
                ) {
                    Text(
                        text = "آية",
                        style = TextStyle(
                            color = ColorProvider(palette.onPrimaryColor),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(10.dp))

                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = verse.arabicText,
                        style = TextStyle(
                            color = ColorProvider(palette.onPrimaryContainerColor),
                            fontSize = if (isTall) 16.sp else if (isNarrow) 13.sp else 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        ),
                        maxLines = if (isTall) 3 else 2
                    )

                    if (isTall && !isArabic) {
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = verse.translationEn,
                            style = TextStyle(
                                color = ColorProvider(palette.onSurfaceVariantColor),
                                fontSize = 12.sp
                            ),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isArabic) "${verse.referenceAr} • اضغط للتغيير" else "${verse.referenceEn} • Tap for next",
                            style = TextStyle(
                                color = ColorProvider(palette.primaryColor),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

class QuranVerseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuranVerseWidget()
}
