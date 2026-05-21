package com.musicplayer.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Long,
    val title: String,
    val path: String,
    val duration: Long,
    val artist: String?,
    val album: String?,
    val folderPath: String
) : Parcelable
