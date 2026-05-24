package com.musicplayer.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.musicplayer.MainActivity
import com.musicplayer.fragments.AudioBrowseFragment
import com.musicplayer.fragments.BuyFragment
import com.musicplayer.fragments.EventFragment
import com.musicplayer.fragments.FolderBrowseFragment
import com.musicplayer.fragments.PlaylistBrowseFragment
import com.musicplayer.fragments.SettingsFragment
import com.musicplayer.fragments.VideoFolderBrowseFragment
import androidx.media3.common.util.UnstableApi

@UnstableApi
class ViewPagerAdapter(
    activity: MainActivity
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 7

    override fun createFragment(position: Int): Fragment {

        return when (position) {

            0 -> AudioBrowseFragment.newInstance()

            1 -> FolderBrowseFragment.newInstance()

            2 -> VideoFolderBrowseFragment.newInstance()

            3 -> PlaylistBrowseFragment.newInstance()

            4 -> BuyFragment.newInstance()

            5 -> EventFragment.newInstance()

            6 -> SettingsFragment.newInstance()

            else -> throw IllegalArgumentException(
                "Invalid position: $position"
            )
        }
    }
}