package com.musicplayer.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.musicplayer.R
import com.musicplayer.databinding.FragmentPlayerBinding
import com.musicplayer.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.media3.common.util.UnstableApi

@UnstableApi
class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private var seekBarJob: kotlinx.coroutines.Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupControls()
        setupBottomSheetLogic()
        observeViewModel()
    }

    private fun setupBottomSheetLogic() {
        view?.post {
            val params = view?.layoutParams as? CoordinatorLayout.LayoutParams
            val behavior = params?.behavior as? BottomSheetBehavior<*>
            behavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        binding.miniPlayerPart.alpha = 0f
                        binding.expandedPlayerPart.alpha = 1f
                    } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        binding.miniPlayerPart.alpha = 1f
                        binding.expandedPlayerPart.alpha = 0f
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    binding.miniPlayerPart.alpha = 1 - slideOffset
                    binding.expandedPlayerPart.alpha = slideOffset
                }
            })
        }
    }

    private fun setupControls() {
        binding.btnPlayPauseLarge.setOnClickListener { viewModel.togglePlayPause() }
        binding.btnMiniPlayPause.setOnClickListener { viewModel.togglePlayPause() }
        binding.btnNext.setOnClickListener { viewModel.playNext() }
        binding.btnMiniNext.setOnClickListener { viewModel.playNext() }
        binding.btnPrev.setOnClickListener { viewModel.playPrevious() }
        binding.btnMiniPrev.setOnClickListener { viewModel.playPrevious() }
        
        binding.btnForward.setOnClickListener { viewModel.seekForward() }
        binding.btnRewind.setOnClickListener { viewModel.seekBackward() }

        binding.btnShuffle.setOnClickListener { viewModel.toggleShuffle() }
        binding.btnMiniShuffle.setOnClickListener { viewModel.toggleShuffle() }
        binding.btnRepeat.setOnClickListener { viewModel.toggleRepeatMode() }
        binding.btnMiniRepeat.setOnClickListener { viewModel.toggleRepeatMode() }
        
        binding.btnFavorite.setOnClickListener {
            viewModel.currentSong.value?.let { song ->
                viewModel.toggleFavorite(song.id)
            }
        }

        binding.btnEqualizer.setOnClickListener {
            EqualizerFragment.newInstance().show(parentFragmentManager, "equalizer")
        }

        binding.fullSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) viewModel.seekTo(progress.toLong())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentSong.collectLatest { song ->
                        if (song != null) {
                            binding.tvMiniTitle.text = song.title
                            binding.tvFullTitle.text = song.title
                            binding.tvMiniArtist.text = song.artist ?: "Unknown"
                            binding.tvFullArtist.text = song.artist ?: "Unknown"
                            
                            Glide.with(this@PlayerFragment)
                                .load(song.path)
                                .placeholder(android.R.drawable.ic_menu_report_image)
                                .into(binding.ivMiniThumbnail)
                            
                            Glide.with(this@PlayerFragment)
                                .load(song.path)
                                .placeholder(android.R.drawable.ic_menu_report_image)
                                .into(binding.ivLargeThumbnail)

                            updateFavoriteState(viewModel.isFavorite(song.id))
                        }
                    }
                }
                
                launch {
                    viewModel.isPlaying.collectLatest { isPlaying ->
                        val res = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                        binding.btnMiniPlayPause.setImageResource(res)
                        binding.btnPlayPauseLarge.setImageResource(res)
                        
                        // Active color for play/pause if playing? User said "all controls are accent teal until active"
                        // Usually playing state makes the button "active".
                        val color = if (isPlaying) R.color.color_active else R.color.highlight
                        binding.btnMiniPlayPause.imageTintList = ColorStateList.valueOf(requireContext().getColor(color))
                        binding.btnPlayPauseLarge.backgroundTintList = ColorStateList.valueOf(requireContext().getColor(color))
                        
                        if (isPlaying) startSeekBarUpdates() else stopSeekBarUpdates()
                    }
                }
                
                launch {
                    viewModel.shuffleModeEnabled.collectLatest { enabled ->
                        val color = if (enabled) R.color.color_active else R.color.highlight
                        val colorList = ColorStateList.valueOf(requireContext().getColor(color))
                        binding.btnShuffle.imageTintList = colorList
                        binding.btnMiniShuffle.imageTintList = colorList
                    }
                }
                
                launch {
                    viewModel.repeatMode.collectLatest { mode ->
                        val (icon, color) = when (mode) {
                            androidx.media3.common.Player.REPEAT_MODE_OFF -> R.drawable.ic_repeat to R.color.highlight
                            androidx.media3.common.Player.REPEAT_MODE_ONE -> R.drawable.ic_repeat_one to R.color.color_active
                            else -> R.drawable.ic_repeat_all to R.color.color_active
                        }
                        val colorList = ColorStateList.valueOf(requireContext().getColor(color))
                        binding.btnRepeat.setImageResource(icon)
                        binding.btnRepeat.imageTintList = colorList
                        binding.btnMiniRepeat.setImageResource(icon)
                        binding.btnMiniRepeat.imageTintList = colorList
                    }
                }
                
                launch {
                    viewModel.favoriteIds.collect { ids ->
                        viewModel.currentSong.value?.let { song ->
                            updateFavoriteState(ids.contains(song.id))
                        }
                    }
                }
            }
        }
    }

    private fun updateFavoriteState(isFavorite: Boolean) {
        val icon = if (isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
        val color = if (isFavorite) R.color.color_active else R.color.highlight
        binding.btnFavorite.setImageResource(icon)
        binding.btnFavorite.imageTintList = ColorStateList.valueOf(requireContext().getColor(color))
    }

    private fun startSeekBarUpdates() {
        seekBarJob?.cancel()
        seekBarJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                val currentPos = viewModel.getCurrentPosition()
                val duration = viewModel.getDuration()
                binding.fullSeekBar.max = duration.toInt()
                binding.fullSeekBar.progress = currentPos.toInt()
                binding.tvCurrentTime.text = formatDuration(currentPos)
                binding.tvTotalTime.text = formatDuration(duration)
                delay(1000)
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

    override fun onDestroyView() {
        super.onDestroyView()
        stopSeekBarUpdates()
        _binding = null
    }
}
