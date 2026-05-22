package com.musicplayer.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.musicplayer.models.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@UnstableApi
class PlayerManager private constructor(private val context: Context) {
    val audioProcessor = UnifiedAudioProcessor()

    private val player = ExoPlayer.Builder(context)
        .setRenderersFactory(object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink? {
                return DefaultAudioSink.Builder(context)
                    .setAudioProcessors(arrayOf(audioProcessor))
                    .build()
            }
        })
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        .build()

    private var mediaSession: MediaSession? = null
    
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist = _currentPlaylist.asStateFlow()

    private val _shuffleModeEnabled = MutableStateFlow(false)
    val shuffleModeEnabled = _shuffleModeEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_ALL)
    val repeatMode = _repeatMode.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: PlayerManager? = null

        fun getInstance(context: Context): PlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlayerManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = player.currentMediaItemIndex
                _currentIndex.value = index
                if (index in _currentPlaylist.value.indices) {
                    _currentSong.value = _currentPlaylist.value[index]
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _shuffleModeEnabled.value = shuffleModeEnabled
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = repeatMode
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("PlayerManager", "Player error: ${error.message}", error)
                // If there's an error, try to play the next item
                if (player.hasNextMediaItem()) {
                    Log.d("PlayerManager", "Error occurred, skipping to next track")
                    player.seekToNext()
                    player.prepare()
                    player.play()
                } else {
                    Log.d("PlayerManager", "Error occurred and no next track, stopping")
                    player.stop()
                }
            }
        })
    }

    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        Log.d("PlayerManager", "playPlaylist called with ${songs.size} songs, startIndex: $startIndex")
        
        // Start service to ensure playback continues and notification shows
        context.startService(Intent(context, com.musicplayer.services.MediaPlaybackService::class.java))

        _currentPlaylist.value = songs.toList()
        val mediaItems = songs.map { song ->
            val artworkUri = if (song.albumId != -1L) {
                Uri.parse("content://media/external/audio/albumart/${song.albumId}")
            } else {
                Uri.parse("file://" + song.path)
            }
            
            MediaItem.Builder()
                .setMediaId(song.path)
                .setUri(song.path)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(artworkUri)
                        .build()
                )
                .build()
        }
        player.setMediaItems(mediaItems)
        player.seekTo(startIndex, 0)
        player.prepare()
        player.play()
    }

    fun setupMediaSession(context: Context) {
        if (mediaSession == null) {
            val intent = Intent(context, com.musicplayer.MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            mediaSession = MediaSession.Builder(context, player)
                .setSessionActivity(pendingIntent)
                .build()
        }
    }

    fun getMediaSession(): MediaSession? = mediaSession

    fun playNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNext()
            player.play()
        }
    }

    fun playPrevious() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPrevious()
            player.play()
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun pause() {
        player.pause()
    }

    fun toggleShuffle() {
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    fun toggleRepeatMode() {
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun seekForward() {
        player.seekTo(player.currentPosition + 10000)
    }

    fun seekBackward() {
        player.seekTo(player.currentPosition - 10000)
    }
    
    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }
    
    fun getCurrentPosition() = player.currentPosition
    fun getDuration() = player.duration
    fun getPlayer() = player

    fun release() {
        mediaSession?.release()
        mediaSession = null
        player.release()
        synchronized(PlayerManager::class.java) {
            if (INSTANCE === this) {
                INSTANCE = null
            }
        }
    }
}
