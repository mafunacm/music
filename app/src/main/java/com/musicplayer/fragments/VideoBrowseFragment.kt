package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.util.UnstableApi
import com.musicplayer.MainActivity
import com.musicplayer.ui.MainViewModel
import com.musicplayer.ui.components.TrackRow
import com.musicplayer.ui.theme.MusicPlayerTheme

@UnstableApi
class VideoBrowseFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    companion object {
        fun newInstance() = VideoBrowseFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MusicPlayerTheme {
                    val videos by viewModel.videos.collectAsState()
                    val favoriteIds by viewModel.favoriteIds.collectAsState()
                    
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(videos) { video ->
                            val song = video.toSong()
                            TrackRow(
                                song = song,
                                isActive = false,
                                isPlaying = false,
                                isFavorite = favoriteIds.contains(song.id),
                                onSelect = {
                                    (activity as? MainActivity)?.playMediaItem(video, videos)
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
