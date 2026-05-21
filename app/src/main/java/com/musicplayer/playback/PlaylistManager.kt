package com.musicplayer.playback

import android.content.Context
import com.musicplayer.models.Song
import java.io.File

class PlaylistManager(private val context: Context) {
    private var currentPlaylist = mutableListOf<Song>()

    private val prefs = context.getSharedPreferences("music_player_prefs", Context.MODE_PRIVATE)

    fun createPlaylistFromFolder(folderName: String, folderSongs: List<Song>) {
        currentPlaylist.clear()
        setPlaylistName(folderName)
        val sortedSongs = folderSongs.sortedBy { File(it.path).name.lowercase() }
        currentPlaylist.addAll(sortedSongs)
    }

    fun setPlaylistName(name: String) {
        prefs.edit().putString("last_playlist_name", name).apply()
    }

    fun getPlaylistName(): String = prefs.getString("last_playlist_name", "Music") ?: "Music"

    // Persistent storage for favorites, recents, and custom playlists is now handled in MainViewModel via Room Database
}
