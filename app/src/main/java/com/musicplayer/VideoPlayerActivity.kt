package com.musicplayer

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import com.musicplayer.databinding.ActivityVideoPlayerBinding
import com.musicplayer.playback.PlayerManager
import androidx.media3.common.MediaItem as ExoMediaItem

@UnstableApi
class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null
    
    private val aspectRatios = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT to "Fit to Screen",
        AspectRatioFrameLayout.RESIZE_MODE_FILL to "Stretch",
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM to "Fill Screen",
        AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH to "Fixed Width",
        AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT to "Fixed Height"
    )
    private var currentRatioIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide status bar and navigation bar for full screen video
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pause music player when video starts
        PlayerManager.getInstance(this).pause()

        setupPlayer()
        setupControls()
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        
        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("VideoPlayer", "Playback error: ${error.message} (code: ${error.errorCode})", error)
                Toast.makeText(this@VideoPlayerActivity, "Error playing video: ${error.message}", Toast.LENGTH_LONG).show()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    Log.d("VideoPlayer", "Player state: READY")
                } else if (playbackState == Player.STATE_BUFFERING) {
                    Log.d("VideoPlayer", "Player state: BUFFERING")
                }
            }
        })

        binding.playerView.setControllerVisibilityListener(androidx.media3.ui.PlayerView.ControllerVisibilityListener { visibility ->
            binding.btnAspectRatio.visibility = visibility
            binding.videoControls.visibility = visibility
            if (visibility == View.GONE) binding.tvAspectRatioMode.visibility = View.GONE
        })

        val videoUris = intent.getStringArrayListExtra("VIDEO_URIS")
        val startIndex = intent.getIntExtra("START_INDEX", 0)

        Log.d("VideoPlayer", "URIs received: ${videoUris?.size}, start index: $startIndex")

        if (!videoUris.isNullOrEmpty()) {
            val mediaItems = videoUris.map { 
                Log.d("VideoPlayer", "Loading URI: $it")
                ExoMediaItem.fromUri(Uri.parse(it)) 
            }
            player?.setMediaItems(mediaItems, startIndex, 0L)
            player?.prepare()
            player?.play()
        } else {
            Log.e("VideoPlayer", "No URIs received in intent")
            Toast.makeText(this, "No videos to play", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupControls() {
        binding.btnAspectRatio.setOnClickListener {
            currentRatioIndex = (currentRatioIndex + 1) % aspectRatios.size
            val (mode, label) = aspectRatios[currentRatioIndex]
            
            binding.playerView.resizeMode = mode
            showModeIndicator(label)
        }

        binding.btnVideoNext.setOnClickListener {
            if (player?.hasNextMediaItem() == true) {
                player?.seekToNextMediaItem()
            } else {
                Toast.makeText(this, "End of playlist", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnVideoPrev.setOnClickListener {
            if (player?.hasPreviousMediaItem() == true) {
                player?.seekToPreviousMediaItem()
            }
        }
    }

    private fun showModeIndicator(label: String) {
        binding.tvAspectRatioMode.text = label
        binding.tvAspectRatioMode.visibility = View.VISIBLE
        binding.tvAspectRatioMode.removeCallbacks(hideIndicatorRunnable)
        binding.tvAspectRatioMode.postDelayed(hideIndicatorRunnable, 2000)
    }

    private val hideIndicatorRunnable = Runnable {
        binding.tvAspectRatioMode.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
