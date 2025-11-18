package com.spineband.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spineband.app.data.database.entities.PostureRecord
import com.spineband.app.ui.theme.*

@Composable
fun PostureLineChart(
    records: List<PostureRecord>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SpineBandWhite)
            .padding(16.dp)
    ) {
        // Título
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Últimos 60 segundos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )

            // Leyenda
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendItem("Ángulo", SpineBandCyan)
                LegendItem("Ideal (25°)", SpineBandGreen)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gráfico
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Esperando datos...",
                    color = SpineBandDarkGray,
                    fontSize = 14.sp
                )
            }
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val maxAngle = 50f
                val padding = 40f

                // Líneas de referencia horizontales
                val gridLines = listOf(0f, 15f, 25f, 35f, 50f)
                gridLines.forEach { angle ->
                    val y = canvasHeight - (angle / maxAngle * (canvasHeight - padding * 2)) - padding
                    drawLine(
                        color = SpineBandLightGray,
                        start = Offset(padding, y),
                        end = Offset(canvasWidth - padding, y),
                        strokeWidth = 1f
                    )
                }

                // Línea de "postura ideal" (25°)
                val idealY = canvasHeight - (25f / maxAngle * (canvasHeight - padding * 2)) - padding
                drawLine(
                    color = SpineBandGreen.copy(alpha = 0.5f),
                    start = Offset(padding, idealY),
                    end = Offset(canvasWidth - padding, idealY),
                    strokeWidth = 3f
                )

                // Dibujar path del ángulo
                if (records.size > 1) {
                    val path = Path()
                    val stepX = (canvasWidth - padding * 2) / (records.size - 1)

                    records.forEachIndexed { index, record ->
                        val x = padding + index * stepX
                        val normalizedAngle = record.angle.coerceIn(0f, maxAngle)
                        val y = canvasHeight - (normalizedAngle / maxAngle * (canvasHeight - padding * 2)) - padding

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = SpineBandCyan,
                        style = Stroke(width = 4f)
                    )

                    // Puntos en la línea
                    records.forEachIndexed { index, record ->
                        val x = padding + index * stepX
                        val normalizedAngle = record.angle.coerceIn(0f, maxAngle)
                        val y = canvasHeight - (normalizedAngle / maxAngle * (canvasHeight - padding * 2)) - padding

                        drawCircle(
                            color = if (record.isGoodPosture) SpineBandGreen else SpineBandOrange,
                            radius = 6f,
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Labels del eje Y
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0°", fontSize = 10.sp, color = SpineBandDarkGray)
            Text("15°", fontSize = 10.sp, color = SpineBandDarkGray)
            Text("25°", fontSize = 10.sp, color = SpineBandGreen, fontWeight = FontWeight.Bold)
            Text("35°", fontSize = 10.sp, color = SpineBandDarkGray)
            Text("50°", fontSize = 10.sp, color = SpineBandDarkGray)
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = SpineBandDarkGray
        )
    }
}