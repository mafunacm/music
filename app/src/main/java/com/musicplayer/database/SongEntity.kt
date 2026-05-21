package com.musicplayer.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.musicplayer.models.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val path: String,
    val id: Long,
    val title: String,
    val duration: Long,
    val artist: String?,
    val album: String?,
    val folderPath: String
) {
    fun toSong() = Song(id, title, path, duration, artist, album, folderPath)

    companion object {
        fun fromSong(song: Song) = SongEntity(
            path = song.path,
            id = song.id,
            title = song.title,
            duration = song.duration,
            artist = song.artist,
            album = song.album,
            folderPath = song.folderPath
        )
    }
}
