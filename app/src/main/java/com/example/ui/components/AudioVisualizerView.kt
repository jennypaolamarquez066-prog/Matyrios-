package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPrimary
import com.example.ui.theme.GeminiTertiary
import kotlin.math.sin

@Composable
fun AudioVisualizerView(
    waveformData: List<Float>,
    isPlaying: Boolean,
    progressFraction: Float,
    amplitude: Float = 0f,
    modifier: Modifier = Modifier.fillMaxWidth().height(48.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barCount = if (waveformData.isNotEmpty()) waveformData.size else 36
        val barWidth = (width / (barCount * 1.5f)).coerceAtLeast(3f)
        val spacing = barWidth * 0.5f

        val activeGradient = Brush.verticalGradient(
            colors = listOf(GeminiPink, GeminiPrimary, GeminiTertiary)
        )
        val inactiveGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
        )

        for (i in 0 until barCount) {
            val baseAmp = waveformData.getOrNull(i) ?: 0.4f
            val dynamicBoost = if (isPlaying) {
                (sin(pulse + i * 0.35f) * 0.25f + amplitude * 0.4f).coerceAtLeast(0f)
            } else 0f

            val barHeight = ((baseAmp + dynamicBoost) * height).coerceIn(4f, height)
            val x = i * (barWidth + spacing) + spacing
            val y = (height - barHeight) / 2f

            val barProgress = i.toFloat() / barCount
            val isPlayed = barProgress <= progressFraction

            drawRoundRect(
                brush = if (isPlayed) activeGradient else inactiveGradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
