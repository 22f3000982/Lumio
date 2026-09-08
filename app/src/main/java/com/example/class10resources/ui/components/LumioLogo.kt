package com.example.class10resources.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.class10resources.ui.theme.AccentGlowYellow
import com.example.class10resources.ui.theme.AccentNavy

/**
 * Lumio Brand Logo:
 * - Stylized rounded 'L' combined with a soft glowing spark / light beam
 * - Deep navy / indigo background with warm yellow / white ambient glow
 * - Clean, rounded, minimal, intelligent, premium aesthetic
 */
@Composable
fun LumioIcon(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val canvasWidth = this.size.width
        val canvasHeight = this.size.height
        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
        val radius = canvasWidth / 2f

        // 1. Deep Navy / Indigo rounded circular canvas
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1E2958),
                    Color(0xFF090D1E)
                ),
                center = center,
                radius = radius
            ),
            radius = radius
        )

        // 2. Soft subtle warm glow ambient halo around spark position
        val sparkPos = Offset(canvasWidth * 0.60f, canvasHeight * 0.40f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AccentGlowYellow.copy(alpha = 0.35f),
                    Color(0xFFFEF08A).copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = sparkPos,
                radius = radius * 0.55f
            ),
            center = sparkPos,
            radius = radius * 0.55f
        )

        // 3. Subtle angled light ray
        val rayPath = Path().apply {
            moveTo(canvasWidth * 0.42f, canvasHeight * 0.65f)
            lineTo(canvasWidth * 0.72f, canvasHeight * 0.32f)
            lineTo(canvasWidth * 0.78f, canvasHeight * 0.38f)
            lineTo(canvasWidth * 0.48f, canvasHeight * 0.71f)
            close()
        }
        drawPath(
            path = rayPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFDE047).copy(alpha = 0.25f),
                    Color(0xFFFEF9C3).copy(alpha = 0.05f)
                )
            )
        )

        // 4. Stylized Minimal Rounded 'L'
        // Vertical stem + smooth curve + horizontal foot
        val lPath = Path().apply {
            val stemLeft = canvasWidth * 0.33f
            val stemWidth = canvasWidth * 0.11f
            val stemTop = canvasHeight * 0.30f
            val baseBottom = canvasHeight * 0.72f
            val footRight = canvasWidth * 0.67f
            val cornerR = canvasWidth * 0.09f

            // Start at top-left of stem (rounded)
            moveTo(stemLeft, stemTop + stemWidth / 2)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    stemLeft, stemTop,
                    stemLeft + stemWidth, stemTop + stemWidth
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            // Down the right side of the stem
            lineTo(stemLeft + stemWidth, baseBottom - stemWidth - cornerR * 0.5f)
            // Inner corner curve
            quadraticTo(
                stemLeft + stemWidth, baseBottom - stemWidth,
                stemLeft + stemWidth + cornerR * 0.5f, baseBottom - stemWidth
            )
            // Out along top of foot
            lineTo(footRight - stemWidth / 2, baseBottom - stemWidth)
            // Right end of foot (rounded)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    footRight - stemWidth, baseBottom - stemWidth,
                    footRight, baseBottom
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            // Back along bottom of foot
            lineTo(stemLeft + cornerR, baseBottom)
            // Outer smooth rounded corner of 'L'
            quadraticTo(
                stemLeft, baseBottom,
                stemLeft, baseBottom - cornerR
            )
            // Up the left side of the stem
            lineTo(stemLeft, stemTop + stemWidth / 2)
            close()
        }

        drawPath(
            path = lPath,
            color = Color.White
        )

        // 5. 4-Point Radiant Warm Yellow / White Spark
        val sparkSize = canvasWidth * 0.16f
        val sx = sparkPos.x
        val sy = sparkPos.y

        val sparkPath = Path().apply {
            moveTo(sx, sy - sparkSize)
            quadraticTo(sx, sy, sx + sparkSize, sy)
            quadraticTo(sx, sy, sx, sy + sparkSize)
            quadraticTo(sx, sy, sx - sparkSize, sy)
            quadraticTo(sx, sy, sx, sy - sparkSize)
            close()
        }
        drawPath(
            path = sparkPath,
            color = AccentGlowYellow
        )

        // Inner radiant core (warm white)
        val coreSize = sparkSize * 0.55f
        val corePath = Path().apply {
            moveTo(sx, sy - coreSize)
            quadraticTo(sx, sy, sx + coreSize, sy)
            quadraticTo(sx, sy, sx, sy + coreSize)
            quadraticTo(sx, sy, sx - coreSize, sy)
            quadraticTo(sx, sy, sx, sy - coreSize)
            close()
        }
        drawPath(
            path = corePath,
            color = Color.White
        )
    }
}

/**
 * Full Lumio Brand Header with Icon + Wordmark
 */
@Composable
fun LumioBrandHeader(
    modifier: Modifier = Modifier,
    iconSize: Dp = 38.dp,
    showSubtitle: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        LumioIcon(size = iconSize)

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Lumio",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (showSubtitle) {
                Text(
                    text = "Concepts First • Intelligent Learning",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
