package com.musicplayer.models

data class FolderItem(
    val name: String,
    val path: String,
    val mediaCount: Int,
    val hasAudio: Boolean = false,
    val hasVideo: Boolean = false
)

