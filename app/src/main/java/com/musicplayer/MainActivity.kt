package com.musicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.musicplayer.adapters.ViewPagerAdapter
import com.musicplayer.databinding.ActivityMainBinding
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.musicplayer.fragments.AudioBrowseFragment
import com.musicplayer.fragments.FolderBrowseFragment
import com.musicplayer.fragments.PlaylistBrowseFragment
import com.musicplayer.fragments.VideoBrowseFragment
import com.musicplayer.fragments.VideoFolderBrowseFragment
import com.musicplayer.models.MediaItem
import com.musicplayer.models.MediaType
import com.musicplayer.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val viewModel: MainViewModel by viewModels()
    private val fragments = listOf(
        AudioBrowseFragment.newInstance(),
        FolderBrowseFragment.newInstance(),
        VideoFolderBrowseFragment.newInstance(),
        PlaylistBrowseFragment.newInstance(),
        com.musicplayer.fragments.BuyFragment.newInstance()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupTabs()
        checkPermissions()
        setupPlayerControls()
        setupSelectionMenu()
        observeViewModel()
    }

    private fun observeViewModel() {
        binding.tvCurrentSong.isSelected = true
        binding.tvCurrentArtist.isSelected = true

        lifecycleScope.launch {
            viewModel.currentSong.collectLatest { song ->
                if (song != null) {
                    binding.miniPlayer.visibility = View.VISIBLE
                    binding.tvCurrentSong.text = song.title
                    binding.tvCurrentArtist.text = song.artist ?: "Unknown"
                    Glide.with(this@MainActivity)
                        .load(song.path)
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .into(binding.ivMiniThumbnail)
                } else {
                    binding.miniPlayer.visibility = View.GONE
                    binding.tvCurrentSong.text = "Not Playing"
                }
            }
        }
        lifecycleScope.launch {
            viewModel.isPlaying.collectLatest { isPlaying ->
                binding.playPauseButton.setImageResource(
                    if (isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
            }
        }
        lifecycleScope.launch {
            viewModel.shuffleModeEnabled.collectLatest { enabled ->
                val color = if (enabled) getColor(R.color.color_active) else getColor(R.color.accent_teal)
                binding.btnMiniShuffle.imageTintList = android.content.res.ColorStateList.valueOf(color)
            }
        }
        lifecycleScope.launch {
            viewModel.repeatMode.collectLatest { mode ->
                val (icon, color) = when (mode) {
                    androidx.media3.common.Player.REPEAT_MODE_OFF -> R.drawable.ic_repeat to R.color.accent_teal
                    androidx.media3.common.Player.REPEAT_MODE_ONE -> R.drawable.ic_repeat_one to R.color.color_active
                    else -> R.drawable.ic_repeat_all to R.color.color_active
                }
                binding.btnMiniRepeat.setImageResource(icon)
                binding.btnMiniRepeat.imageTintList = android.content.res.ColorStateList.valueOf(getColor(color))
            }
        }
    }

    private fun setupTabs() {
        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle, fragments)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if (position == 4) {
                val textView = android.widget.TextView(this)
                textView.text = "BUY"
                textView.setTextColor(if (tab.isSelected) getColor(R.color.color_active) else getColor(R.color.accent_teal))
                textView.setTypeface(null, android.graphics.Typeface.BOLD)
                textView.gravity = android.view.Gravity.CENTER
                tab.customView = textView
            } else {
                val customView = layoutInflater.inflate(R.layout.custom_tab, null)
                val iconView = customView.findViewById<ImageView>(R.id.tabIcon)
                iconView.setImageResource(when (position) {
                    0 -> R.drawable.ic_tab_music
                    1 -> R.drawable.ic_tab_music_folder
                    2 -> R.drawable.ic_tab_video_folder
                    else -> R.drawable.ic_tab_playlist
                })
                // Removed tinting to preserve original PNG details
                tab.customView = customView
            }
        }.attach()
        
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                val view = tab.customView
                if (view is android.widget.TextView) {
                    view.setTextColor(getColor(R.color.color_active))
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                val view = tab.customView
                if (view is android.widget.TextView) {
                    view.setTextColor(getColor(R.color.accent_teal))
                }
            }
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })
        
        binding.tabLayout.setSelectedTabIndicatorColor(getColor(R.color.color_active))
        binding.tabLayout.tabIconTint = null
    }

    private fun setupPlayerControls() {
        binding.playPauseButton.setOnClickListener {
            viewModel.togglePlayPause()
        }
        binding.btnMiniNext.setOnClickListener {
            viewModel.playNext()
        }
        binding.btnMiniPrev.setOnClickListener {
            viewModel.playPrevious()
        }
        binding.btnMiniShuffle.setOnClickListener {
            viewModel.toggleShuffle()
        }
        binding.btnMiniRepeat.setOnClickListener {
            viewModel.toggleRepeatMode()
        }
        binding.miniPlayer.setOnClickListener {
            startActivity(Intent(this, PlaylistActivity::class.java))
        }
    }

    private fun setupSelectionMenu() {
        binding.btnCloseSelection.setOnClickListener {
            hideSelectionMenu()
        }
    }

    fun hideSelectionMenu() {
        binding.folderSelectionMenu.visibility = android.view.View.GONE
        val currentFragment = fragments[binding.viewPager.currentItem]
        if (currentFragment is FolderBrowseFragment) {
            currentFragment.onFolderSelected(null, null)
        } else if (currentFragment is VideoFolderBrowseFragment) {
            currentFragment.handleBackPress() // Reset selection state if needed
        }
    }

    fun playMediaItem(item: MediaItem, playlist: List<MediaItem>) {
        if (item.type == MediaType.VIDEO) {
            // Stop audio if video is starting
            viewModel.pauseMedia()
            val intent = Intent(this, VideoPlayerActivity::class.java).apply {
                val uris = ArrayList<String>()
                playlist.forEach { uris.add(it.uri.toString()) }
                putStringArrayListExtra("VIDEO_URIS", uris)
                putExtra("START_INDEX", playlist.indexOf(item))
            }
            startActivity(intent)
        } else {
            // Convert to Song and play via ViewModel if it's audio
            val folderName = item.folderPath.substringAfterLast(java.io.File.separator)
            viewModel.playSong(item.toSong(), playlist.map { it.toSong() }, folderName)
        }
    }

    override fun onBackPressed() {
        val currentFragment = fragments[binding.viewPager.currentItem]
        if ((currentFragment is FolderBrowseFragment && currentFragment.handleBackPress()) ||
            (currentFragment is VideoFolderBrowseFragment && currentFragment.handleBackPress())) {
            return
        }
        super.onBackPressed()
    }

    fun showSelectionMenu(folderName: String, onPlayAll: () -> Unit) {
        binding.tvSelectedFolderName.text = folderName
        binding.folderSelectionMenu.visibility = android.view.View.VISIBLE
        binding.btnPlayAll.setOnClickListener {
            onPlayAll()
            hideSelectionMenu()
            startActivity(Intent(this, PlaylistActivity::class.java))
        }
    }

    private fun checkPermissions() {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val needPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (needPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needPermissions, 100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 100 || !grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            Toast.makeText(this, "Permissions required", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.loadMedia()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
}
