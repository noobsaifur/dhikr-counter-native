package com.countdhikr.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
    animateBackground: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")

    // Slow sinusoidal floating animations
    val animationTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Primary base color
    val baseColor = MaterialTheme.colorScheme.background

    // Define mesh colors based on theme
    val mesh1 = if (darkTheme) Color(0xFF0F0D2B) else Color(0xFFC5F2DF)
    val mesh2 = if (darkTheme) Color(0xFF060B24) else Color(0xFFFEF6D9)
    val mesh3 = if (darkTheme) Color(0xFF05172C) else Color(0xFFD9E8F5)
    val mesh4 = if (darkTheme) Color(0xFF090918) else Color(0xFFD1EDE2)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor)
            .drawBehind {
                val width = size.width
                val height = size.height

                if (!animateBackground) {
                    // Beautiful high-fidelity static gradients (drawn once, cached, no frame ticks)
                    val c1 = Offset(width * 0.15f, height * 0.15f)
                    val c2 = Offset(width * 0.85f, height * 0.25f)
                    val c3 = Offset(width * 0.35f, height * 0.85f)
                    val c4 = Offset(width * 0.90f, height * 0.80f)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(mesh1.copy(alpha = 0.8f), Color.Transparent),
                            center = c1,
                            radius = width * 0.70f
                        ),
                        center = c1,
                        radius = width * 0.70f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(mesh2.copy(alpha = 0.7f), Color.Transparent),
                            center = c2,
                            radius = width * 0.55f
                        ),
                        center = c2,
                        radius = width * 0.55f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(mesh3.copy(alpha = 0.65f), Color.Transparent),
                            center = c3,
                            radius = width * 0.65f
                        ),
                        center = c3,
                        radius = width * 0.65f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(mesh4.copy(alpha = 0.55f), Color.Transparent),
                            center = c4,
                            radius = width * 0.50f
                        ),
                        center = c4,
                        radius = width * 0.50f
                    )
                } else {
                    // Draw moving sinusoidal dynamic gradients (animated path, frame ticks)
                    val c1X = width * (0.15f + 0.05f * sin(animationTime))
                    val c1Y = height * (0.15f + 0.05f * cos(animationTime))
                    val r1 = width * 0.70f

                    val c2X = width * (0.85f - 0.06f * cos(animationTime + 1f))
                    val c2Y = height * (0.25f + 0.05f * sin(animationTime + 1f))
                    val r2 = width * 0.55f

                    val c3X = width * (0.35f + 0.07f * sin(animationTime - 2f))
                    val c3Y = height * (0.85f - 0.05f * cos(animationTime - 2f))
                    val r3 = width * 0.65f

                    val c4X = width * (0.90f - 0.05f * cos(animationTime + 3f))
                    val c4Y = height * (0.80f + 0.04f * sin(animationTime + 3f))
                    val r4 = width * 0.50f

                    // Draw Ellipse 1 (Emerald Aurora)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(mesh1.copy(alpha = 0.8f), Color.Transparent),
                            center = Offset(c1X, c1Y),
                            radius = r1
                        ),
                        center = Offset(c1X, c1Y),
                        radius = r1
                    )

                    // Draw Ellipse 2 (Golden Aurora)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(mesh2.copy(alpha = 0.7f), Color.Transparent),
                            center = Offset(c2X, c2Y),
                            radius = r2
                        ),
                        center = Offset(c2X, c2Y),
                        radius = r2
                    )

                    // Draw Ellipse 3 (Blueish Aurora)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(mesh3.copy(alpha = 0.65f), Color.Transparent),
                            center = Offset(c3X, c3Y),
                            radius = r3
                        ),
                        center = Offset(c3X, c3Y),
                        radius = r3
                    )

                    // Draw Ellipse 4 (Muted Teal Aurora)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(mesh4.copy(alpha = 0.55f), Color.Transparent),
                            center = Offset(c4X, c4Y),
                            radius = r4
                        ),
                        center = Offset(c4X, c4Y),
                        radius = r4
                    )
                }
            },
        content = content
    )
}
