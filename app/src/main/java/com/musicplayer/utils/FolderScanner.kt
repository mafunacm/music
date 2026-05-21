package com.musicplayer.utils

import com.musicplayer.models.FolderItem
import com.musicplayer.models.MediaItem
import com.musicplayer.models.MediaType
import com.musicplayer.models.Song
import java.io.File

class FolderScanner {

    fun getGroupedSongs(songs: List<Song>): Map<String, List<Song>> {
        return songs.groupBy { it.folderPath }
    }

    fun getGroupedVideos(videos: List<MediaItem>): Map<String, List<MediaItem>> {
        return videos.groupBy { it.folderPath }
    }

    /**
     * Gets immediate subfolders of [currentPath] that contain media.
     */
    fun getSubFolders(
        currentPath: String,
        mediaType: MediaType,
        folderCounts: Map<String, Int>
    ): List<FolderItem> {
        val root = File(currentPath)
        if (!root.exists() || !root.isDirectory) return emptyList()

        val subfolders = mutableListOf<FolderItem>()
        
        root.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                val path = file.absolutePath
                val count = getMediaCountInHierarchy(path, folderCounts)
                if (count > 0) {
                    subfolders.add(
                        FolderItem(
                            name = file.name,
                            path = path,
                            mediaCount = count,
                            hasAudio = mediaType == MediaType.AUDIO,
                            hasVideo = mediaType == MediaType.VIDEO
                        )
                    )
                }
            }
        }

        return subfolders.sortedBy { it.name.lowercase() }
    }

    /**
     * Gets the "root" folders for a given media type.
     */
    fun getRootFolders(folderCounts: Map<String, Int>, mediaType: MediaType): List<FolderItem> {
        val allPaths = folderCounts.keys.toList().sortedBy { it.length }
        val roots = mutableListOf<String>()
        
        for (path in allPaths) {
            if (roots.none { path.startsWith(it + File.separator) }) {
                roots.add(path)
            }
        }
        
        return roots.map { path ->
            val file = File(path)
            FolderItem(
                name = file.name,
                path = path,
                mediaCount = getMediaCountInHierarchy(path, folderCounts),
                hasAudio = mediaType == MediaType.AUDIO,
                hasVideo = mediaType == MediaType.VIDEO
            )
        }.sortedBy { it.name.lowercase() }
    }

    private fun getMediaCountInHierarchy(path: String, folderCounts: Map<String, Int>): Int {
        return folderCounts.filter { it.key == path || it.key.startsWith(path + File.separator) }
            .values.sum()
    }
}
