package com.musicplayer.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.musicplayer.models.MediaItem
import com.musicplayer.models.MediaType
import java.io.File

class MediaStoreHelper(private val context: Context) {

    private fun isWhatsAppFile(path: String): Boolean {
        return path.contains("WhatsApp", ignoreCase = true)
    }

    fun getAllAudioFiles(): List<MediaItem> {
        val audioList = mutableListOf<MediaItem>()
        // ... (projection and query same)
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED
        )

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val path = it.getString(pathColumn)
                val duration = it.getLong(durationColumn)
                val size = it.getLong(sizeColumn)
                val dateAdded = it.getLong(dateAddedColumn)
                val dateModified = it.getLong(dateModifiedColumn)

                val folderPath = File(path).parent ?: ""
                if (isWhatsAppFile(path)) continue

                val uri = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                audioList.add(
                    MediaItem(
                        id = id,
                        name = name,
                        path = path,
                        uri = uri,
                        duration = duration,
                        size = size,
                        dateAdded = dateAdded,
                        dateModified = dateModified,
                        folderPath = folderPath,
                        type = MediaType.AUDIO
                    )
                )
            }
        }

        return audioList
    }

    fun getAllVideoFiles(): List<MediaItem> {
        val videoList = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED
        )

        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val dateModifiedColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val path = it.getString(pathColumn)
                val duration = it.getLong(durationColumn)
                val size = it.getLong(sizeColumn)
                val dateAdded = it.getLong(dateAddedColumn)
                val dateModified = it.getLong(dateModifiedColumn)

                val folderPath = File(path).parent ?: ""
                if (isWhatsAppFile(path)) continue

                val uri = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                videoList.add(
                    MediaItem(
                        id = id,
                        name = name,
                        path = path,
                        uri = uri,
                        duration = duration,
                        size = size,
                        dateAdded = dateAdded,
                        dateModified = dateModified,
                        folderPath = folderPath,
                        type = MediaType.VIDEO
                    )
                )
            }
        }

        return videoList
    }

    fun getFolders(mediaList: List<MediaItem>): Map<String, List<MediaItem>> {
        return mediaList.groupBy { it.folderPath }
    }
}