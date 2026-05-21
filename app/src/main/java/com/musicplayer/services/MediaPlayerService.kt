package com.musicplayer.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.musicplayer.models.MediaItem
import com.musicplayer.utils.PreferencesManager
import java.io.IOException
import kotlin.random.Random

import android.util.Log

class MediaPlayerService : Service() {
    private val TAG = "MediaPlayerService"
    // ...
    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlaylist: MutableList<MediaItem> = mutableListOf()
    private var currentIndex = -1
    private var currentItem: MediaItem? = null
    private var loopMode = LoopMode.NONE
    private var shuffleEnabled = false
    private lateinit var preferencesManager: PreferencesManager

    inner class LocalBinder : Binder() {
        fun getService(): MediaPlayerService = this@MediaPlayerService
    }

    enum class LoopMode {
        NONE, ONE, ALL
    }

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        loopMode = preferencesManager.getLoopMode()
        shuffleEnabled = preferencesManager.isShuffleEnabled()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun playMediaItem(item: MediaItem) {
        currentItem = item
        currentIndex = currentPlaylist.indexOf(item)
        item.playCount++
        item.lastPlayed = System.currentTimeMillis()
        preferencesManager.updatePlayCount(item)
        initializePlayer(item)
    }

    fun playPlaylist(playlist: List<MediaItem>, startIndex: Int = 0) {
        currentPlaylist.clear()
        currentPlaylist.addAll(playlist)
        currentIndex = startIndex
        if (shuffleEnabled) {
            shufflePlaylist()
        }
        if (currentPlaylist.isNotEmpty()) {
            playMediaItem(currentPlaylist[currentIndex])
        }
    }

    private fun initializePlayer(item: MediaItem) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@MediaPlayerService, item.uri)
                prepare()
                start()
                setOnCompletionListener {
                    onPlaybackComplete()
                }
                setOnErrorListener { _, what, extra ->
                    true
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun onPlaybackComplete() {
        when (loopMode) {
            LoopMode.ONE -> {
                // Replay current song
                currentItem?.let { initializePlayer(it) }
            }
            LoopMode.ALL -> {
                playNext()
            }
            LoopMode.NONE -> {
                if (currentIndex < currentPlaylist.size - 1) {
                    playNext()
                } else {
                    // End of playlist
                }
            }
        }
    }

    fun playNext() {
        if (currentPlaylist.isNotEmpty()) {
            var nextIndex = currentIndex + 1
            if (nextIndex >= currentPlaylist.size) {
                if (loopMode == LoopMode.ALL) {
                    nextIndex = 0
                } else {
                    return
                }
            }
            currentIndex = nextIndex
            playMediaItem(currentPlaylist[currentIndex])
        }
    }

    fun playPrevious() {
        if (currentPlaylist.isNotEmpty()) {
            var prevIndex = currentIndex - 1
            if (prevIndex < 0) {
                if (loopMode == LoopMode.ALL) {
                    prevIndex = currentPlaylist.size - 1
                } else {
                    return
                }
            }
            currentIndex = prevIndex
            playMediaItem(currentPlaylist[currentIndex])
        }
    }

    fun pauseMedia() {
        mediaPlayer?.pause()
    }

    fun resumeMedia() {
        mediaPlayer?.start()
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun skipForward10() {
        skip(10000)
    }

    fun skipForward5() {
        skip(5000)
    }

    fun skipBackward10() {
        skip(-10000)
    }

    fun skipBackward5() {
        skip(-5000)
    }

    fun skip(milliseconds: Int) {
        mediaPlayer?.let {
            val newPosition = it.currentPosition + milliseconds
            if (newPosition in 0..it.duration) {
                it.seekTo(newPosition)
            }
        }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun getCurrentMediaItem(): MediaItem? = currentItem

    fun cycleLoopMode() {
        loopMode = when (loopMode) {
            LoopMode.NONE -> LoopMode.ONE
            LoopMode.ONE -> LoopMode.ALL
            LoopMode.ALL -> LoopMode.NONE
        }
        preferencesManager.saveLoopMode(loopMode)
    }

    fun getLoopMode(): LoopMode = loopMode

    fun toggleShuffle() {
        shuffleEnabled = !shuffleEnabled
        preferencesManager.saveShuffleEnabled(shuffleEnabled)
        if (shuffleEnabled) {
            shufflePlaylist()
        }
    }

    private fun shufflePlaylist() {
        val currentItem = currentPlaylist.getOrNull(currentIndex)
        currentPlaylist.shuffle(Random(System.currentTimeMillis()))
        currentIndex = currentPlaylist.indexOf(currentItem)
        if (currentIndex == -1 && currentPlaylist.isNotEmpty()) {
            currentIndex = 0
        }
    }

    fun isShuffleEnabled(): Boolean = shuffleEnabled

    fun getPlaylist(): List<MediaItem> = currentPlaylist

    fun setPlaylist(playlist: List<MediaItem>, startIndex: Int) {
        playPlaylist(playlist, startIndex)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }
}