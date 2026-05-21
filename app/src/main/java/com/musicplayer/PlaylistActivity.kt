package com.musicplayer

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.adapters.MediaAdapter
import com.musicplayer.models.Song
import java.util.concurrent.TimeUnit
import com.musicplayer.databinding.ActivityPlaylistBinding
import com.musicplayer.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
            insets
        }

        setupRecyclerView()
        setupBottomSheet()
        setupFullPlayerControls()
        
        val initialPlaylist = viewModel.currentPlaylist.value
        mediaAdapter.submitList(initialPlaylist)
        updateHeader(initialPlaylist)
        
        observeViewModel()
        
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupBottomSheet() {
        val behavior = BottomSheetBehavior.from(binding.fullPlayer.root)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    binding.fullPlayer.miniPlayerPart.alpha = 0f
                    binding.fullPlayer.expandedPlayerPart.alpha = 1f
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.fullPlayer.miniPlayerPart.alpha = 1f
                    binding.fullPlayer.expandedPlayerPart.alpha = 0f
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.fullPlayer.miniPlayerPart.alpha = 1 - slideOffset
                binding.fullPlayer.expandedPlayerPart.alpha = slideOffset
            }
        })
    }

    private fun setupFullPlayerControls() {
        binding.fullPlayer.btnPlayPauseLarge.setOnClickListener { viewModel.togglePlayPause() }
        binding.fullPlayer.btnMiniPlayPause.setOnClickListener { viewModel.togglePlayPause() }
        binding.fullPlayer.btnNext.setOnClickListener { viewModel.playNext() }
        binding.fullPlayer.btnMiniNext.setOnClickListener { viewModel.playNext() }
        binding.fullPlayer.btnPrev.setOnClickListener { viewModel.playPrevious() }
        binding.fullPlayer.btnMiniPrev.setOnClickListener { viewModel.playPrevious() }
        
        binding.fullPlayer.btnForward.setOnClickListener { viewModel.seekForward() }
        binding.fullPlayer.btnRewind.setOnClickListener { viewModel.seekBackward() }

        binding.fullPlayer.btnShuffle.setOnClickListener { viewModel.toggleShuffle() }
        binding.fullPlayer.btnRepeat.setOnClickListener { viewModel.toggleRepeatMode() }

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
        
        binding.fullPlayer.fullSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) viewModel.seekTo(progress.toLong())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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
            onFavoriteClick = { song -> viewModel.toggleFavorite(song.id) }
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
                val foregroundView = viewHolder.itemView.findViewById<View>(R.id.rootView)
                getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive)
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                val foregroundView = viewHolder.itemView.findViewById<View>(R.id.rootView)
                getDefaultUIUtil().clearView(foregroundView)
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
                return 0.1f // Swipe only 10% to trigger
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun observeViewModel() {
        binding.fullPlayer.tvMiniTitle.isSelected = true
        binding.fullPlayer.tvMiniArtist.isSelected = true
        binding.fullPlayer.tvFullTitle.isSelected = true
        binding.fullPlayer.tvFullArtist.isSelected = true

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.playlistName.collect { name ->
                        binding.tvPlaylistName.text = name
                        mediaAdapter.setPlaylistName(name)
                        
                        val isRecent = name == "Recently Played"
                        val isFavorites = name == "Favorites"
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
                            binding.fullPlayer.tvMiniTitle.text = song.title
                            binding.fullPlayer.tvFullTitle.text = song.title
                            binding.fullPlayer.tvMiniArtist.text = song.artist ?: "Unknown"
                            binding.fullPlayer.tvFullArtist.text = song.artist ?: "Unknown"
                            
                            Glide.with(this@PlaylistActivity)
                                .load(song.path)
                                .placeholder(android.R.drawable.ic_menu_report_image)
                                .into(binding.fullPlayer.ivMiniThumbnail)
                            
                            Glide.with(this@PlaylistActivity)
                                .load(song.path)
                                .placeholder(android.R.drawable.ic_menu_report_image)
                                .into(binding.fullPlayer.ivLargeThumbnail)
                        }
                    }
                }
                launch {
                    viewModel.isPlaying.collect { isPlaying ->
                        mediaAdapter.setIsAnimating(isPlaying)
                        val res = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                        binding.fullPlayer.btnMiniPlayPause.setImageResource(res)
                        binding.fullPlayer.btnPlayPauseLarge.setImageResource(res)
                        binding.btnPlayAllHeader.setImageResource(res)
                        
                        if (isPlaying) {
                            startSeekBarUpdates()
                        } else {
                            stopSeekBarUpdates()
                        }
                    }
                }
        launch {
            viewModel.shuffleModeEnabled.collect { enabled ->
                val color = if (enabled) getColor(R.color.accent_teal) else getColor(R.color.text_secondary)
                binding.fullPlayer.btnShuffle.setImageResource(R.drawable.ic_shuffle)
                binding.fullPlayer.btnShuffle.imageTintList = android.content.res.ColorStateList.valueOf(color)
                binding.btnShuffleHeader.setImageResource(R.drawable.ic_shuffle)
                binding.btnShuffleHeader.imageTintList = android.content.res.ColorStateList.valueOf(color)
            }
        }
        launch {
            viewModel.repeatMode.collect { mode ->
                val (icon, color) = when (mode) {
                    androidx.media3.common.Player.REPEAT_MODE_OFF -> R.drawable.ic_repeat to R.color.text_secondary
                    androidx.media3.common.Player.REPEAT_MODE_ONE -> R.drawable.ic_repeat_one to R.color.accent_teal
                    else -> R.drawable.ic_repeat_all to R.color.accent_teal
                }
                binding.fullPlayer.btnRepeat.setImageResource(icon)
                binding.fullPlayer.btnRepeat.imageTintList = android.content.res.ColorStateList.valueOf(getColor(color))
                
                binding.btnRepeatHeader.setImageResource(icon)
                binding.btnRepeatHeader.imageTintList = android.content.res.ColorStateList.valueOf(getColor(color))
            }
        }
            }
        }
    }

    private var seekBarJob: kotlinx.coroutines.Job? = null

    private fun startSeekBarUpdates() {
        seekBarJob?.cancel()
        seekBarJob = lifecycleScope.launch {
            while (true) {
                val currentPos = viewModel.getCurrentPosition()
                val duration = viewModel.getDuration()
                binding.fullPlayer.fullSeekBar.max = duration.toInt()
                binding.fullPlayer.fullSeekBar.progress = currentPos.toInt()
                binding.fullPlayer.tvCurrentTime.text = formatDuration(currentPos)
                binding.fullPlayer.tvTotalTime.text = formatDuration(duration)
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun stopSeekBarUpdates() {
        seekBarJob?.cancel()
    }

    private fun formatDuration(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
