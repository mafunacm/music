package com.musicplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SpectrumBars(color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.height(18.dp).width(20.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Bar(color, 450, 0.3f, 0.95f)
        Bar(color, 350, 0.75f, 0.2f)
        Bar(color, 550, 0.5f, 1.0f)
        Bar(color, 400, 0.85f, 0.15f)
    }
}

@Composable
private fun RowScope.Bar(color: Color, duration: Int, startHeight: Float, targetHeight: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "bar")
    val height by infiniteTransition.animateFloat(
        initialValue = startHeight,
        targetValue = targetHeight,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "height"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(height.coerceIn(0.1f, 1.0f))
            .background(color, RoundedCornerShape(2.dp))
    )
}
