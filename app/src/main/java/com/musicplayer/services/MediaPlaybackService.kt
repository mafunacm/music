package com.musicplayer.services

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.musicplayer.playback.PlayerManager
import androidx.media3.session.MediaNotification
import androidx.media3.session.CommandButton
import com.google.common.collect.ImmutableList
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaStyleNotificationHelper

class MediaPlaybackService : MediaSessionService() {
    
    private lateinit var playerManager: PlayerManager

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        playerManager = PlayerManager.getInstance(this)
        playerManager.setupMediaSession(this)

        setMediaNotificationProvider(object : MediaNotification.Provider {
            override fun createNotification(
                session: MediaSession,
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {
                val player = session.player
                val metadata = player.mediaMetadata
                val channelId = "playback_channel"
                
                val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val channel = android.app.NotificationChannel(channelId, "Playback", android.app.NotificationManager.IMPORTANCE_LOW)
                    notificationManager.createNotificationChannel(channel)
                }

                val notificationBuilder = NotificationCompat.Builder(this@MediaPlaybackService, channelId)
                    .setContentTitle(metadata.title ?: "Music Player")
                    .setContentText(metadata.artist ?: "Unknown Artist")
                    .setSmallIcon(androidx.media3.session.R.drawable.media3_notification_small_icon)
                    .setStyle(MediaStyleNotificationHelper.MediaStyle(session))
                    .setColor(0xFF2D1B44.toInt()) // Set notification color
                    .setColorized(true)
                    .setOngoing(player.isPlaying)

                // Add standard playback controls
                notificationBuilder.addAction(actionFactory.createMediaAction(session, androidx.core.graphics.drawable.IconCompat.createWithResource(this@MediaPlaybackService, android.R.drawable.ic_media_previous), "Previous", androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM))
                val playPauseIcon = if (player.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                notificationBuilder.addAction(actionFactory.createMediaAction(session, androidx.core.graphics.drawable.IconCompat.createWithResource(this@MediaPlaybackService, playPauseIcon), "Play/Pause", androidx.media3.common.Player.COMMAND_PLAY_PAUSE))
                notificationBuilder.addAction(actionFactory.createMediaAction(session, androidx.core.graphics.drawable.IconCompat.createWithResource(this@MediaPlaybackService, android.R.drawable.ic_media_next), "Next", androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM))

                return MediaNotification(1001, notificationBuilder.build())
            }

            override fun handleCustomCommand(session: MediaSession, action: String, extras: android.os.Bundle): Boolean = false
        })
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return playerManager.getMediaSession()
    }

    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = playerManager.getPlayer()
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        playerManager.release()
        super.onDestroy()
    }
}
