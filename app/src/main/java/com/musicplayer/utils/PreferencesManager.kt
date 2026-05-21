package com.musicplayer.utils

import android.content.Context
import android.content.SharedPreferences
import com.musicplayer.models.MediaItem
import com.musicplayer.services.MediaPlayerService

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("music_player", Context.MODE_PRIVATE)

    fun saveLoopMode(mode: MediaPlayerService.LoopMode) {
        prefs.edit().putString("loop_mode", mode.name).apply()
    }

    fun getLoopMode(): MediaPlayerService.LoopMode {
        val modeName = prefs.getString("loop_mode", MediaPlayerService.LoopMode.NONE.name) ?: MediaPlayerService.LoopMode.NONE.name
        return try {
            MediaPlayerService.LoopMode.valueOf(modeName)
        } catch (e: IllegalArgumentException) {
            MediaPlayerService.LoopMode.NONE
        }
    }

    fun saveShuffleEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("shuffle_enabled", enabled).apply()
    }

    fun isShuffleEnabled(): Boolean {
        return prefs.getBoolean("shuffle_enabled", false)
    }

    fun saveSortType(sortType: String) {
        prefs.edit().putString("sort_type", sortType).apply()
    }

    fun getSortType(): String {
        return prefs.getString("sort_type", "NAME") ?: "NAME"
    }

    fun updatePlayCount(item: MediaItem) {
        val playCount = prefs.getInt("play_count_${item.id}", 0)
        prefs.edit().putInt("play_count_${item.id}", playCount + 1).apply()
        prefs.edit().putLong("last_played_${item.id}", System.currentTimeMillis()).apply()
    }

    fun getPlayCount(item: MediaItem): Int {
        return prefs.getInt("play_count_${item.id}", 0)
    }

    fun getLastPlayed(item: MediaItem): Long {
        return prefs.getLong("last_played_${item.id}", 0)
    }
}