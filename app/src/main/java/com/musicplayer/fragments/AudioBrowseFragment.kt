package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.R
import com.musicplayer.MainActivity
import com.musicplayer.adapters.MediaAdapter
import com.musicplayer.databinding.FragmentBrowseBinding
import com.musicplayer.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.media3.common.util.UnstableApi

import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.musicplayer.ui.components.TrackRow
import com.musicplayer.ui.theme.MusicPlayerTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

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
                    
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(songs) { song ->
                            TrackRow(
                                song = song,
                                isActive = song.id == currentSong?.id,
                                isPlaying = isPlaying,
                                onSelect = {
                                    viewModel.playSong(song, songs, "Library")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // XML/Fab logic removed/to be updated
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }
}
