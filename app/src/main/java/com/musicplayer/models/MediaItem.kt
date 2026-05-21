package com.musicplayer.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaItem(
    val id: Long,
    val name: String,
    val path: String,
    val uri: Uri,
    val duration: Long,
    val size: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val folderPath: String,
    val type: MediaType,
    var playCount: Int = 0,
    var lastPlayed: Long = 0
) : Parcelable {
    fun toSong(): Song {
        return Song(
            id = id,
            title = name,
            path = path,
            duration = duration,
            artist = "Video",
            album = null,
            folderPath = folderPath
        )
    }
}

enum class MediaType {
    AUDIO, VIDEO, FOLDER
}
