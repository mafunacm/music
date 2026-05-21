package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.MainActivity
import com.musicplayer.adapters.MediaAdapter
import com.musicplayer.databinding.FragmentBrowseBinding
import com.musicplayer.models.MediaItem
import com.musicplayer.models.Song
import com.musicplayer.utils.MediaStoreHelper
import com.musicplayer.utils.PreferencesManager

class VideoBrowseFragment : Fragment() {
    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var mediaStoreHelper: MediaStoreHelper
    private lateinit var preferencesManager: PreferencesManager
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
        mediaStoreHelper = MediaStoreHelper(requireContext())
        preferencesManager = PreferencesManager(requireContext())
        setupRecyclerView()
        loadVideoFiles()
    }

    private fun setupRecyclerView() {
        mediaAdapter = MediaAdapter(showNumbers = false) { song ->
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

    private fun loadVideoFiles() {
        videoList = mediaStoreHelper.getAllVideoFiles()
        val sortedList = sortMediaList(videoList)
        mediaAdapter.submitList(sortedList.map { it.toSong() })
    }

    fun refreshMedia() {
        loadVideoFiles()
    }

    fun updatePlayingItem(id: Long) {
        if (::mediaAdapter.isInitialized) {
            mediaAdapter.setPlayingItem(id)
        }
    }

    private fun sortMediaList(list: List<MediaItem>): List<MediaItem> {
        val sortType = preferencesManager.getSortType()
        return when (sortType) {
            "NAME" -> list.sortedBy { it.name.lowercase() }
            "DURATION" -> list.sortedBy { it.duration }
            "DATE_ADDED" -> list.sortedByDescending { it.dateAdded }
            "MOST_PLAYED" -> list.sortedByDescending { preferencesManager.getPlayCount(it) }
            "RECENTLY_PLAYED" -> list.sortedByDescending { preferencesManager.getLastPlayed(it) }
            else -> list
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
