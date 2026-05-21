package com.musicplayer.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val name: String
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistName", "songPath"])
data class PlaylistSongEntity(
    val playlistName: String,
    val songPath: String
)
