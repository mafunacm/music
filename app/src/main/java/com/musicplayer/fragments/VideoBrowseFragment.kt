package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.MainActivity
import com.musicplayer.adapters.MediaAdapter
import com.musicplayer.databinding.FragmentBrowseBinding
import com.musicplayer.models.MediaItem
import com.musicplayer.models.Song
import com.musicplayer.ui.MainViewModel
import com.musicplayer.utils.MediaStoreHelper
import com.musicplayer.utils.PreferencesManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VideoBrowseFragment : Fragment() {
    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!
    private lateinit var mediaAdapter: MediaAdapter
    private val viewModel: MainViewModel by activityViewModels()
    private var videoList = listOf<MediaItem>()

    companion object {
        fun newInstance() = VideoBrowseFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        mediaAdapter = MediaAdapter(
            showNumbers = false,
            onFavoriteClick = { song -> viewModel.toggleFavorite(song.id) },
            onPlaylistClick = { song -> (activity as? MainActivity)?.showAddToPlaylistDialog(song) }
        ) { song ->
            val mediaItem = videoList.find { it.id == song.id }
            if (mediaItem != null) {
                (activity as? MainActivity)?.playMediaItem(mediaItem, videoList)
            }
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mediaAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.videos.collectLatest { videos ->
                videoList = videos
                mediaAdapter.submitList(videos.map { it.toSong() })
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentSong.collectLatest { song ->
                mediaAdapter.setPlayingItem(song?.id ?: -1)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isPlaying.collectLatest { isPlaying ->
                mediaAdapter.setIsAnimating(isPlaying)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteIds.collectLatest { ids ->
                mediaAdapter.setFavorites(ids)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
