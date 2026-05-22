package com.musicplayer.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
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
import com.musicplayer.PlaylistActivity
import com.musicplayer.ui.MainViewModel
import com.musicplayer.ui.theme.MusicPlayerTheme

@UnstableApi
class PlaylistBrowseFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    companion object {
        fun newInstance() = PlaylistBrowseFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MusicPlayerTheme {
                    val customPlaylists by viewModel.customPlaylists.collectAsState()
                    val basePlaylists = listOf("Favorites", "Recently Played")
                    val allPlaylists = basePlaylists + customPlaylists

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(allPlaylists) { name ->
                            PlaylistRow(name) {
                                viewModel.loadPlaylist(name)
                                startActivity(Intent(requireContext(), PlaylistActivity::class.java))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PlaylistRow(name: String, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null, tint = Color.Gray)
            Text(text = name, color = Color.White, fontSize = 16.sp)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.refreshCustomPlaylists()
    }
}
