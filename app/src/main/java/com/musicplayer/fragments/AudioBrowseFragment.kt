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
import androidx.media3.common.util.UnstableApi

@UnstableApi
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
        observeViewModel()
        setupFab()
    }

    private fun setupFab() {
        binding.fabSettings.visibility = View.VISIBLE
        binding.fabSettings.setOnClickListener {
            (activity as? MainActivity)?.showSettingsPopup(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentSong.collectLatest { song ->
                if (song != null) {
                    // Move up when mini player shows up
                    binding.fabSettings.animate()
                        .translationY(-400f)
                        .alpha(1f)
                        .setDuration(300)
                        .start()
                } else {
                    // Move down when mini player is gone
                    binding.fabSettings.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(300)
                        .start()
                }
            }
        }
    }


    private fun setupRecyclerView() {
        mediaAdapter = MediaAdapter(
            showNumbers = false,
            onFavoriteClick = { song -> viewModel.toggleFavorite(song.id) },
            onPlaylistClick = { song -> (activity as? MainActivity)?.showAddToPlaylistDialog(song) },
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
                val revealWidth = -120f * recyclerView.context.resources.displayMetrics.density
                // Cap the swipe distance to reveal width if swiping left
                val translationX = if (dX < revealWidth) revealWidth else dX
                
                val foregroundView = viewHolder.itemView.findViewById<View>(R.id.rootView)
                getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, translationX, dY, actionState, isCurrentlyActive)
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                val position = viewHolder.bindingAdapterPosition
                val foregroundView = viewHolder.itemView.findViewById<View>(R.id.rootView)
                
                // If this is the swiped item, don't let ItemTouchHelper reset its translation
                if (position != mediaAdapter.getSwipedPosition()) {
                    getDefaultUIUtil().clearView(foregroundView)
                }
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

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 0.1f // Swipe only 10% to trigger
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.songs.collectLatest { songs ->
                mediaAdapter.submitList(songs)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentSong.collectLatest { song ->
                mediaAdapter.setPlayingItem(song?.id ?: -1)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteIds.collectLatest { ids ->
                mediaAdapter.setFavorites(ids)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isPlaying.collectLatest { isPlaying ->
                mediaAdapter.setIsAnimating(isPlaying)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
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
