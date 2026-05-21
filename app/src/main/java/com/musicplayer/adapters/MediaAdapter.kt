package com.musicplayer.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.musicplayer.R
import com.musicplayer.databinding.ItemMediaBinding
import com.musicplayer.models.Song
import java.util.concurrent.TimeUnit

class MediaAdapter(
    private val showNumbers: Boolean = false,
    private val onFavoriteClick: ((Song) -> Unit)? = null,
    private val onItemClick: (Song) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    var currentList = listOf<Song>()
        private set
    private var playingItemId: Long = -1
    private var favoriteIds = setOf<Long>()
    private var currentPlaylistName: String = ""
    private var swipedPosition: Int = -1
    private var isAnimating: Boolean = false

    fun submitList(list: List<Song>) {
        Log.d("MediaAdapter", "submitList: size ${list.size}")
        currentList = list
        notifyDataSetChanged()
    }

    fun setIsAnimating(animating: Boolean) {
        if (isAnimating != animating) {
            isAnimating = animating
            notifyDataSetChanged()
        }
    }

    fun setPlayingItem(id: Long) {
        playingItemId = id
        notifyDataSetChanged()
    }

    fun setFavorites(ids: Set<Long>) {
        favoriteIds = ids
        notifyDataSetChanged()
    }

    fun setPlaylistName(name: String) {
        currentPlaylistName = name
        notifyDataSetChanged()
    }

    fun setSwipedPosition(position: Int) {
        val old = swipedPosition
        swipedPosition = position
        if (old != -1) notifyItemChanged(old)
        if (swipedPosition != -1) notifyItemChanged(swipedPosition)
    }

    fun getSwipedPosition() = swipedPosition

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemCount() = currentList.size

    fun isFavoriteActionEnabled(position: Int): Boolean {
        if (position !in currentList.indices) return false
        // Always allow swiping to reveal actions, unless it's a specific edge case
        return true
    }

    inner class MediaViewHolder(val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            if (showNumbers) {
                binding.tvNumber.visibility = View.VISIBLE
                binding.tvNumber.text = (bindingAdapterPosition + 1).toString()
            } else {
                binding.tvNumber.visibility = View.GONE
            }

            binding.tvName.text = song.title
            binding.tvInfo.text = song.artist ?: "Unknown Artist"

            val isPlaying = song.id == playingItemId
            binding.rootView.setBackgroundColor(
                if (isPlaying) binding.root.context.getColor(R.color.playing_item_bg) else binding.root.context.getColor(R.color.background)
            )
            
            if (isPlaying) {
                binding.spectrumView.visibility = View.VISIBLE
                if (isAnimating) {
                    binding.spectrumView.startAnimation()
                } else {
                    binding.spectrumView.stopAnimation()
                }
            } else {
                binding.spectrumView.visibility = View.GONE
                binding.spectrumView.stopAnimation()
            }
            
            Glide.with(binding.ivThumbnail)
                .load(song.path)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .into(binding.ivThumbnail)

            binding.rootView.setOnClickListener {
                if (swipedPosition == bindingAdapterPosition) {
                    setSwipedPosition(-1)
                } else {
                    onItemClick(song)
                }
            }

            val canBeFavorite = isFavoriteActionEnabled(bindingAdapterPosition)
            binding.swipeRevealLayout.visibility = if (canBeFavorite) View.VISIBLE else View.GONE

            binding.swipeRevealLayout.setOnClickListener {
                onFavoriteClick?.invoke(song)
                setSwipedPosition(-1)
            }

            // Apply translation if this item is swiped
            val revealWidth = -100f * binding.root.context.resources.displayMetrics.density
            binding.rootView.translationX = if (swipedPosition == bindingAdapterPosition) revealWidth else 0f
        }
    }
}
