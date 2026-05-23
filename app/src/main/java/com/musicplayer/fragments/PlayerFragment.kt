package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.util.UnstableApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.musicplayer.MainActivity
import com.musicplayer.R
import com.musicplayer.ui.MainViewModel
import com.musicplayer.ui.components.MiniPlayerBar
import com.musicplayer.ui.screens.NowPlayingScreen
import com.musicplayer.ui.theme.MusicPlayerTheme

@UnstableApi
class PlayerFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MusicPlayerTheme {
                    PlayerScreen()
                }
            }
        }
    }

    @Composable
    private fun PlayerScreen() {
        val song by viewModel.currentSong.collectAsState()
        val isPlaying by viewModel.isPlaying.collectAsState()
        val isShuffle by viewModel.shuffleModeEnabled.collectAsState()
        val repeatMode by viewModel.repeatMode.collectAsState()
        val isFavorite = song?.let { viewModel.isFavorite(it.id) } ?: false

        var currentTime by remember { mutableLongStateOf(0L) }
        var totalDuration by remember { mutableLongStateOf(0L) }
        var slideOffset by remember { mutableFloatStateOf(0f) }

        // Update playback time
        LaunchedEffect(isPlaying, song) {
            if (isPlaying && song != null) {
                while (true) {
                    currentTime = viewModel.getCurrentPosition().coerceAtLeast(0L)
                    totalDuration = viewModel.getDuration().coerceAtLeast(0L)
                    kotlinx.coroutines.delay(1000)
                }
            } else {
                currentTime = viewModel.getCurrentPosition().coerceAtLeast(0L)
                totalDuration = viewModel.getDuration().coerceAtLeast(0L)
            }
        }

        LaunchedEffect(Unit) {
            val activity = activity as? MainActivity ?: return@LaunchedEffect
            val bottomSheet = activity.findViewById<View>(R.id.playerBottomSheet)
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}
                override fun onSlide(bottomSheet: View, offset: Float) {
                    slideOffset = offset
                }
            })
            // Initial offset
            slideOffset = if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) 1f else 0f
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Full Player
            Box(modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = slideOffset }
            ) {
                NowPlayingScreen(
                    song = song,
                    isPlaying = isPlaying,
                    isShuffle = isShuffle,
                    isRepeat = repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF,
                    currentTime = currentTime,
                    totalDuration = totalDuration,
                    onClose = { (activity as? MainActivity)?.let { BottomSheetBehavior.from(it.findViewById<View>(R.id.playerBottomSheet)).state = BottomSheetBehavior.STATE_COLLAPSED } },
                    onPlayPause = { viewModel.togglePlayPause() },
                    onShuffle = { viewModel.toggleShuffle() },
                    onRepeat = { viewModel.toggleRepeatMode() },
                    onPrev = { viewModel.playPrevious() },
                    onNext = { viewModel.playNext() },
                    onFavoriteToggle = { song?.let { viewModel.toggleFavorite(it.id) } },
                    onSeek = { viewModel.seekTo(it) },
                    isFavorite = isFavorite
                )
            }

            // Mini Player
            if (slideOffset < 1f) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .graphicsLayer { alpha = 1f - slideOffset }
                ) {
                    MiniPlayerBar(
                        song = song,
                        isPlaying = isPlaying,
                        isShuffle = isShuffle,
                        isRepeat = repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF,
                        progress = if (totalDuration > 0) currentTime.toFloat() / totalDuration else 0f,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onPrev = { viewModel.playPrevious() },
                        onNext = { viewModel.playNext() },
                        onShuffle = { viewModel.toggleShuffle() },
                        onRepeat = { viewModel.toggleRepeatMode() },
                        onOpen = { (activity as? MainActivity)?.let { BottomSheetBehavior.from(it.findViewById<View>(R.id.playerBottomSheet)).state = BottomSheetBehavior.STATE_EXPANDED } }
                    )
                }
            }
        }
    }
}
