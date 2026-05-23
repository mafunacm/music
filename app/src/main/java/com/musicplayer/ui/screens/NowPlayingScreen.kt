package com.musicplayer.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicplayer.models.Song
import com.musicplayer.ui.components.*
import com.musicplayer.ui.theme.*
import java.util.concurrent.TimeUnit
import androidx.media3.common.Player

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    song: Song?,
    isPlaying: Boolean,
    isShuffle: Boolean,
    repeatMode: Int,
    currentTime: Long,
    totalDuration: Long,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    isFavorite: Boolean
) {
    val isLoopOne = repeatMode == Player.REPEAT_MODE_ONE

    Box(modifier = Modifier.fillMaxSize()) {
        WaveBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()) // Prevent chopping
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close", tint = PlayerDormant)
                }
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = PlayerSubtext,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(onClick = { /* More */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = PlayerDormant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Album Art
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AlbumArt(imageUrl = song?.path, size = 180.dp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Track Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song?.title ?: "No Track Playing",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                    Text(
                        text = song?.artist ?: "Unknown Artist",
                        color = PlayerSubtext,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) PlayerActive else PlayerDormant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seekable Progress Bar (Slider styled as a strip)
            Column(modifier = Modifier.fillMaxWidth()) {
                val progress = if (totalDuration > 0) currentTime.toFloat() / totalDuration else 0f
                var sliderValue by remember(progress) { mutableFloatStateOf(progress) }

                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = {
                        onSeek((sliderValue * totalDuration).toLong())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = PlayerActive,
                        activeTrackColor = Color.Transparent, 
                        inactiveTrackColor = Color.Transparent
                    ),
                    track = { sliderState ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(sliderState.value.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(Brush.horizontalGradient(listOf(PlayerInactive, PlayerActive)))
                            )
                        }
                    },
                    thumb = {
                        Spacer(
                            modifier = Modifier
                                .size(12.dp)
                                .background(PlayerActive, CircleShape)
                                .border(2.dp, Color.Black, CircleShape)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${formatDuration(currentTime)} / ${formatDuration(totalDuration)}",
                    color = PlayerSubtext,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Transport Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onShuffle,
                    enabled = !isLoopOne
                ) {
                    Icon(
                        Icons.Default.Shuffle, 
                        contentDescription = "Shuffle", 
                        tint = if (isLoopOne) PlayerDormant else if (isShuffle) PlayerActive else PlayerInactive
                    )
                }
                IconButton(
                    onClick = onPrev,
                    enabled = !isLoopOne
                ) {
                    Icon(
                        Icons.Default.SkipPrevious, 
                        contentDescription = "Prev", 
                        tint = if (isLoopOne) PlayerDormant else PlayerInactive, 
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Surface(
                    onClick = onPlayPause,
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = if (isPlaying) PlayerActive else PlayerInactive,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onNext,
                    enabled = !isLoopOne
                ) {
                    Icon(
                        Icons.Default.SkipNext, 
                        contentDescription = null, 
                        tint = if (isLoopOne) PlayerDormant else PlayerInactive, 
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(onClick = onRepeat) {
                    val repeatIcon = when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    }
                    val repeatColor = if (repeatMode == Player.REPEAT_MODE_OFF) PlayerInactive else PlayerActive
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = repeatColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Gig callout
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = PlayerInactive.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PlayerInactive.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = PlayerInactive.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = PlayerInactive, modifier = Modifier.size(16.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Catch me live", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Vibes on Main · 17 June 2026", color = PlayerSubtext, fontSize = 12.sp)
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PlayerInactive
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Black, modifier = Modifier.size(10.dp))
                            Text("Save", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Buy More
            Button(
                onClick = { /* Buy */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PlayerActive.copy(alpha = 0.1f),
                    contentColor = PlayerActive
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, PlayerActive.copy(alpha = 0.25f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Buy More Music", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatDuration(ms: Long): String {
    if (ms < 0) return "00:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
