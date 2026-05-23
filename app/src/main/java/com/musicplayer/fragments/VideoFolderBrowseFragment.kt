package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.util.UnstableApi
import com.musicplayer.MainActivity
import com.musicplayer.models.FolderItem
import com.musicplayer.ui.MainViewModel
import com.musicplayer.ui.components.TrackRow
import com.musicplayer.ui.theme.MusicPlayerTheme
import java.io.File

@UnstableApi
class VideoFolderBrowseFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var currentPath by mutableStateOf<String?>(null)

    companion object {
        fun newInstance() = VideoFolderBrowseFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MusicPlayerTheme {
                    VideoFolderContent()
                }
            }
        }
    }

    @Composable
    private fun VideoFolderContent() {
        val subfolders = viewModel.getFoldersForPath(currentPath, com.musicplayer.models.MediaType.VIDEO)
        val videos = if (currentPath != null) {
            viewModel.getMediaForPath(currentPath!!, com.musicplayer.models.MediaType.VIDEO)
        } else {
            emptyList()
        }
        val favoriteIds by viewModel.favoriteIds.collectAsState()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(subfolders) { folder ->
                FolderRow(folder) {
                    currentPath = folder.path
                }
            }
            items(videos) { song ->
                // Using TrackRow for videos too, but with a video icon
                TrackRow(
                    song = song,
                    isActive = false,
                    isPlaying = false,
                    isFavorite = favoriteIds.contains(song.id),
                    onSelect = {
                        val videosInFolder = viewModel.videoFolders.value[currentPath] ?: emptyList()
                        val mediaItem = videosInFolder.find { it.id == song.id }
                        if (mediaItem != null) {
                            (activity as? MainActivity)?.playMediaItem(mediaItem, videosInFolder)
                        }
                    },
                    onFavoriteToggle = {
                        viewModel.toggleFavorite(song.id)
                    },
                    onAddToPlaylist = {
                        (activity as? MainActivity)?.showAddToPlaylistDialog(song)
                    }
                )
            }
        }
    }

    @Composable
    private fun FolderRow(folder: FolderItem, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color.Gray)
            Column {
                Text(text = folder.name, color = Color.White, fontSize = 16.sp)
                Text(text = "${folder.mediaCount} items", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }

    fun handleBackPress(): Boolean {
        if (currentPath != null) {
            val file = File(currentPath!!)
            val parent = file.parent
            val roots = viewModel.getFoldersForPath(null, com.musicplayer.models.MediaType.VIDEO).map { it.path }
            currentPath = if (roots.any { currentPath == it }) null else parent
            return true
        }
        return false
    }
}
