package com.example.services

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import com.example.data.preferences.AdhanSoundStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Resolves and previews native Android phone notification, alarm, and ringtone sounds
 * via [RingtoneManager] without generating custom synthesized audio files.
 */
class AdhanAudioManager(private val context: Context) {

    private var activeRingtone: Ringtone? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun resolveSystemSoundUri(style: AdhanSoundStyle): Uri? {
        return when (style) {
            AdhanSoundStyle.MAQAM_HIJAZ ->
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: Settings.System.DEFAULT_NOTIFICATION_URI
            AdhanSoundStyle.MAKKAH_HARAM ->
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: Settings.System.DEFAULT_ALARM_ALERT_URI
            AdhanSoundStyle.MADINAH_TONE ->
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: Settings.System.DEFAULT_RINGTONE_URI
            AdhanSoundStyle.SILENT -> null
        }
    }

    fun play(style: AdhanSoundStyle) {
        if (style == AdhanSoundStyle.SILENT) return
        stop()
        val uri = resolveSystemSoundUri(style) ?: return
        runCatching {
            val ringtone = RingtoneManager.getRingtone(context, uri) ?: return
            ringtone.audioAttributes = AudioAttributes.Builder()
                .setUsage(
                    if (style == AdhanSoundStyle.MAKKAH_HARAM) {
                        AudioAttributes.USAGE_ALARM
                    } else {
                        AudioAttributes.USAGE_NOTIFICATION
                    }
                )
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone.play()
            activeRingtone = ringtone
            _isPlaying.value = true
        }
    }

    fun stop() {
        runCatching {
            activeRingtone?.let {
                if (it.isPlaying) it.stop()
            }
        }
        activeRingtone = null
        _isPlaying.value = false
    }
}
