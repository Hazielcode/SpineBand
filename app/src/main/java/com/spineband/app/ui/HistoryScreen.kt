package com.spineband.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spineband.app.ui.theme.*

data class DayData(
    val day: String,
    val date: String,
    val goodPostureMinutes: Int,
    val badPostureMinutes: Int,
    val alerts: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Datos simulados (después serán de la BD)
    val weekData = remember {
        listOf(
            DayData("Lun", "11/11", 180, 60, 5),
            DayData("Mar", "12/11", 200, 40, 3),
            DayData("Mié", "13/11", 150, 90, 8),
            DayData("Jue", "14/11", 220, 20, 2),
            DayData("Vie", "15/11", 190, 50, 4),
            DayData("Sáb", "16/11", 170, 70, 6),
            DayData("Dom", "17/11", 210, 30, 3)
        )
    }

    val totalGoodPosture = weekData.sumOf { it.goodPostureMinutes }
    val totalBadPosture = weekData.sumOf { it.badPostureMinutes }
    val totalTime = totalGoodPosture + totalBadPosture
    val goodPosturePercent = if (totalTime > 0) (totalGoodPosture * 100) / totalTime else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SpineBandWhite,
                        SpineBandOffWhite
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        "Historial",
                        fontWeight = FontWeight.Bold,
                        color = SpineBandNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = SpineBandNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SpineBandWhite
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Resumen Semanal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SpineBandWhite
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = SpineBandCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Resumen Semanal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpineBandNavy
                            )
                        }

                        // Porcentaje circular
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { goodPosturePercent / 100f },
                                modifier = Modifier.size(120.dp),
                                color = SpineBandGreen,
                                trackColor = SpineBandLightGray,
                                strokeWidth = 12.dp
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$goodPosturePercent%",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SpineBandGreen
                                )
                                Text(
                                    text = "Buena",
                                    fontSize = 14.sp,
                                    color = SpineBandDarkGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Estadísticas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            WeekStatColumn(
                                label = "Buena Postura",
                                value = "${totalGoodPosture / 60}h ${totalGoodPosture % 60}m",
                                color = SpineBandGreen
                            )

                            WeekStatColumn(
                                label = "Mala Postura",
                                value = "${totalBadPosture / 60}h ${totalBadPosture % 60}m",
                                color = SpineBandOrange
                            )
                        }
                    }
                }

                // Gráfico de Barras Semanal
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                text = "Últimos 7 Días",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpineBandNavy
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Barras simples
                        weekData.forEach { day ->
                            DayBarChart(day)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                // Lista de Días
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = SpineBandCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Detalle por Día",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpineBandNavy
                            )
                        }

                        weekData.forEach { day ->
                            DayDetailRow(day)
                            if (day != weekData.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = SpineBandLightGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun WeekStatColumn(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
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
            fontSize = 12.sp,
            color = SpineBandDarkGray
        )
    }
}

@Composable
fun DayBarChart(day: DayData) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${day.day} ${day.date}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SpineBandNavy,
                modifier = Modifier.width(70.dp)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                val total = day.goodPostureMinutes + day.badPostureMinutes
                val goodPercent = if (total > 0) day.goodPostureMinutes.toFloat() / total else 0f

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(goodPercent)
                        .background(SpineBandGreen)
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f - goodPercent)
                        .background(SpineBandOrange)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${(day.goodPostureMinutes * 100) / (day.goodPostureMinutes + day.badPostureMinutes)}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandGreen,
                modifier = Modifier.width(45.dp)
            )
        }
    }
}

@Composable
fun DayDetailRow(day: DayData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "${day.day} ${day.date}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )
            Text(
                text = "${day.goodPostureMinutes + day.badPostureMinutes} min total",
                fontSize = 12.sp,
                color = SpineBandDarkGray
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = SpineBandOrange,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${day.alerts} alertas",
                fontSize = 12.sp,
                color = SpineBandDarkGray
            )
        }
    }
}