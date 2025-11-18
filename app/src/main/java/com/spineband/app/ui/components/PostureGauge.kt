package com.spineband.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spineband.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PostureGauge(
    angle: Float,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val currentAngle by animateFloatAsState(
        targetValue = if (animationPlayed) angle else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "angle"
    )

    LaunchedEffect(angle) {
        animationPlayed = true
    }

    Box(
        modifier = modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = canvasWidth / 2 * 0.8f
            val strokeWidth = 20f

            // Arco de fondo (0-50°)
            val sections = listOf(
                0f to 15f to SpineBandGreen,      // Excelente
                15f to 25f to SpineBandCyan,      // Buena
                25f to 35f to SpineBandOrange,    // Regular
                35f to 50f to SpineBandRed        // Mala
            )

            sections.forEach { (range, color) ->
                val startAngle = 135f + (range.first / 50f) * 270f
                val sweepAngle = ((range.second - range.first) / 50f) * 270f

                drawArc(
                    color = color.copy(alpha = 0.3f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(
                        (canvasWidth - radius * 2) / 2,
                        (canvasHeight - radius * 2) / 2
                    ),
                    size = Size(radius * 2, radius * 2)
                )
            }

            // Marcadores de grados
            listOf(0f, 15f, 25f, 35f, 50f).forEach { degree ->
                val angleDeg = 135f + (degree / 50f) * 270f
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val startRadius = radius * 0.85f
                val endRadius = radius * 0.95f

                val startX = canvasWidth / 2 + startRadius * cos(angleRad).toFloat()
                val startY = canvasHeight / 2 + startRadius * sin(angleRad).toFloat()
                val endX = canvasWidth / 2 + endRadius * cos(angleRad).toFloat()
                val endY = canvasHeight / 2 + endRadius * sin(angleRad).toFloat()

                drawLine(
                    color = SpineBandDarkGray,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }

            // Aguja indicadora
            val needleAngle = 135f + (currentAngle.coerceIn(0f, 50f) / 50f) * 270f
            rotate(needleAngle, pivot = Offset(canvasWidth / 2, canvasHeight / 2)) {
                val needleLength = radius * 0.7f
                drawLine(
                    color = SpineBandNavy,
                    start = Offset(canvasWidth / 2, canvasHeight / 2),
                    end = Offset(canvasWidth / 2, canvasHeight / 2 - needleLength),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
            }

            // Centro de la aguja
            drawCircle(
                color = SpineBandNavy,
                radius = 12f,
                center = Offset(canvasWidth / 2, canvasHeight / 2)
            )
        }

        // Valor actual
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 40.dp)
        ) {
            Text(
                text = "%.1f°".format(currentAngle),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )
            Text(
                text = when {
                    currentAngle <= 15 -> "Excelente"
                    currentAngle <= 25 -> "Buena"
                    currentAngle <= 35 -> "Regular"
                    else -> "Mala"
                },
                fontSize = 14.sp,
                color = when {
                    currentAngle <= 15 -> SpineBandGreen
                    currentAngle <= 25 -> SpineBandCyan
                    currentAngle <= 35 -> SpineBandOrange
                    else -> SpineBandRed
                },
                fontWeight = FontWeight.Medium
            )
        }
    }
}