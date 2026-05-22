package com.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import com.musicplayer.adapters.ViewPagerAdapter
import com.musicplayer.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import com.musicplayer.fragments.AudioBrowseFragment
import com.musicplayer.fragments.FolderBrowseFragment
import com.musicplayer.fragments.PlaylistBrowseFragment
import com.musicplayer.fragments.VideoBrowseFragment
import com.musicplayer.fragments.VideoFolderBrowseFragment
import com.musicplayer.fragments.EventFragment
import com.musicplayer.models.MediaItem
import com.musicplayer.models.MediaType
import com.musicplayer.models.Song
import com.musicplayer.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi

import androidx.activity.enableEdgeToEdge
import androidx.core.view.updatePadding

@UnstableApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val viewModel: MainViewModel by viewModels()
    private val appDao by lazy { com.musicplayer.database.AppDatabase.getDatabase(this).appDao() }
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val fragments = listOf(
        AudioBrowseFragment.newInstance(),
        FolderBrowseFragment.newInstance(),
        VideoFolderBrowseFragment.newInstance(),
        PlaylistBrowseFragment.newInstance(),
        com.musicplayer.fragments.BuyFragment.newInstance(),
        EventFragment.newInstance(),
        com.musicplayer.fragments.SettingsFragment.newInstance()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply top padding to the main content container to avoid status bar overlap
            binding.mainContentContainer.updatePadding(top = systemBars.top)

            // Calculate the Peek Height including the Navigation Bar
            val density = resources.displayMetrics.density
            val behavior = BottomSheetBehavior.from(binding.playerBottomSheet)
            // 120dp for the mini player + system nav bar height
            behavior.peekHeight = (120 * density).toInt() + systemBars.bottom

            insets
        }

        setupTabs()
        checkPermissions()
        setupSelectionMenu()
        setupPlayerBottomSheet()
        observeViewModel()

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val behavior = BottomSheetBehavior.from(binding.playerBottomSheet)
                if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    return
                }

                val currentFragment = fragments[binding.viewPager.currentItem]
                if ((currentFragment is FolderBrowseFragment && currentFragment.handleBackPress()) ||
                    (currentFragment is VideoFolderBrowseFragment && currentFragment.handleBackPress())) {
                    return
                }
                
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        })
    }

    private fun setupPlayerBottomSheet() {
        val behavior = BottomSheetBehavior.from(binding.playerBottomSheet)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Potential logic for state changes
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Logic for fading elements handled inside PlayerFragment if needed, 
                // but we'll try to keep it simple here.
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, com.musicplayer.services.MediaPlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
    }

    override fun onStop() {
        super.onStop()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.currentSong.collectLatest { song ->
                binding.playerBottomSheet.visibility = if (song != null) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupTabs() {
        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle, fragments)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if (position == 4 || position == 5) {
                val textView = android.widget.TextView(this)
                textView.text = if (position == 4) "BUY" else "EVENT"
                textView.setTextColor(if (tab.isSelected) getColor(R.color.color_active) else getColor(R.color.highlight))
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
                    3 -> R.drawable.ic_tab_playlist
                    else -> R.drawable.ic_tab_settings
                })
                
                if (position == 6) {
                    iconView.imageTintList = android.content.res.ColorStateList.valueOf(
                        if (tab.isSelected) getColor(R.color.color_active) else getColor(R.color.domant)
                    )
                }
                
                tab.customView = customView
            }
        }.attach()
        
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                val view = tab.customView
                if (view is android.widget.TextView) {
                    view.setTextColor(getColor(R.color.color_active))
                } else if (tab.position == 6) {
                    val iconView = view?.findViewById<ImageView>(R.id.tabIcon)
                    iconView?.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.color_active))
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                val view = tab.customView
                if (view is android.widget.TextView) {
                    view.setTextColor(getColor(R.color.highlight))
                } else if (tab.position == 6) {
                    val iconView = view?.findViewById<ImageView>(R.id.tabIcon)
                    iconView?.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.domant))
                }
            }
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })
        
        binding.tabLayout.setSelectedTabIndicatorColor(getColor(R.color.color_active))
        binding.tabLayout.tabIconTint = null
    }

    fun showAddToPlaylistDialog(song: Song) {
        lifecycleScope.launch {
            val playlists = appDao.getAllPlaylistNames()
            if (playlists.isEmpty()) {
                Toast.makeText(this@MainActivity, "No custom playlists found. Create one in the Playlists tab.", Toast.LENGTH_LONG).show()
                return@launch
            }

            val builder = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Add to Playlist")
            
            builder.setItems(playlists.toTypedArray()) { _, which ->
                val selectedPlaylist = playlists[which]
                viewModel.addSongToPlaylist(selectedPlaylist, song)
                Toast.makeText(this@MainActivity, "Added to $selectedPlaylist", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }
    }

    fun showSettingsPopup(view: View) {
        val popup = android.widget.PopupMenu(this, view)
        popup.menu.add("Eq")
        popup.menu.add("Theme")
        popup.menu.add("About")
        
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Eq" -> {
                    com.musicplayer.fragments.EqualizerFragment.newInstance().show(supportFragmentManager, "equalizer")
                    true
                }
                "Theme" -> {
                    Toast.makeText(this, "Theme settings coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                "About" -> {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("About")
                        .setMessage("Music Player v1.0\nAn expert-crafted media experience.")
                        .setPositiveButton("OK", null)
                        .show()
                    true
                }
                else -> false
            }
        }
        popup.show()
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

    @androidx.media3.common.util.UnstableApi
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

    fun showSelectionMenu(folderName: String, onPlayAll: () -> Unit) {
        binding.tvSelectedFolderName.text = folderName
        binding.folderSelectionMenu.visibility = android.view.View.VISIBLE
        binding.btnPlayAll.setOnClickListener {
            onPlayAll()
            hideSelectionMenu()
            // Opening PlaylistActivity is optional now since we have the bottom sheet player,
            // but let's keep it if the user wants to see the list.
            startActivity(Intent(this, PlaylistActivity::class.java))
        }
    }

    private fun checkPermissions() {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS
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
}
