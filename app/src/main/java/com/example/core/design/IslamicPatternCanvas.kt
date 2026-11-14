package com.example.core.design

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Draws an understated Islamic 8-pointed Rub el Hizb geometric watermark in the background
 * of hero cards or compass views for Material 3 Expressive surface depth.
 */
fun Modifier.islamicStarWatermark(
    color: Color,
    alpha: Float = 0.08f,
    centerBiasX: Float = 0.88f,
    centerBiasY: Float = 0.28f,
    scaleFactor: Float = 0.72f
): Modifier = drawBehind {
    val cx = size.width * centerBiasX
    val cy = size.height * centerBiasY
    val radius = min(size.width, size.height) * scaleFactor
    val strokeColor = color.copy(alpha = alpha)

    // Draw two rotated overlapping squares (Rub el Hizb)
    for (squareIndex in 0..1) {
        val rotationOffset = squareIndex * (PI / 4.0)
        val path = Path()
        for (corner in 0..3) {
            val angle = rotationOffset + corner * (PI / 2.0)
            val x = cx + radius * cos(angle).toFloat()
            val y = cy + radius * sin(angle).toFloat()
            if (corner == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, color = strokeColor, style = Stroke(width = 2.5f))
    }

    // Concentric delicate rings
    drawCircle(
        color = strokeColor,
        radius = radius * 0.58f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f)
    )
    drawCircle(
        color = strokeColor,
        radius = radius * 0.34f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f)
    )
}
