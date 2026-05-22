package com.musicplayer.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.PlaylistActivity
import com.musicplayer.databinding.FragmentBrowseBinding
import com.musicplayer.adapters.PlaylistAdapter
import com.musicplayer.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.media3.common.util.UnstableApi

@UnstableApi
class PlaylistBrowseFragment : Fragment() {
    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: PlaylistAdapter

    companion object {
        fun newInstance() = PlaylistBrowseFragment()
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
        setupFab()
        observeViewModel()
        viewModel.refreshCustomPlaylists()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PlaylistAdapter { name ->
            viewModel.loadPlaylist(name)
            startActivity(Intent(requireContext(), PlaylistActivity::class.java))
        }
        binding.recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddPlaylist.visibility = View.VISIBLE
        binding.fabAddPlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.customPlaylists.collectLatest { customPlaylists ->
                val basePlaylists = listOf("Favorites", "Recently Played")
                adapter.submitList(basePlaylists + customPlaylists)
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("New Playlist")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString()
                if (name.isNotEmpty()) {
                    viewModel.createPlaylist(name)
                    viewModel.refreshCustomPlaylists()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
