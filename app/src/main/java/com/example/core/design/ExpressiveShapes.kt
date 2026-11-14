package com.example.core.design

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Material 3 Expressive scalloped organic flower/cookie shape (e.g. 8-lobed or 12-lobed badge).
 * Used for Tasbeeh tactile button, Surah badges, and Qibla center emblem.
 */
class ScallopedStarShape(
    private val lobes: Int = 8,
    private val depthFraction: Float = 0.11f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = min(size.width, size.height) / 2f
        val steps = lobes * 16

        for (i in 0..steps) {
            val angle = (2.0 * PI * i) / steps
            val wave = cos(lobes * angle).toFloat()
            val r = radius * (1f - depthFraction * (1f - wave) / 2f)
            val x = cx + r * cos(angle).toFloat()
            val y = cy + r * sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

/**
 * Material 3 Expressive 4-sided rounded cookie / clover shape from the M3 Expressive design system.
 */
class ExpressiveCookie4Shape(
    private val indentation: Float = 0.14f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rx = size.width / 2f
        val ry = size.height / 2f
        val steps = 96

        for (i in 0..steps) {
            val theta = (2.0 * PI * i) / steps
            // 4 lobes rotated by 45 degrees
            val modulation = 1f - indentation * (0.5f + 0.5f * cos(4.0 * theta).toFloat())
            val x = cx + rx * modulation * cos(theta).toFloat()
            val y = cy + ry * modulation * sin(theta).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

/**
 * Architectural Mihrab pointed arch top shape for featured Islamic cards.
 */
class MihrabArchShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val archHeight = h * 0.24f
        val path = Path().apply {
            moveTo(0f, h)
            lineTo(0f, archHeight)
            cubicTo(
                0f, archHeight * 0.35f,
                w * 0.28f, archHeight * 0.18f,
                w * 0.5f, 0f
            )
            cubicTo(
                w * 0.72f, archHeight * 0.18f,
                w, archHeight * 0.35f,
                w, archHeight
            )
            lineTo(w, h)
            close()
        }
        return Outline.Generic(path)
    }
}
