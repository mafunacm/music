package com.musicplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.databinding.ItemFolderBinding

class PlaylistAdapter(private val onClick: (String) -> Unit) :
    RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    private var items = listOf<String>()

    fun submitList(list: List<String>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = items[position]
        holder.binding.tvFolderName.text = name
        holder.binding.tvMediaCount.text = "Playlist"
        holder.itemView.setOnClickListener { onClick(name) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemFolderBinding) : RecyclerView.ViewHolder(binding.root)
}
