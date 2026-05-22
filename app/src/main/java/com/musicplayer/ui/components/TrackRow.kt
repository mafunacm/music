package com.musicplayer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicplayer.models.Song
import com.musicplayer.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun TrackRow(
    song: Song,
    isActive: Boolean,
    isPlaying: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val revealWidthPx = with(density) { 80.dp.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D0D0D)) // Base background
    ) {
        // Revealed Menu (Background Layer)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(80.dp)
                .fillMaxHeight()
                .background(PlayerActive.copy(alpha = 0.12f))
                .clickable {
                    scope.launch { offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
                    // Trigger "More" action here if needed
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "More",
                color = PlayerActive,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Foreground Content
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val target = if (offsetX.value < -revealWidthPx / 2) -revealWidthPx else 0f
                            scope.launch {
                                offsetX.animateTo(target, spring(stiffness = Spring.StiffnessMediumLow))
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (offsetX.value + dragAmount).coerceIn(-revealWidthPx, 0f)
                            scope.launch { offsetX.snapTo(newOffset) }
                        }
                    )
                }
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isActive) PlayerActive.copy(alpha = 0.07f) else Color(0xFF121212)
                )
                .border(
                    width = 1.dp,
                    color = if (isActive) PlayerActive.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable {
                    if (offsetX.value != 0f) {
                        scope.launch { offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
                    } else {
                        onSelect()
                    }
                }
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
}
