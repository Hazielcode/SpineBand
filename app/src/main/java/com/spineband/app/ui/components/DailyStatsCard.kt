package com.spineband.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spineband.app.ui.theme.*
import com.spineband.app.viewmodel.DashboardStats

@Composable
fun DailyStatsCard(
    stats: DashboardStats?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SpineBandWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = SpineBandCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Estadísticas de Hoy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpineBandNavy
                )
            }

            if (stats == null || stats.totalRecords == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin datos aún",
                        color = SpineBandDarkGray,
                        fontSize = 14.sp
                    )
                }
            } else {
                // Gráfico circular de porcentaje
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gráfico circular animado
                    AnimatedCircularProgress(
                        percentage = stats.goodPosturePercentage,
                        modifier = Modifier.size(120.dp)
                    )

                    // Estadísticas
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatsRow(
                            icon = Icons.Default.Timer,
                            label = "Tiempo total",
                            value = "${stats.totalTimeMinutes} min",
                            color = SpineBandNavy
                        )
                        StatsRow(
                            icon = Icons.Default.CheckCircle,
                            label = "Buena postura",
                            value = "${stats.goodPostureCount}",
                            color = SpineBandGreen
                        )
                        StatsRow(
                            icon = Icons.Default.Warning,
                            label = "Mala postura",
                            value = "${stats.badPostureCount}",
                            color = SpineBandOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Promedio y mejores/peores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MiniStatCard(
                        label = "Promedio",
                        value = "%.1f°".format(stats.averageAngle),
                        color = SpineBandCyan
                    )
                    MiniStatCard(
                        label = "Mejor",
                        value = "%.1f°".format(stats.bestAngle),
                        color = SpineBandGreen
                    )
                    MiniStatCard(
                        label = "Peor",
                        value = "%.1f°".format(stats.worstAngle),
                        color = SpineBandOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedCircularProgress(
    percentage: Float,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val currentPercentage by animateFloatAsState(
        targetValue = if (animationPlayed) percentage else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "percentage"
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val strokeWidth = canvasSize * 0.12f
            val radius = (canvasSize - strokeWidth) / 2

            // Fondo del círculo
            drawArc(
                color = SpineBandLightGray,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset((canvasSize - radius * 2) / 2, (canvasSize - radius * 2) / 2)
            )

            // Progreso
            val sweepAngle = (currentPercentage / 100f) * 360f
            drawArc(
                color = if (percentage >= 70) SpineBandGreen else if (percentage >= 50) SpineBandOrange else SpineBandRed,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset((canvasSize - radius * 2) / 2, (canvasSize - radius * 2) / 2)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%.0f%%".format(currentPercentage),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )
            Text(
                text = "Calidad",
                fontSize = 12.sp,
                color = SpineBandDarkGray
            )
        }
    }
}

@Composable
private fun StatsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = SpineBandDarkGray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = SpineBandDarkGray
        )
    }
}