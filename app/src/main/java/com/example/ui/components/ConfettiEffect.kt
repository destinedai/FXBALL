package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class ConfettiParticle(
    val initialX: Float,
    val speedX: Float,
    val speedY: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFF00E676),
            Color(0xFFFFD600),
            Color(0xFF2979FF),
            Color(0xFFFF1744),
            Color(0xFF00E5FF),
            Color(0xFFFF9100)
        )
        List(40) {
            ConfettiParticle(
                initialX = Random.nextFloat(),
                speedX = (Random.nextFloat() - 0.5f) * 160f,
                speedY = 600f + Random.nextFloat() * 500f,
                size = 12f + Random.nextFloat() * 12f,
                color = colors[Random.nextInt(colors.size)],
                rotationSpeed = (Random.nextFloat() - 0.5f) * 360f
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
        )
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            particles.forEach { p ->
                val x = (p.initialX * w) + (p.speedX * progress.value)
                val y = -40f + (p.speedY * progress.value)
                val alpha = (1f - progress.value).coerceIn(0f, 1f)

                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(x, y),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}
