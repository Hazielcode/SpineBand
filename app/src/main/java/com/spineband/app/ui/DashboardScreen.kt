package com.spineband.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
    userId: Int,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }

    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            postureRecordDao = database.postureRecordDao(),
            userId = userId,
            esp32IP = esp32IP,
            context = context
        )
    )

    val scrollState = rememberScrollState()

    val isConnected by viewModel.isConnected.collectAsState()
    val currentAngle by viewModel.currentAngle.collectAsState()
    val currentStatus by viewModel.currentStatus.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val sessionHistory by viewModel.sessionHistory.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()
    val sessionDuration by viewModel.sessionDuration.collectAsState()
    val badPostureAlert by viewModel.badPostureAlert.collectAsState()
    val alertsEnabled by viewModel.alertsEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleAlerts(true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadSessionHistory()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
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

            AnimatedVisibility(
                visible = badPostureAlert,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpineBandRed),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "⚠️ ¡CORRIGE TU POSTURA!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Llevas más de 10 segundos en mala posición",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.dismissAlert() }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ConnectionStatusCard(
                    isConnected = isConnected,
                    esp32IP = esp32IP
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (alertsEnabled) SpineBandCyan.copy(alpha = 0.1f) else SpineBandLightGray
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (alertsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = if (alertsEnabled) SpineBandCyan else SpineBandDarkGray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Alertas de Postura",
                                    fontWeight = FontWeight.Bold,
                                    color = SpineBandNavy
                                )
                                Text(
                                    if (alertsEnabled) "Vibración y sonido activos" else "Alertas desactivadas",
                                    fontSize = 12.sp,
                                    color = SpineBandDarkGray
                                )
                            }
                        }

                        Switch(
                            checked = alertsEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) != PackageManager.PERMISSION_GRANTED) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.toggleAlerts(true)
                                    }
                                } else {
                                    viewModel.toggleAlerts(enabled)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SpineBandCyan,
                                checkedTrackColor = SpineBandCyan.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

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

                PostureLineChart(records = chartData)

                DailyStatsCard(stats = todayStats)

                SessionHistoryList(records = sessionHistory)

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