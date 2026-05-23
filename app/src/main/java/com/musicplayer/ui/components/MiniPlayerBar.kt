package com.musicplayer.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicplayer.models.Song
import com.musicplayer.ui.theme.*
import java.util.concurrent.TimeUnit
import androidx.media3.common.Player

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiniPlayerBar(
    song: Song?,
    isPlaying: Boolean,
    isShuffle: Boolean,
    repeatMode: Int,
    progress: Float,
    currentTime: Long,
    totalDuration: Long,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoopOne = repeatMode == Player.REPEAT_MODE_ONE

    Box(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .padding(top = 8.dp)
            .navigationBarsPadding() // 4. Fixed player overlap with nav bar
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xF914141C))
            .clickable { onOpen() }
    ) {
        Column {
            // Progress Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.07f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(PlayerInactive, PlayerActive)))
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                // Row 1: Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onShuffle, 
                        modifier = Modifier.size(36.dp),
                        enabled = !isLoopOne
                    ) {
                        Icon(
                            Icons.Default.Shuffle, 
                            contentDescription = null, 
                            tint = if (isLoopOne) PlayerDormant else if (isShuffle) PlayerActive else PlayerInactive, 
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(
                        onClick = onPrev, 
                        modifier = Modifier.size(36.dp),
                        enabled = !isLoopOne
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious, 
                            contentDescription = null, 
                            tint = if (isLoopOne) PlayerDormant else PlayerInactive, 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Surface(
                        onClick = onPlayPause,
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = if (isPlaying) PlayerActive else PlayerInactive,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onNext, 
                        modifier = Modifier.size(36.dp),
                        enabled = !isLoopOne
                    ) {
                        Icon(
                            Icons.Default.SkipNext, 
                            contentDescription = null, 
                            tint = if (isLoopOne) PlayerDormant else PlayerInactive, 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onRepeat, modifier = Modifier.size(36.dp)) {
                        val repeatIcon = when (repeatMode) {
                            Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        }
                        val repeatColor = if (repeatMode == Player.REPEAT_MODE_OFF) PlayerInactive else PlayerActive
                        Icon(
                            imageVector = repeatIcon,
                            contentDescription = null,
                            tint = repeatColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Track Info + Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(22.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = PlayerInactive.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = PlayerInactive, modifier = Modifier.size(12.dp))
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = song?.title ?: "Not Playing",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1f)
                                    .basicMarquee(iterations = Int.MAX_VALUE)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            // 5. Duration moved up to align with title
                            Text(
                                text = "${formatDuration(currentTime)} / ${formatDuration(totalDuration)}",
                                color = PlayerSubtext,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = song?.artist ?: "Unknown Artist",
                            color = PlayerSubtext,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }

                    // Up caret in highlight color
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Open",
                        tint = PlayerInactive,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Icon(
                        Icons.Default.KeyboardArrowDown, 
                        contentDescription = null, 
                        tint = PlayerDormant, 
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    if (ms < 0) return "00:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
