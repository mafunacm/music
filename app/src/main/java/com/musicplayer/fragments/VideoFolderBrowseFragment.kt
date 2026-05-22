package com.musicplayer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ConcatAdapter
import com.musicplayer.MainActivity
import com.musicplayer.adapters.FolderAdapter
import com.musicplayer.adapters.MediaAdapter
import com.musicplayer.databinding.FragmentBrowseBinding
import com.musicplayer.models.MediaType
import com.musicplayer.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import androidx.media3.common.util.UnstableApi

@UnstableApi
class VideoFolderBrowseFragment : Fragment() {
    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var concatAdapter: ConcatAdapter
    private val viewModel: MainViewModel by activityViewModels()
    private var currentPath: String? = null

    companion object {
        fun newInstance() = VideoFolderBrowseFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        folderAdapter = FolderAdapter(
            onFolderClick = { folder -> openFolder(folder.path) },
            onFolderLongClick = { folder, view -> selectFolder(folder.path, view) }
        )

        mediaAdapter = MediaAdapter(
            showNumbers = false,
            onFavoriteClick = { song -> viewModel.toggleFavorite(song.id) },
            onPlaylistClick = { song -> (activity as? MainActivity)?.showAddToPlaylistDialog(song) }
        ) { song ->
            val videosInFolder = viewModel.videoFolders.value[currentPath] ?: emptyList()
            val mediaItem = videosInFolder.find { it.id == song.id }
            if (mediaItem != null) {
                (activity as? MainActivity)?.playMediaItem(mediaItem, videosInFolder)
            }
        }

        concatAdapter = ConcatAdapter(folderAdapter, mediaAdapter)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = concatAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.videoFolders.collectLatest {
                refreshUI()
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

    private fun refreshUI() {
        val subfolders = viewModel.getFoldersForPath(currentPath, MediaType.VIDEO)
        folderAdapter.submitList(subfolders)
        
        val videos = if (currentPath != null) {
            viewModel.getMediaForPath(currentPath!!, MediaType.VIDEO)
        } else {
            emptyList()
        }
        mediaAdapter.submitList(videos)
    }

    private fun openFolder(path: String) {
        currentPath = path
        refreshUI()
    }

    private fun selectFolder(folderPath: String, anchorView: View) {
        folderAdapter.setSelectedFolder(folderPath)
        
        val popup = android.widget.PopupMenu(requireContext(), anchorView)
        popup.menu.add("Play All")
        popup.menu.add("Add to Playlist")
        
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Play All" -> {
                    val videos = viewModel.videoFolders.value[folderPath] ?: emptyList()
                    if (videos.isNotEmpty()) {
                        (activity as? MainActivity)?.playMediaItem(videos.first(), videos)
                    }
                    true
                }
                "Add to Playlist" -> {
                    // Logic for adding to playlist
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    fun handleBackPress(): Boolean {
        if (currentPath != null) {
            val file = File(currentPath!!)
            val parent = file.parent
            
            val roots = viewModel.getFoldersForPath(null, MediaType.VIDEO).map { it.path }
            
            if (roots.any { currentPath == it }) {
                currentPath = null
            } else {
                currentPath = parent
            }
            
            refreshUI()
            return true
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
