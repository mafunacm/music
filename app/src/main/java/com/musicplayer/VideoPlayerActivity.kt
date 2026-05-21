package com.musicplayer

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
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
import kotlinx.coroutines.*

@UnstableApi
class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null

    private var repeatA: Long = -1L
    private var repeatB: Long = -1L
    private var abJob: Job? = null

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
        setupGestureDetection()
    }

    private fun setupGestureDetection() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (distanceX != 0f && Math.abs(distanceX) > Math.abs(distanceY)) {
                    val seekAmount = -(distanceX * 100).toLong() // Scale distance to seek time
                    player?.let {
                        it.seekTo(it.currentPosition + seekAmount)
                    }
                    return true
                }
                return false
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (binding.playerView.isControllerFullyVisible) {
                    binding.playerView.hideController()
                } else {
                    binding.playerView.showController()
                }
                return true
            }
        })

        binding.playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this)
            .setSeekForwardIncrementMs(5000L)
            .setSeekBackIncrementMs(5000L)
            .build()
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
            binding.bottomVideoControls.visibility = visibility
            binding.btnBack.visibility = visibility
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
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAspectRatio.setOnClickListener {
            currentRatioIndex = (currentRatioIndex + 1) % aspectRatios.size
            val (mode, label) = aspectRatios[currentRatioIndex]

            binding.playerView.resizeMode = mode
            showModeIndicator(label)
        }

        binding.btnABRepeat.setOnClickListener {
            val pos = player?.currentPosition ?: 0L
            if (repeatA == -1L) {
                repeatA = pos
                binding.btnABRepeat.text = "A - "
                Toast.makeText(this, "A point set", Toast.LENGTH_SHORT).show()
            } else if (repeatB == -1L) {
                if (pos > repeatA) {
                    repeatB = pos
                    binding.btnABRepeat.text = "A - B"
                    binding.btnABRepeat.setTextColor(getColor(R.color.color_active))
                    startABRepeat()
                    Toast.makeText(this, "B point set, repeating", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "B must be after A", Toast.LENGTH_SHORT).show()
                }
            } else {
                stopABRepeat()
                repeatA = -1L
                repeatB = -1L
                binding.btnABRepeat.text = "A - B"
                binding.btnABRepeat.setTextColor(getColor(R.color.white))
                Toast.makeText(this, "Repeat cleared", Toast.LENGTH_SHORT).show()
            }
        }

        // Custom seek logic for tap (5s) and long press (10s) 
        // This is tricky because ExoPlayer's default UI handles these buttons.
        // We'll use a listener to detect button events if we were using custom layouts,
        // but since we are using app:use_controller="true", we might need to override the view IDs.
        // For now, I'll set the increment in setupPlayer.
    }

    private fun startABRepeat() {
        abJob?.cancel()
        abJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                player?.let {
                    if (it.currentPosition >= repeatB) {
                        it.seekTo(repeatA)
                    }
                }
                delay(100)
            }
        }
    }

    private fun stopABRepeat() {
        abJob?.cancel()
        abJob = null
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
