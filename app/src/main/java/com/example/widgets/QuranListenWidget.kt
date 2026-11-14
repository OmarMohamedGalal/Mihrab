package com.example.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
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
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MihrabApplication
import com.example.core.i18n.AppLanguage
import com.example.data.quran.QuranMetadata
import com.example.data.quran.QuranRecitersCatalog
import kotlinx.coroutines.flow.first
import kotlin.math.min

val GLANCE_LISTEN_PLAYING_KEY = booleanPreferencesKey("glance_listen_playing")
val GLANCE_LISTEN_SURAH_KEY = intPreferencesKey("glance_listen_surah")

/**
 * Callback triggered when the user taps the bottom-left Pause/Stop squircle on the
 * Quran Listen widget: immediately stops the active Quran recitation and refreshes widget state.
 */
class StopQuranAudioWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val app = context.applicationContext as MihrabApplication
        val audioManager = app.container.quranAudioManager
        val activeSurah = audioManager.state.value.surahNumber
        val currentSurah = if (activeSurah > 0) {
            activeSurah
        } else {
            app.container.preferencesDataStore.preferencesFlow.first().lastReadSurah.coerceIn(1, 114)
        }

        // Stop Quran playback immediately on tap
        audioManager.stop()

        val manager = GlanceAppWidgetManager(context)
        val ids = manager.getGlanceIds(QuranListenWidget::class.java)
        if (ids.isEmpty()) {
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { state ->
                state.toMutablePreferences().apply {
                    this[GLANCE_LISTEN_PLAYING_KEY] = false
                    this[GLANCE_LISTEN_SURAH_KEY] = currentSurah
                }
            }
            QuranListenWidget().update(context, glanceId)
        } else {
            ids.forEach { id ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { state ->
                    state.toMutablePreferences().apply {
                        this[GLANCE_LISTEN_PLAYING_KEY] = false
                        this[GLANCE_LISTEN_SURAH_KEY] = currentSurah
                    }
                }
            }
            QuranListenWidget().updateAll(context)
        }
    }
}

class QuranListenWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as MihrabApplication
        val prefsFlow = app.container.preferencesDataStore.preferencesFlow
        val audioFlow = app.container.quranAudioManager.state
        val initialPrefs = prefsFlow.first()
        val initialAudio = audioFlow.value

        provideContent {
            val prefs by prefsFlow.collectAsState(initial = initialPrefs)
            val audio by audioFlow.collectAsState(initial = initialAudio)
            val glancePrefs = currentState<Preferences>()

            val activeSurahNumber = when {
                audio.surahNumber > 0 -> audio.surahNumber
                glancePrefs[GLANCE_LISTEN_SURAH_KEY] != null -> glancePrefs[GLANCE_LISTEN_SURAH_KEY]!!.coerceIn(1, 114)
                else -> prefs.lastReadSurah.coerceIn(1, 114)
            }
            val isPlaying = audio.isPlaying || audio.isBuffering || (glancePrefs[GLANCE_LISTEN_PLAYING_KEY] == true && audio.surahNumber > 0)

            val surahMeta = QuranMetadata.surahByNumber(activeSurahNumber)
            val reciterId = if (audio.surahNumber > 0) audio.reciterId else prefs.selectedReciterId
            val reciter = QuranRecitersCatalog.reciterById(reciterId)
            val isArabic = prefs.language == AppLanguage.ARABIC
            val palette = WidgetThemeResolver.resolve(context, prefs)

            val size = LocalSize.current
            val minSideDp = min(size.width.value, size.height.value).coerceAtLeast(72f)
            val bitmapSizePx = (minSideDp * 2.8f).toInt().coerceIn(200, 640)

            val discBitmap = WidgetShapeRenderer.createQuranListenDiscBitmap(
                sizePx = bitmapSizePx,
                surahNameAr = surahMeta.nameAr,
                surahNameEn = surahMeta.nameEn,
                reciterName = if (isArabic) reciter.nameAr else reciter.nameEn,
                isPlaying = isPlaying,
                colors = palette
            )

            val openSurahPlayerAction = WidgetDestinations.openQuranListenPlayerAction(
                context = context,
                surahNumber = surahMeta.number
            )
            val stopQuranAction = actionRunCallback<StopQuranAudioWidgetAction>()

            GlanceTheme {
                QuranListenWidgetContent(
                    discBitmap = discBitmap,
                    surahNameAr = surahMeta.nameAr,
                    surahNameEn = surahMeta.nameEn,
                    reciterName = if (isArabic) reciter.nameAr else reciter.nameEn,
                    isArabic = isArabic,
                    isPlaying = isPlaying,
                    minSideDp = minSideDp,
                    openPlayerAction = openSurahPlayerAction,
                    stopQuranAction = stopQuranAction
                )
            }
        }
    }

    @Composable
    private fun QuranListenWidgetContent(
        discBitmap: android.graphics.Bitmap,
        surahNameAr: String,
        surahNameEn: String,
        reciterName: String,
        isArabic: Boolean,
        isPlaying: Boolean,
        minSideDp: Float,
        openPlayerAction: Action,
        stopQuranAction: Action
    ) {
        val discBoxDp = minSideDp.dp
        val isCompact = minSideDp < 115f
        val pauseButtonSizeDp = (minSideDp * 0.30f).coerceIn(36f, 64f).dp
        val topBadgeSizeDp = (minSideDp * 0.28f).coerceIn(32f, 58f).dp
        val centerAreaPadDp = (minSideDp * 0.22f).coerceIn(18f, 46f).dp

        Box(
            contentAlignment = Alignment.Center,
            modifier = GlanceModifier.fillMaxSize()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = GlanceModifier.size(discBoxDp)
            ) {
                // Full custom-rendered M3 Expressive vinyl disc + top-right video badge + bottom-left pause/stop squircle
                Image(
                    provider = ImageProvider(discBitmap),
                    contentDescription = "Quran Listen $surahNameEn $surahNameAr",
                    contentScale = ContentScale.Fit,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(openPlayerAction)
                )

                // Center hit-region with Surah Name in the middle (opens the player of this Surah on tap)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(centerAreaPadDp)
                        .clickable(openPlayerAction)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = GlanceModifier
                            .background(ColorProvider(Color(0xFFEDE0D4)))
                            .cornerRadius(99.dp)
                            .padding(
                                horizontal = if (isCompact) 8.dp else 12.dp,
                                vertical = if (isCompact) 4.dp else 6.dp
                            )
                            .clickable(openPlayerAction)
                    ) {
                        Text(
                            text = "سورة $surahNameAr",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF211C1A)),
                                fontSize = if (isCompact) 11.sp else 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 1
                        )
                        if (!isCompact) {
                            Text(
                                text = if (isArabic) reciterName else surahNameEn,
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF5A493E)),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Top-right video/media badge hit-region (also opens the player of this Surah)
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.Top,
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = GlanceModifier
                                .size(topBadgeSizeDp)
                                .clickable(openPlayerAction)
                        ) {}
                    }
                }

                // Bottom-left Pause / Stop squircle button hit-region (on tap pause the Quran stops)
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = GlanceModifier
                                .size(pauseButtonSizeDp)
                                .cornerRadius(14.dp)
                                .clickable(stopQuranAction)
                        ) {
                            Text(
                                text = if (isPlaying) "❚❚" else "❚❚",
                                style = TextStyle(
                                    color = ColorProvider(Color.Transparent),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

class QuranListenWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuranListenWidget()
}
