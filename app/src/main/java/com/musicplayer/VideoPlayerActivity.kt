package com.musicplayer

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

    private val bottomVideoControls: View? by lazy { binding.playerView.findViewById(R.id.bottomVideoControls) }
    private val progressAndMarkers: View? by lazy { binding.playerView.findViewById(R.id.progressAndMarkers) }
    private val markerA: View? by lazy { binding.playerView.findViewById(R.id.markerA) }
    private val markerB: View? by lazy { binding.playerView.findViewById(R.id.markerB) }
    private val btnAspectRatio: android.widget.ImageButton? by lazy { binding.playerView.findViewById(R.id.btnAspectRatio) }
    private val btnABRepeat: android.widget.TextView? by lazy { binding.playerView.findViewById(R.id.btnABRepeat) }

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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

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
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (distanceX != 0f && kotlin.math.abs(distanceX) > kotlin.math.abs(distanceY)) {
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
            .setSeekForwardIncrementMs(15000L)
            .setSeekBackIncrementMs(15000L)
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
                    updateMarkers()
                } else if (playbackState == Player.STATE_BUFFERING) {
                    Log.d("VideoPlayer", "Player state: BUFFERING")
                }
            }
        })

        binding.playerView.setControllerVisibilityListener(androidx.media3.ui.PlayerView.ControllerVisibilityListener { visibility ->
            bottomVideoControls?.visibility = visibility
            binding.btnBack.visibility = visibility
            if (visibility == View.GONE) binding.tvAspectRatioMode.visibility = View.GONE
            if (visibility == View.VISIBLE) updateMarkers()
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

    private fun updateMarkers() {
        val duration = player?.duration ?: 0L
        if (duration <= 0) return

        if (repeatA != -1L) {
            val progress = repeatA.toFloat() / duration
            val x = binding.playerView.width * progress
            markerA?.translationX = x
            markerA?.visibility = View.VISIBLE
        } else {
            markerA?.visibility = View.GONE
        }

        if (repeatB != -1L) {
            val progress = repeatB.toFloat() / duration
            val x = binding.playerView.width * progress
            markerB?.translationX = x
            markerB?.visibility = View.VISIBLE
        } else {
            markerB?.visibility = View.GONE
        }
    }

    private fun setupControls() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnBack.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.highlight))

        btnAspectRatio?.setOnClickListener {
            currentRatioIndex = (currentRatioIndex + 1) % aspectRatios.size
            val (mode, label) = aspectRatios[currentRatioIndex]

            binding.playerView.resizeMode = mode
            showModeIndicator(label)
            
            // Highlight if not standard (Fit)
            if (currentRatioIndex != 0) {
                btnAspectRatio?.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.color_active))
            } else {
                btnAspectRatio?.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.highlight))
            }
        }

        btnABRepeat?.setOnClickListener {
            val pos = player?.currentPosition ?: 0L
            if (repeatA == -1L) {
                repeatA = pos
                btnABRepeat?.text = "A - "
                btnABRepeat?.setTextColor(getColor(R.color.color_active))
                updateMarkers()
                Toast.makeText(this, "A point set", Toast.LENGTH_SHORT).show()
            } else if (repeatB == -1L) {
                if (pos > repeatA) {
                    repeatB = pos
                    btnABRepeat?.text = "A - B"
                    btnABRepeat?.setTextColor(getColor(R.color.color_active))
                    updateMarkers()
                    startABRepeat()
                    Toast.makeText(this, "B point set, repeating", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "B must be after A", Toast.LENGTH_SHORT).show()
                }
            } else {
                stopABRepeat()
                repeatA = -1L
                repeatB = -1L
                btnABRepeat?.text = "A - B"
                btnABRepeat?.setTextColor(getColor(R.color.highlight))
                updateMarkers()
                Toast.makeText(this, "Repeat cleared", Toast.LENGTH_SHORT).show()
            }
        }
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
