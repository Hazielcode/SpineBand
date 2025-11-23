package com.spineband.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spineband.app.data.database.AppDatabase
import com.spineband.app.ui.theme.*
import com.spineband.app.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    userId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }

    val viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(
            postureRecordDao = database.postureRecordDao(),
            userId = userId
        )
    )

    // Estados del ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val weekSummary by viewModel.weekSummary.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    val allTimeStats by viewModel.allTimeStats.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Semana", "Mes", "Todo")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SpineBandWhite, SpineBandOffWhite)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timeline,
                            contentDescription = null,
                            tint = SpineBandCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Historial Completo",
                            fontWeight = FontWeight.Bold,
                            color = SpineBandNavy
                        )
                    }
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
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = SpineBandCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SpineBandWhite
                )
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SpineBandCyan)
                }
            } else {
                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SpineBandWhite,
                    contentColor = SpineBandNavy
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Contenido según tab seleccionada
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Vista Semanal
                            weekSummary?.let { week ->
                                item {
                                    WeeklySummaryCard(week)
                                }

                                item {
                                    WeeklyChartCard(week)
                                }

                                items(week.dailyStats) { day ->
                                    DayDetailCard(day)
                                }
                            }
                        }
                        1 -> {
                            // Vista Mensual
                            item {
                                MonthlyCalendarCard(monthlyStats)
                            }

                            items(monthlyStats) { day ->
                                DayDetailCard(day)
                            }
                        }
                        2 -> {
                            // Vista Todo el Tiempo
                            allTimeStats?.let { stats ->
                                item {
                                    AllTimeStatsCard(stats)
                                }

                                item {
                                    AchievementsCard(stats)
                                }

                                item {
                                    TrendChartCard(stats)
                                }
                            } ?: item {
                                NoDataCard()
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========== COMPONENTES DE CARDS ==========

@Composable
fun WeeklySummaryCard(weekSummary: WeekSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpineBandWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Resumen Semanal",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Porcentaje circular grande
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(150.dp)
            ) {
                CircularProgressIndicator(
                    progress = { weekSummary.goodPosturePercentage / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = when {
                        weekSummary.goodPosturePercentage >= 80 -> SpineBandGreen
                        weekSummary.goodPosturePercentage >= 60 -> SpineBandOrange
                        else -> SpineBandRed
                    },
                    trackColor = SpineBandLightGray,
                    strokeWidth = 16.dp
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${weekSummary.goodPosturePercentage}%",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpineBandNavy
                    )
                    Text(
                        "Buena Postura",
                        fontSize = 14.sp,
                        color = SpineBandDarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Estadísticas en fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(
                    icon = Icons.Default.CheckCircle,
                    value = "${weekSummary.totalGoodMinutes / 60}h ${weekSummary.totalGoodMinutes % 60}m",
                    label = "Buena Postura",
                    color = SpineBandGreen
                )

                StatColumn(
                    icon = Icons.Default.Warning,
                    value = "${weekSummary.totalBadMinutes / 60}h ${weekSummary.totalBadMinutes % 60}m",
                    label = "Mala Postura",
                    color = SpineBandOrange
                )

                StatColumn(
                    icon = Icons.Default.NotificationsActive,
                    value = weekSummary.totalAlerts.toString(),
                    label = "Alertas",
                    color = SpineBandRed
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Indicador de tendencia
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when(weekSummary.trend) {
                        "improving" -> SpineBandGreen.copy(alpha = 0.1f)
                        "declining" -> SpineBandRed.copy(alpha = 0.1f)
                        else -> SpineBandCyan.copy(alpha = 0.1f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when(weekSummary.trend) {
                            "improving" -> Icons.Default.TrendingUp
                            "declining" -> Icons.Default.TrendingDown
                            else -> Icons.Default.TrendingFlat
                        },
                        contentDescription = null,
                        tint = when(weekSummary.trend) {
                            "improving" -> SpineBandGreen
                            "declining" -> SpineBandRed
                            else -> SpineBandCyan
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when(weekSummary.trend) {
                            "improving" -> "¡Mejorando! 📈"
                            "declining" -> "Necesitas mejorar 📉"
                            else -> "Manteniéndote estable →"
                        },
                        fontWeight = FontWeight.Medium,
                        color = SpineBandNavy
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyChartCard(weekSummary: WeekSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpineBandWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Progreso Diario",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Gráfico de barras
            weekSummary.dailyStats.forEach { day ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Día y fecha
                    Column(
                        modifier = Modifier.width(60.dp)
                    ) {
                        Text(
                            day.dayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpineBandNavy
                        )
                        Text(
                            day.dateString,
                            fontSize = 12.sp,
                            color = SpineBandDarkGray
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Barra de progreso
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SpineBandLightGray)
                    ) {
                        Row(Modifier.fillMaxSize()) {
                            if (day.totalMinutes > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(day.goodPostureMinutes.toFloat() / day.totalMinutes)
                                        .background(SpineBandGreen)
                                )
                            }
                        }

                        // Texto centrado con porcentaje
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${day.goodPosturePercentage}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tiempo total
                    Text(
                        "${day.totalMinutes}m",
                        fontSize = 12.sp,
                        color = SpineBandDarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun DayDetailCard(day: DayStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpineBandWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fecha e indicador
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                day.goodPosturePercentage >= 80 -> SpineBandGreen
                                day.goodPosturePercentage >= 60 -> SpineBandOrange
                                else -> SpineBandRed
                            }
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        "${day.dayName} ${day.dateString}",
                        fontWeight = FontWeight.Bold,
                        color = SpineBandNavy
                    )
                    Text(
                        "${day.totalMinutes} min monitoreados",
                        fontSize = 12.sp,
                        color = SpineBandDarkGray
                    )
                }
            }

            // Estadísticas rápidas
            Row(
                horizontalArrangement = Arrangement.End  // ✅ CORRECTO
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "${day.goodPosturePercentage}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            day.goodPosturePercentage >= 80 -> SpineBandGreen
                            day.goodPosturePercentage >= 60 -> SpineBandOrange
                            else -> SpineBandRed
                        }
                    )

                    Row {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = SpineBandOrange
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${day.alertCount} alertas",
                            fontSize = 11.sp,
                            color = SpineBandDarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyCalendarCard(monthlyStats: List<DayStats>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpineBandWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Vista Mensual",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )

            // Aquí iría un calendario visual
            // Por ahora mostramos resumen
            if (monthlyStats.isNotEmpty()) {
                val avgPercentage = monthlyStats.map { it.goodPosturePercentage }.average().toInt()
                val totalHours = monthlyStats.sumOf { it.totalMinutes } / 60

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "$avgPercentage%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpineBandCyan
                        )
                        Text(
                            "Promedio",
                            fontSize = 12.sp,
                            color = SpineBandDarkGray
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${monthlyStats.size}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpineBandNavy
                        )
                        Text(
                            "Días activos",
                            fontSize = 12.sp,
                            color = SpineBandDarkGray
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${totalHours}h",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpineBandGreen
                        )
                        Text(
                            "Total",
                            fontSize = 12.sp,
                            color = SpineBandDarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllTimeStatsCard(stats: AllTimeStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpineBandWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFFFFD700), // Dorado
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Estadísticas Totales",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )

            Spacer(modifier = Modifier.height(8.dp))

            stats.firstUseDate?.let { date ->
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
                Text(
                    "Usando SpineBand desde ${dateFormat.format(date)}",
                    fontSize = 14.sp,
                    color = SpineBandDarkGray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid de estadísticas
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(
                        value = stats.totalDays.toString(),
                        label = "Días activos",
                        icon = Icons.Default.CalendarMonth
                    )
                    StatBox(
                        value = "${stats.totalHours}h",
                        label = "Horas totales",
                        icon = Icons.Default.Schedule
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(
                        value = "${stats.overallGoodPosturePercentage}%",
                        label = "Postura correcta",
                        icon = Icons.Default.CheckCircle
                    )
                    StatBox(
                        value = stats.totalAlerts.toString(),
                        label = "Alertas totales",
                        icon = Icons.Default.NotificationsActive
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementsCard(stats: AllTimeStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpineBandCyan.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Stars,
                    contentDescription = null,
                    tint = SpineBandCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Logros Destacados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpineBandNavy
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mejor día
            stats.bestDayEver?.let { day ->
                AchievementRow(
                    icon = Icons.Default.Star,
                    title = "Mejor día: ${day.goodPosturePercentage}%",
                    subtitle = "${day.dayName} ${day.dateString}",
                    color = SpineBandGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Racha más larga (simulado por ahora)
            AchievementRow(
                icon = Icons.Default.LocalFireDepartment,
                title = "Racha más larga: ${stats.totalDays} días",
                subtitle = "¡Sigue así!",
                color = SpineBandOrange
            )
        }
    }
}

@Composable
fun TrendChartCard(stats: AllTimeStats) {
    // Placeholder para gráfico de tendencia futura
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpineBandWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Evolución General",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Gráfico de tendencias próximamente...",
                fontSize = 14.sp,
                color = SpineBandDarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NoDataCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpineBandWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Sensors,
                contentDescription = null,
                tint = SpineBandCyan,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Sin datos aún",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Conecta tu SpineBand para comenzar a registrar tu postura",
                fontSize = 14.sp,
                color = SpineBandDarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ========== COMPONENTES AUXILIARES ==========

@Composable
fun StatColumn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            fontSize = 12.sp,
            color = SpineBandDarkGray
        )
    }
}

@Composable
fun StatBox(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.size(140.dp),
        colors = CardDefaults.cardColors(containerColor = SpineBandOffWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = SpineBandCyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )
            Text(
                label,
                fontSize = 11.sp,
                color = SpineBandDarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AchievementRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color = SpineBandDarkGray
            )
        }
    }
}