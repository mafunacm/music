package com.musicplayer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.util.UnstableApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.musicplayer.databinding.ActivityPlaylistBinding
import com.musicplayer.models.Song
import com.musicplayer.ui.MainViewModel
import com.musicplayer.ui.components.TrackRow
import com.musicplayer.ui.theme.MusicPlayerTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@UnstableApi
class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            
            // Apply top padding to avoid status bar overlap
            binding.root.updatePadding(top = systemBars.top)
            
            // Calculate the Peek Height dynamically (Nav Bar height in dp + 2)
            val navBarDp = systemBars.bottom / density
            // Using your formula (navBar + 2) but ensuring a minimum of 85dp so UI doesn't break
            val dynamicPlayerHeight = (navBarDp + 2).coerceAtLeast(85f)
            
            val behavior = BottomSheetBehavior.from(binding.playerBottomSheet)
            val peekHeightPx = (dynamicPlayerHeight * density).toInt() + systemBars.bottom
            behavior.peekHeight = peekHeightPx

            // Dynamic black area
            binding.root.setBackgroundColor(android.graphics.Color.BLACK)
            val currentSong = viewModel.currentSong.value
            val bottomPadding = if (currentSong != null) peekHeightPx else systemBars.bottom

            // Push the list content up so it's not behind the mini player/nav bar
            binding.composeView.updatePadding(bottom = bottomPadding)
            binding.composeView.clipToPadding = true
            
            insets
        }

        setupComposeList()
        setupHeaderControls()
        observeViewModel()
        
        // Back button highlight color (Teal)
        binding.toolbar.navigationIcon?.setTint(getColor(R.color.highlight))
        binding.toolbar.setNavigationOnClickListener { finish() }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val behavior = BottomSheetBehavior.from(binding.playerBottomSheet)
                if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    return
                }
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        })
    }

    private fun setupComposeList() {
        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MusicPlayerTheme {
                    val playlist by MainViewModel.sharedViewingPlaylist.collectAsState()
                    val currentSong by viewModel.currentSong.collectAsState()
                    val isPlaying by viewModel.isPlaying.collectAsState()
                    val favoriteIds by viewModel.favoriteIds.collectAsState()

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(playlist, key = { it.id }) { song ->
                            TrackRow(
                                song = song,
                                isActive = song.id == currentSong?.id,
                                isPlaying = isPlaying,
                                isFavorite = favoriteIds.contains(song.id),
                                onSelect = {
                                    viewModel.playSong(song, playlist)
                                },
                                onFavoriteToggle = {
                                    viewModel.toggleFavorite(song.id)
                                },
                                onAddToPlaylist = {
                                    showAddToPlaylistDialog(song)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setupHeaderControls() {
        binding.btnPlayAllHeader.setOnClickListener {
            viewModel.playViewingPlaylist()
        }
        binding.btnAddSongsHeader.setOnClickListener {
            Toast.makeText(this, "Add songs coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateHeader(playlist: List<Song>) {
        val totalDuration = playlist.sumOf { it.duration }
        binding.tvPlaylistMetadata.text = "${playlist.size} songs • ${formatTotalDuration(totalDuration)}"
        
        // 2. Play All button logic: Empty -> Gray (Dormant), Not empty -> Highlight (Teal)
        val hasSongs = playlist.isNotEmpty()
        val color = if (hasSongs) getColor(R.color.highlight) else getColor(R.color.domant)
        binding.btnPlayAllHeader.setTextColor(color)
        (binding.btnPlayAllHeader as? com.google.android.material.button.MaterialButton)?.iconTint = 
            android.content.res.ColorStateList.valueOf(color)
        binding.btnPlayAllHeader.isEnabled = hasSongs
    }

    private fun formatTotalDuration(durationMs: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        return if (hours > 0) {
            String.format("%dh %02dm %02ds", hours, minutes, seconds)
        } else {
            String.format("%02dm %02ds", minutes, seconds)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    MainViewModel.sharedPlaylistName.collect { name ->
                        binding.tvPlaylistName.text = name
                        // Add button hidden for auto-playlists
                        binding.btnAddSongsHeader.visibility = if (name == "Recently Played" || name == "Favorites") View.GONE else View.VISIBLE
                    }
                }
                launch {
                    MainViewModel.sharedViewingPlaylist.collect { playlist ->
                        updateHeader(playlist)
                    }
                }
                launch {
                    viewModel.currentSong.collectLatest { song ->
                        binding.playerBottomSheet.visibility = if (song != null) View.VISIBLE else View.GONE
                        // Trigger an inset update to adjust the black area height
                        ViewCompat.requestApplyInsets(binding.root)
                    }
                }
            }
        }
    }

    private fun showAddToPlaylistDialog(song: Song) {
        lifecycleScope.launch {
            val appDao = com.musicplayer.database.AppDatabase.getDatabase(this@PlaylistActivity).appDao()
            val playlists = appDao.getAllPlaylistNames()
            if (playlists.isEmpty()) {
                Toast.makeText(this@PlaylistActivity, "No custom playlists found.", Toast.LENGTH_LONG).show()
                return@launch
            }

            val builder = androidx.appcompat.app.AlertDialog.Builder(this@PlaylistActivity)
            builder.setTitle("Add to Playlist")
            builder.setItems(playlists.toTypedArray()) { _, which ->
                val selectedPlaylist = playlists[which]
                viewModel.addSongToPlaylist(selectedPlaylist, song)
                Toast.makeText(this@PlaylistActivity, "Added to $selectedPlaylist", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }
    }
}
