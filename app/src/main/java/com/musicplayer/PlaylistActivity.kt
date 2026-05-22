package com.musicplayer

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import com.musicplayer.adapters.MediaAdapter
import com.musicplayer.models.Song
import java.util.concurrent.TimeUnit
import com.musicplayer.databinding.ActivityPlaylistBinding
import com.musicplayer.ui.MainViewModel
import kotlinx.coroutines.launch
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi

@UnstableApi
class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var mediaAdapter: MediaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            
            // Push player up above nav bar
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(binding.playerBottomSheet)
            behavior.peekHeight = (80 * resources.displayMetrics.density).toInt() + systemBars.bottom

            insets
        }

        setupRecyclerView()
        setupHeaderControls()
        
        val initialPlaylist = viewModel.currentPlaylist.value
        mediaAdapter.submitList(initialPlaylist)
        updateHeader(initialPlaylist)
        
        observeViewModel()
        
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

    private fun setupHeaderControls() {
        binding.btnShuffleHeader.setOnClickListener { viewModel.toggleShuffle() }
        binding.btnRepeatHeader.setOnClickListener { viewModel.toggleRepeatMode() }
        binding.btnPrevHeader.setOnClickListener { viewModel.playPrevious() }
        binding.btnNextHeader.setOnClickListener { viewModel.playNext() }
        binding.btnPlayAllHeader.setOnClickListener {
            if (viewModel.isPlaying.value) {
                viewModel.togglePlayPause()
            } else {
                val list = viewModel.currentPlaylist.value
                if (list.isNotEmpty()) {
                    viewModel.playSong(list[0], list)
                }
            }
        }
    }

    private fun updateHeader(playlist: List<Song>) {
        val totalDuration = playlist.sumOf { it.duration }
        binding.tvPlaylistMetadata.text = "${playlist.size} songs • ${formatTotalDuration(totalDuration)}"
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

    private fun setupRecyclerView() {
        mediaAdapter = MediaAdapter(
            showNumbers = true,
            onFavoriteClick = { song -> viewModel.toggleFavorite(song.id) },
            onPlaylistClick = { song -> showAddToPlaylistDialog(song) }
        ) { song ->
            viewModel.playSong(song, viewModel.currentPlaylist.value)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlaylistActivity)
            adapter = mediaAdapter
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                target: androidx.recyclerview.widget.RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (direction == ItemTouchHelper.LEFT) {
                        mediaAdapter.setSwipedPosition(position)
                    } else {
                        mediaAdapter.setSwipedPosition(-1)
                    }
                }
            }

            override fun onChildDraw(
                c: android.graphics.Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val revealWidth = -120f * recyclerView.context.resources.displayMetrics.density
                val translationX = if (dX < revealWidth) revealWidth else dX
                
                val foregroundView = viewHolder.itemView.findViewById<View>(R.id.rootView)
                ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, translationX, dY, actionState, isCurrentlyActive)
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                val position = viewHolder.bindingAdapterPosition
                val foregroundView = viewHolder.itemView.findViewById<View>(R.id.rootView)
                if (position != mediaAdapter.getSwipedPosition()) {
                    ItemTouchHelper.Callback.getDefaultUIUtil().clearView(foregroundView)
                }
            }

            override fun getSwipeDirs(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder
            ): Int {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return 0
                
                val isSwiped = mediaAdapter.getSwipedPosition() == position
                val canBeFavorite = mediaAdapter.isFavoriteActionEnabled(position)
                
                var dirs = 0
                if (canBeFavorite && !isSwiped) dirs = dirs or ItemTouchHelper.LEFT
                if (isSwiped) dirs = dirs or ItemTouchHelper.RIGHT
                
                return dirs
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 0.1f
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.playlistName.collect { name ->
                        binding.tvPlaylistName.text = name
                        mediaAdapter.setPlaylistName(name)
                        
                        val isRecent = name == "Recently Played"
                        binding.btnPlayAllHeader.visibility = if (isRecent) View.GONE else View.VISIBLE
                        binding.btnShuffleHeader.visibility = if (isRecent) View.GONE else View.VISIBLE
                        binding.btnRepeatHeader.visibility = if (isRecent) View.GONE else View.VISIBLE
                        binding.btnPrevHeader.visibility = if (isRecent) View.GONE else View.VISIBLE
                        binding.btnNextHeader.visibility = if (isRecent) View.GONE else View.VISIBLE
                        binding.btnAddSongsHeader.visibility = if (isRecent) View.GONE else View.VISIBLE
                    }
                }
                launch {
                    viewModel.currentPlaylist.collect { playlist ->
                        mediaAdapter.submitList(playlist)
                        updateHeader(playlist)
                    }
                }
                launch {
                    viewModel.favoriteIds.collect { ids ->
                        mediaAdapter.setFavorites(ids)
                    }
                }
                launch {
                    viewModel.currentSong.collect { song ->
                        if (song != null) {
                            mediaAdapter.setPlayingItem(song.id)
                            binding.playerBottomSheet.visibility = View.VISIBLE
                        } else {
                            binding.playerBottomSheet.visibility = View.GONE
                        }
                    }
                }
                launch {
                    viewModel.isPlaying.collect { isPlaying ->
                        mediaAdapter.setIsAnimating(isPlaying)
                        val res = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                        binding.btnPlayAllHeader.setImageResource(res)
                    }
                }
                launch {
                    viewModel.shuffleModeEnabled.collect { enabled ->
                        val color = if (enabled) getColor(R.color.color_active) else getColor(R.color.highlight)
                        binding.btnShuffleHeader.imageTintList = android.content.res.ColorStateList.valueOf(color)
                    }
                }
                launch {
                    viewModel.repeatMode.collect { mode ->
                        val (icon, color) = when (mode) {
                            androidx.media3.common.Player.REPEAT_MODE_OFF -> R.drawable.ic_repeat to R.color.highlight
                            androidx.media3.common.Player.REPEAT_MODE_ONE -> R.drawable.ic_repeat_one to R.color.color_active
                            else -> R.drawable.ic_repeat_all to R.color.color_active
                        }
                        binding.btnRepeatHeader.setImageResource(icon)
                        binding.btnRepeatHeader.imageTintList = android.content.res.ColorStateList.valueOf(getColor(color))
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
