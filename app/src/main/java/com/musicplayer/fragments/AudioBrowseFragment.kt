package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.musicplayer.MainActivity
import com.musicplayer.ui.MainViewModel
import androidx.media3.common.util.UnstableApi

import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.musicplayer.ui.components.TrackRow
import com.musicplayer.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@UnstableApi
class AudioBrowseFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    companion object {
        fun newInstance() = AudioBrowseFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MusicPlayerTheme {
                    val songs by viewModel.songs.collectAsState()
                    val currentSong by viewModel.currentSong.collectAsState()
                    val isPlaying by viewModel.isPlaying.collectAsState()
                    val favoriteIds by viewModel.favoriteIds.collectAsState()
                    
                    var selectedSort by remember { mutableStateOf("All") }
                    val sortOptions = listOf("All", "Artist", "Album", "Genre")

                    Column(modifier = Modifier.fillMaxSize().background(PlayerBg)) {
                        // Breadcrumbs / Sort Options
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(sortOptions) { option ->
                                val isSelected = selectedSort == option
                                Surface(
                                    onClick = { selectedSort = option },
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) PlayerActive.copy(alpha = 0.15f) else Color.Transparent,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, PlayerActive) else null
                                ) {
                                    Text(
                                        text = option,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        color = if (isSelected) PlayerActive else PlayerDormant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(songs) { song ->
                                TrackRow(
                                    song = song,
                                    isActive = song.id == currentSong?.id,
                                    isPlaying = isPlaying,
                                    isFavorite = favoriteIds.contains(song.id),
                                    onSelect = {
                                        viewModel.playSong(song, songs, "Library")
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
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
