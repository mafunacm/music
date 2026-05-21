package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.R
import com.musicplayer.MainActivity
import com.musicplayer.adapters.MediaAdapter
import com.musicplayer.databinding.FragmentBrowseBinding
import com.musicplayer.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AudioBrowseFragment : Fragment() {
    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!
    private lateinit var mediaAdapter: MediaAdapter
    private val viewModel: MainViewModel by activityViewModels()

    companion object {
        fun newInstance() = AudioBrowseFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupShuffleFab()
        observeViewModel()
    }

    private fun setupShuffleFab() {
        binding.fabShuffle.visibility = View.VISIBLE
        binding.fabShuffle.setOnClickListener {
            val songs = viewModel.songs.value
            if (songs.isNotEmpty()) {
                viewModel.playSong(songs.shuffled().first(), songs.shuffled())
                viewModel.toggleShuffle()
            }
        }
    }

    private fun setupRecyclerView() {
        mediaAdapter = MediaAdapter(
            showNumbers = false,
            onFavoriteClick = { song -> viewModel.toggleFavorite(song.id) },
            onItemClick = { song ->
                viewModel.playSong(song, mediaAdapter.currentList, "Library")
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mediaAdapter
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
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
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
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
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.songs.collectLatest { songs ->
                mediaAdapter.submitList(songs)
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
        lifecycleScope.launch {
            viewModel.isPlaying.collectLatest { isPlaying ->
                mediaAdapter.setIsAnimating(isPlaying)
            }
        }
        lifecycleScope.launch {
            // Observe playlist name changes to update swipe rules in adapter
            viewModel.playlistName.collectLatest { name ->
                mediaAdapter.setPlaylistName(name)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
