package com.musicplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.musicplayer.ui.theme.*

@Composable
fun WaveBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val anim1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "wv1"
    )

    val anim2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "wv2"
    )

    val anim3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "wv3"
    )

    Box(modifier = Modifier.fillMaxSize().background(DeepBlue)) {
        // WV1
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = -0.04f * size.width * anim1
                translationY = 0.03f * size.height * anim1
                scaleX = 1.1f + 0.05f * anim1
                scaleY = 1.1f + 0.05f * anim1
            }
            .blur(40.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PlayerInactive.copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(size.width * 0.4f, size.height * 0.5f),
                    radius = size.width * 0.6f
                )
            )
        }

        // WV2
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = 0.05f * size.width * anim2
                translationY = -0.04f * size.height * anim2
                scaleX = 1.1f + 0.15f * anim2
                scaleY = 1.1f + 0.15f * anim2
            }
            .blur(50.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AccentPurple.copy(alpha = 0.4f), DarkPurple.copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(size.width * 0.6f, size.height * 0.6f),
                    radius = size.width * 0.7f
                )
            )
        }

        // WV3
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = -0.05f * size.width * anim3
                translationY = 0.05f * size.height * anim3
                scaleX = 1.15f + 0.05f * anim3
                scaleY = 1.15f + 0.05f * anim3
            }
            .blur(60.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PlayerActive.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    radius = size.width * 0.4f
                )
            )
        }

        // Overlay darken
        Box(modifier = Modifier.fillMaxSize().background(Color(0x8C05050F)))
    }
}
