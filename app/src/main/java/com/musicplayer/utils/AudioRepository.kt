package com.musicplayer.utils

import android.content.Context
import android.provider.MediaStore
import com.musicplayer.models.Song
import java.io.File

class AudioRepository(private val context: Context) {

    fun getAllSongs(): List<Song> {
        val songList = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (it.moveToNext()) {
                val path = it.getString(pathColumn)
                
                // Filter formats and WhatsApp
                if (!isSupportedFormat(path) || path.contains("WhatsApp", ignoreCase = true)) continue

                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn)
                val duration = it.getLong(durationColumn)
                val artist = it.getString(artistColumn)
                val album = it.getString(albumColumn)
                val albumId = it.getLong(albumIdColumn)
                val folderPath = File(path).parent ?: ""

                songList.add(
                    Song(id, title, path, duration, artist, album, folderPath, albumId)
                )
            }
        }
        return songList
    }

    private fun isSupportedFormat(path: String): Boolean {
        val extensions = listOf("mp3", "wav", "m4a", "flac", "ogg")
        return extensions.any { path.endsWith(".$it", ignoreCase = true) }
    }
}
