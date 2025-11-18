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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spineband.app.data.database.AppDatabase
import com.spineband.app.ui.components.*
import com.spineband.app.ui.theme.*
import com.spineband.app.viewmodel.DashboardViewModel
import com.spineband.app.viewmodel.DashboardViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    esp32IP: String,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }

    // TODO: Obtener userId real del usuario actual logueado
    val userId = 1 // Temporal - reemplazar con el usuario real

    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            postureRecordDao = database.postureRecordDao(),
            userId = userId,
            esp32IP = esp32IP
        )
    )

    val scrollState = rememberScrollState()

    // Estados del ViewModel
    val isConnected by viewModel.isConnected.collectAsState()
    val currentAngle by viewModel.currentAngle.collectAsState()
    val currentStatus by viewModel.currentStatus.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val sessionHistory by viewModel.sessionHistory.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()
    val sessionDuration by viewModel.sessionDuration.collectAsState()
    val badPostureAlert by viewModel.badPostureAlert.collectAsState()

    // Cargar historial al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadSessionHistory()
    }

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
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "SpineBand",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = SpineBandNavy
                        )
                        Text(
                            "Monitor Inteligente",
                            fontSize = 12.sp,
                            color = SpineBandCyan
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = SpineBandNavy
                        )
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Historial",
                            tint = SpineBandNavy
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = SpineBandNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SpineBandWhite
                )
            )

            // Alerta de mala postura
            PostureAlertBanner(
                show = badPostureAlert,
                onDismiss = { viewModel.dismissAlert() }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Estado de Conexión
                ConnectionStatusCard(
                    isConnected = isConnected,
                    esp32IP = esp32IP
                )

                // Medidor de Postura (Gauge)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SpineBandWhite
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Estado Actual",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpineBandNavy
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PostureGauge(angle = currentAngle)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Cronómetro de sesión
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = SpineBandCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = formatSessionDuration(sessionDuration),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = SpineBandNavy
                            )
                        }
                    }
                }

                // Gráfico en tiempo real
                PostureLineChart(records = chartData)

                // Estadísticas del día
                DailyStatsCard(stats = todayStats)

                // Historial de sesión
                SessionHistoryList(records = sessionHistory)

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.resetSession() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SpineBandOrange
                        )
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reiniciar")
                    }

                    Button(
                        onClick = { viewModel.calibrate() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpineBandCyan
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calibrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ConnectionStatusCard(
    isConnected: Boolean,
    esp32IP: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                SpineBandGreen.copy(alpha = 0.1f)
            else
                SpineBandRed.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isConnected) SpineBandGreen else SpineBandRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isConnected) "✓ Conectado" else "✗ Desconectado",
                    fontWeight = FontWeight.Bold,
                    color = if (isConnected) SpineBandGreen else SpineBandRed,
                    fontSize = 16.sp
                )
                Text(
                    text = "ESP32: $esp32IP",
                    fontSize = 12.sp,
                    color = SpineBandDarkGray
                )
            }
        }
    }
}

private fun formatSessionDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, secs)
    } else {
        "%02d:%02d".format(minutes, secs)
    }
}