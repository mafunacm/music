package com.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
fun TrackRow(
    song: Song,
    isActive: Boolean,
    isPlaying: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isActive) PlayerActive.copy(alpha = 0.07f) else Color(0xCC121212)
            )
            .border(
                width = 1.dp,
                color = if (isActive) PlayerActive.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = PlayerDormant.copy(alpha = 0.35f),
                modifier = Modifier.size(13.dp)
            )

            // Thumb
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isActive) {
                            Brush.linearGradient(
                                listOf(PlayerActive.copy(alpha = 0.18f), AccentPurple.copy(alpha = 0.28f))
                            )
                        } else Brush.linearGradient(listOf(Color(0xFF1C1C1C), Color(0xFF1C1C1C)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isActive && isPlaying) {
                    SpectrumBars(color = PlayerActive)
                } else {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isActive) PlayerActive else PlayerDormant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = if (isActive) PlayerActive else Color(0xFFBBBBBB),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = song.artist ?: "Unknown Artist",
                    color = PlayerSubtext,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
