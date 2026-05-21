package com.musicplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.musicplayer.databinding.ItemFolderBinding
import com.musicplayer.models.FolderItem

class FolderAdapter(
    private val onFolderClick: (FolderItem) -> Unit,
    private val onFolderLongClick: (FolderItem, View) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    private var items = listOf<FolderItem>()

    private var selectedPath: String? = null

    fun submitList(
        folders: List<FolderItem>
    ) {

        items = folders

        notifyDataSetChanged()
    }

    fun setSelectedFolder(
        path: String?
    ) {

        selectedPath = path

        notifyDataSetChanged()
    }

    fun getFolderPosition(
        path: String
    ): Int {

        return items.indexOfFirst { it.path == path }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FolderViewHolder {

        val binding = ItemFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FolderViewHolder,
        position: Int
    ) {

        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class FolderViewHolder(
        private val binding: ItemFolderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: FolderItem
        ) {

            binding.tvFolderName.text =
                item.name

            binding.tvMediaCount.text =
                "${item.mediaCount} items"

            val selected =
                item.path == selectedPath

            binding.root.setCardBackgroundColor(

                if (selected)
                    0xFF2A1B3D.toInt()
                else
                    0xFF1E1E1E.toInt()
            )

            binding.root.setOnClickListener {

                onFolderClick(item)
            }

            binding.root.setOnLongClickListener {

                onFolderLongClick(item, binding.root)

                true
            }
        }
    }
}