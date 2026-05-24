package com.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.google.common.util.concurrent.ListenableFuture
import com.musicplayer.adapters.ViewPagerAdapter
import com.musicplayer.databinding.ActivityMainBinding
import com.musicplayer.fragments.FolderBrowseFragment
import com.musicplayer.fragments.VideoFolderBrowseFragment
import com.musicplayer.models.MediaItem
import com.musicplayer.models.MediaType
import com.musicplayer.models.Song
import com.musicplayer.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.view.WindowCompat
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val viewModel: MainViewModel by viewModels()

    private val appDao by lazy {
        com.musicplayer.database.AppDatabase.getDatabase(this).appDao()
    }

    private var controllerFuture: ListenableFuture<MediaController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        setupTabs()
        checkPermissions()
        setupSelectionMenu()
        setupPlayerBottomSheet()
        observeViewModel()

        onBackPressedDispatcher.addCallback(
            this,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    val behavior =
                        BottomSheetBehavior.from(binding.playerBottomSheet)

                    if (behavior.state ==
                        BottomSheetBehavior.STATE_EXPANDED
                    ) {
                        behavior.state =
                            BottomSheetBehavior.STATE_COLLAPSED
                        return
                    }

                    val currentFragment =
                        supportFragmentManager.findFragmentByTag(
                            "f" + binding.viewPager.currentItem
                        )

                    if (
                        (currentFragment is FolderBrowseFragment &&
                                currentFragment.handleBackPress()) ||
                        (currentFragment is VideoFolderBrowseFragment &&
                                currentFragment.handleBackPress())
                    ) {
                        return
                    }

                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        )
    }

    private fun setupPlayerBottomSheet() {
        val behavior =
            BottomSheetBehavior.from(binding.playerBottomSheet)

        behavior.addBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(
                    bottomSheet: View,
                    newState: Int
                ) {}

                override fun onSlide(
                    bottomSheet: View,
                    slideOffset: Float
                ) {}
            }
        )
    }

    override fun onStart() {
        super.onStart()

        val sessionToken = SessionToken(
            this,
            ComponentName(
                this,
                com.musicplayer.services.MediaPlaybackService::class.java
            )
        )

        controllerFuture =
            MediaController.Builder(this, sessionToken).buildAsync()
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

                val isVisible = song != null

                binding.playerBottomSheet.visibility =
                    if (isVisible) View.VISIBLE else View.GONE
            }
        }
    }

    // ---------------------------
    // TABS
    // ---------------------------
    private fun setupTabs() {

        val adapter = ViewPagerAdapter(this)

        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = 7

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->

            if (position == 4 || position == 5) {

                val textView = android.widget.TextView(this)

                textView.text =
                    if (position == 4) "BUY" else "EVENT"

                textView.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )

                textView.gravity =
                    android.view.Gravity.CENTER

                tab.customView = textView

            } else {

                val customView =
                    layoutInflater.inflate(
                        R.layout.custom_tab,
                        null
                    )

                val iconView =
                    customView.findViewById<ImageView>(
                        R.id.tabIcon
                    )

                iconView.setImageResource(
                    when (position) {
                        0 -> R.drawable.ic_tab_music
                        1 -> R.drawable.ic_tab_music_folder
                        2 -> R.drawable.ic_tab_video_folder
                        3 -> R.drawable.ic_tab_playlist
                        6 -> R.drawable.ic_tab_settings
                        else -> R.drawable.ic_tab_music
                    }
                )

                tab.customView = customView
            }

        }.attach()

        binding.tabLayout.tabIconTint = null
    }

    // ---------------------------
    // PERMISSIONS
    // ---------------------------
    private fun checkPermissions() {

        val permissions =
            if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.TIRAMISU
            ) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }

        val needPermissions =
            permissions.filter {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()

        if (needPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                needPermissions,
                100
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode != 100 ||
            !grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            Toast.makeText(
                this,
                "Permissions required",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            viewModel.loadMedia()
        }
    }

    // ---------------------------
    // MEDIA PLAY
    // ---------------------------
    fun playMediaItem(
        item: MediaItem,
        playlist: List<MediaItem>
    ) {

        if (item.type == MediaType.VIDEO) {

            viewModel.pauseMedia()

            val intent =
                Intent(this, VideoPlayerActivity::class.java)

            val uris = ArrayList<String>()

            playlist.forEach {
                uris.add(it.uri.toString())
            }

            intent.putStringArrayListExtra(
                "VIDEO_URIS",
                uris
            )

            intent.putExtra(
                "START_INDEX",
                playlist.indexOf(item)
            )

            startActivity(intent)

        } else {

            val folderName =
                item.folderPath.substringAfterLast(
                    java.io.File.separator
                )

            viewModel.playSong(
                item.toSong(),
                playlist.map { it.toSong() },
                folderName
            )
        }
    }

    // ---------------------------
    // UI HELPERS
    // ---------------------------
    private fun setupSelectionMenu() {
        binding.btnCloseSelection.setOnClickListener {
            hideSelectionMenu()
        }
    }

    fun hideSelectionMenu() {
        binding.folderSelectionMenu.visibility = View.GONE
    }

    fun showSelectionMenu(
        folderName: String,
        onPlayAll: () -> Unit
    ) {
        binding.tvSelectedFolderName.text = folderName
        binding.folderSelectionMenu.visibility = View.VISIBLE

        binding.btnPlayAll.setOnClickListener {
            onPlayAll()
            hideSelectionMenu()
        }
    }

    fun showAddToPlaylistDialog(song: Song) {
        lifecycleScope.launch {
            val playlists = appDao.getAllPlaylistNames()
            if (playlists.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "No custom playlists found. Create one in the Playlists tab.",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            val builder = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Add to Playlist")

            builder.setItems(playlists.toTypedArray()) { _, which ->
                val selectedPlaylist = playlists[which]
                viewModel.addSongToPlaylist(selectedPlaylist, song)
                Toast.makeText(
                    this@MainActivity,
                    "Added to $selectedPlaylist",
                    Toast.LENGTH_SHORT
                ).show()
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }
    }
}