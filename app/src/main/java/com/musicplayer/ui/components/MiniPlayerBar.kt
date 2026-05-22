package com.musicplayer.ui.components

import androidx.compose.foundation.background
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

@Composable
fun MiniPlayerBar(
    song: Song?,
    isPlaying: Boolean,
    isShuffle: Boolean,
    isRepeat: Boolean,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .padding(top = 16.dp, bottom = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xF914141C))
            .clickable { onOpen() }
    ) {
        Column {
            // Progress Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.07f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.14f)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(PlayerInactive, PlayerActive)))
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onShuffle, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, tint = if (isShuffle) PlayerActive else PlayerInactive, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onPrev, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = PlayerInactive, modifier = Modifier.size(20.dp))
                    }
                    Surface(
                        onClick = onPlayPause,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = if (isPlaying) PlayerActive else PlayerInactive,
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.SkipNext, contentDescription = null, tint = PlayerInactive, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onRepeat, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isRepeat) Icons.Default.RepeatOne else Icons.Default.Repeat,
                            contentDescription = null,
                            tint = if (isRepeat) PlayerActive else PlayerInactive,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Track Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(20.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = PlayerInactive.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = PlayerInactive, modifier = Modifier.size(11.dp))
                        }
                    }
                    Text(
                        text = song?.title ?: "Not Playing",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                        // TODO: Add basicMarquee here if available
                    )
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = PlayerDormant, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
