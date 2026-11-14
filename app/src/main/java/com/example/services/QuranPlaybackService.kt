package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.MihrabApplication
import com.example.R
import com.example.data.quran.QuranMetadata
import com.example.data.quran.QuranRecitersCatalog

/**
 * Foreground MediaPlayback Service that keeps Quran recitation playing when the screen is off
 * or the app is in the background, and provides lock-screen transport controls.
 */
class QuranPlaybackService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = applicationContext as? MihrabApplication
        val audioManager = app?.container?.quranAudioManager

        when (intent?.action) {
            ACTION_TOGGLE_PLAY -> audioManager?.togglePlayPause()
            ACTION_NEXT -> audioManager?.nextTrack()
            ACTION_PREV -> audioManager?.previousTrack()
            ACTION_STOP -> {
                audioManager?.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val state = audioManager?.state?.value ?: QuranAudioState()
        if (state.surahNumber <= 0 && !state.isPlaying && !state.isBuffering) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildMediaNotification(state)
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        }
        return START_STICKY
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Quran Recitation Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background and screen-off Quran audio playback controls"
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(channel)
    }

    private fun buildMediaNotification(state: QuranAudioState): Notification {
        val surah = QuranMetadata.surahByNumber(state.surahNumber.coerceIn(1, 114))
        val reciter = QuranRecitersCatalog.reciterById(state.reciterId)
        val title = "سورة ${surah.nameAr} • ${surah.nameEn}"
        val subtitle = "${reciter.nameAr} • ${reciter.nameEn}" +
            if (state.isOfflinePlayback) " (Offline)" else ""

        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevPending = serviceActionPendingIntent(ACTION_PREV, 101)
        val playPausePending = serviceActionPendingIntent(ACTION_TOGGLE_PLAY, 102)
        val nextPending = serviceActionPendingIntent(ACTION_NEXT, 103)
        val stopPending = serviceActionPendingIntent(ACTION_STOP, 104)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_art)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSubText(if (state.isPlaying) "Playing" else if (state.isBuffering) "Loading..." else "Paused")
            .setContentIntent(openIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(state.isPlaying || state.isBuffering)
            .setSilent(true)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPending)
            .addAction(
                if (state.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (state.isPlaying) "Pause" else "Play",
                playPausePending
            )
            .addAction(android.R.drawable.ic_media_next, "Next", nextPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPending)
            .build()
    }

    private fun serviceActionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, QuranPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_ID = "mihrab_quran_playback_channel"
        const val NOTIFICATION_ID = 8820
        const val ACTION_SYNC = "com.example.quran.ACTION_SYNC"
        const val ACTION_TOGGLE_PLAY = "com.example.quran.ACTION_TOGGLE_PLAY"
        const val ACTION_NEXT = "com.example.quran.ACTION_NEXT"
        const val ACTION_PREV = "com.example.quran.ACTION_PREV"
        const val ACTION_STOP = "com.example.quran.ACTION_STOP"

        fun sync(context: Context) {
            runCatching {
                val intent = Intent(context, QuranPlaybackService::class.java).apply {
                    action = ACTION_SYNC
                }
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            runCatching {
                context.stopService(Intent(context, QuranPlaybackService::class.java))
            }
        }
    }
}
