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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    isFavorite: Boolean,
    onSelect: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val revealWidthPx = with(density) { 120.dp.toPx() } // Wider for 2 icons
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PlayerInactive.copy(alpha = 0.2f)) // Highlight color background on swipe
    ) {
        // Revealed Menu (Favorite and Add icons)
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(120.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {
                onFavoriteToggle()
                scope.launch { offsetX.animateTo(0f) }
            }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) PlayerActive else Color.White
                )
            }
            IconButton(onClick = {
                onAddToPlaylist()
                scope.launch { offsetX.animateTo(0f) }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add to Playlist", tint = Color.White)
            }
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
                            } else {
                                Brush.linearGradient(listOf(Color(0xFF1C1C1C), Color(0xFF1C1C1C)))
                            }
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

                // Caret moved to the right and made highlight color
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = PlayerInactive,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
