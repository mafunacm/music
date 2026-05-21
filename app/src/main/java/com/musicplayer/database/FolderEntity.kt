package com.musicplayer.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.musicplayer.models.FolderItem

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val path: String,
    val name: String,
    val mediaCount: Int,
    val hasAudio: Boolean,
    val hasVideo: Boolean
) {
    fun toFolderItem() = FolderItem(name, path, mediaCount, hasAudio, hasVideo)

    companion object {
        fun fromFolderItem(item: FolderItem) = FolderEntity(
            path = item.path,
            name = item.name,
            mediaCount = item.mediaCount,
            hasAudio = item.hasAudio,
            hasVideo = item.hasVideo
        )
    }
}
