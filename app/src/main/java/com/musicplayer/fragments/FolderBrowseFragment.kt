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
import com.musicplayer.adapters.FolderAdapter
import com.musicplayer.adapters.MediaAdapter
import com.musicplayer.databinding.FragmentBrowseBinding
import com.musicplayer.models.MediaType
import com.musicplayer.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class FolderBrowseFragment : Fragment() {
    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var concatAdapter: ConcatAdapter
    private val viewModel: MainViewModel by activityViewModels()
    private var currentPath: String? = null

    companion object {
        fun newInstance() = FolderBrowseFragment()
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
            onFavoriteClick = { song -> viewModel.toggleFavorite(song.id) }
        ) { song ->
            val listToPlay = mediaAdapter.currentList
            val name = currentPath?.substringAfterLast(java.io.File.separator) ?: "Music"
            viewModel.playSong(song, listToPlay, name)
        }

        concatAdapter = ConcatAdapter(folderAdapter, mediaAdapter)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = concatAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.folders.collectLatest {
                refreshUI()
            }
        }
        lifecycleScope.launch {
            viewModel.currentSong.collectLatest { song ->
                song?.let { mediaAdapter.setPlayingItem(it.id) }
            }
        }
        lifecycleScope.launch {
            viewModel.favoriteIds.collectLatest { ids ->
                mediaAdapter.setFavorites(ids)
            }
        }
    }

    private fun refreshUI() {
        val subfolders = viewModel.getFoldersForPath(currentPath, MediaType.AUDIO)
        folderAdapter.submitList(subfolders)
        
        val songs = if (currentPath != null) {
            viewModel.getMediaForPath(currentPath!!, MediaType.AUDIO)
        } else {
            emptyList()
        }
        mediaAdapter.submitList(songs)
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
                    viewModel.playAllInFolder(folderPath)
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

    fun onFolderSelected(path: String?, items: Any?) {
        folderAdapter.setSelectedFolder(path)
    }

    fun handleBackPress(): Boolean {
        if (currentPath != null) {
            val file = File(currentPath!!)
            val parent = file.parent
            
            // Check if parent is still within media roots
            val roots = viewModel.getFoldersForPath(null, MediaType.AUDIO).map { it.path }
            
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
