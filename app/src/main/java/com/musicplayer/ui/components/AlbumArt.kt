package com.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.musicplayer.ui.theme.PlayerInactive

@Composable
fun AlbumArt(
    imageUrl: String?,
    size: Dp = 240.dp,
    modifier: Modifier = Modifier
) {
    // imageUrl is ignored for now to avoid compilation errors without a loader
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 32.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = PlayerInactive.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1a1a2e), Color(0xFF16213e), Color(0xFF0f3460))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glide integration omitted for now, using placeholder
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(size * 0.3f),
            tint = PlayerInactive.copy(alpha = 0.4f)
        )
    }
}

