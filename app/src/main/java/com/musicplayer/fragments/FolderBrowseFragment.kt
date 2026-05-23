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
import com.musicplayer.models.FolderItem
import com.musicplayer.ui.MainViewModel
import com.musicplayer.ui.components.TrackRow
import com.musicplayer.ui.theme.MusicPlayerTheme
import java.io.File

@UnstableApi
class FolderBrowseFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private var currentPath by mutableStateOf<String?>(null)

    companion object {
        fun newInstance() = FolderBrowseFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MusicPlayerTheme {
                    FolderContent()
                }
            }
        }
    }

    @Composable
    private fun FolderContent() {
        val currentSong by viewModel.currentSong.collectAsState()
        val isPlaying by viewModel.isPlaying.collectAsState()
        val favoriteIds by viewModel.favoriteIds.collectAsState()

        val subfolders = viewModel.getFoldersForPath(currentPath, com.musicplayer.models.MediaType.AUDIO)
        val songs = if (currentPath != null) {
            viewModel.getMediaForPath(currentPath!!, com.musicplayer.models.MediaType.AUDIO)
        } else {
            emptyList()
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(subfolders) { folder ->
                FolderRow(folder) {
                    currentPath = folder.path
                }
            }
            items(songs) { song ->
                TrackRow(
                    song = song,
                    isActive = song.id == currentSong?.id,
                    isPlaying = isPlaying,
                    isFavorite = favoriteIds.contains(song.id),
                    onSelect = {
                        viewModel.playSong(song, songs, currentPath?.substringAfterLast(File.separator) ?: "Music")
                    },
                    onFavoriteToggle = {
                        viewModel.toggleFavorite(song.id)
                    },
                    onAddToPlaylist = {
                        (activity as? com.musicplayer.MainActivity)?.showAddToPlaylistDialog(song)
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
            Icon(Icons.Default.Folder, contentDescription = null, tint = Color.Gray)
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
            val roots = viewModel.getFoldersForPath(null, com.musicplayer.models.MediaType.AUDIO).map { it.path }
            currentPath = if (roots.any { currentPath == it }) null else parent
            return true
        }
        return false
    }

    fun onFolderSelected(path: String?, items: Any?) {
        // No-op or update state if needed
    }
}
