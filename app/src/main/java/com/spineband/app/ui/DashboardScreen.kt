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
import com.spineband.app.data.SpineBandApi
import com.spineband.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    esp32IP: String,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit  // ← AGREGADO
) {
    var angleX by remember { mutableStateOf(0f) }
    var angleY by remember { mutableStateOf(0f) }
    var angleZ by remember { mutableStateOf(0f) }
    var isGoodPosture by remember { mutableStateOf(true) }
    var isConnected by remember { mutableStateOf(false) }
    var sessionTime by remember { mutableStateOf(0) }
    var goodPostureTime by remember { mutableStateOf(0) }
    var badPostureTime by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()

    // Obtener datos del ESP32
    LaunchedEffect(esp32IP) {
        val apiInstance = SpineBandApi("http://$esp32IP")

        while (true) {
            try {
                val response = apiInstance.getPostureData()

                if (response != null) {
                    angleX = response.angleX
                    angleY = response.angleY
                    angleZ = response.angleZ
                    isGoodPosture = kotlin.math.abs(angleX) < 15 &&
                            kotlin.math.abs(angleY) < 15
                    isConnected = true

                    sessionTime++
                    if (isGoodPosture) goodPostureTime++ else badPostureTime++
                } else {
                    isConnected = false
                }
            } catch (e: Exception) {
                isConnected = false
            }
            delay(1000)
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
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
                            "Monitor en Tiempo Real",
                            fontSize = 12.sp,
                            color = SpineBandCyan
                        )
                    }
                },
                actions = {
                    // ✅ BOTÓN DE PERFIL AGREGADO
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Estado de Conexión
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
                                text = if (isConnected) "Conectado" else "Desconectado",
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

                // Estado de Postura Actual
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
                        Icon(
                            if (isGoodPosture) Icons.Default.SentimentSatisfied
                            else Icons.Default.SentimentDissatisfied,
                            contentDescription = null,
                            tint = if (isGoodPosture) SpineBandGreen else SpineBandOrange,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isGoodPosture) "Buena Postura" else "Mala Postura",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGoodPosture) SpineBandGreen else SpineBandOrange
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isGoodPosture)
                                "¡Excelente! Mantén esta posición"
                            else
                                "Endereza tu espalda",
                            fontSize = 14.sp,
                            color = SpineBandDarkGray
                        )
                    }
                }

                // Ángulos en Tiempo Real
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
                                Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = SpineBandCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Ángulos",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpineBandNavy
                            )
                        }

                        // Ángulo X
                        AngleRow(
                            label = "Eje X (Inclinación)",
                            value = angleX,
                            color = SpineBandCyan
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Ángulo Y
                        AngleRow(
                            label = "Eje Y (Rotación)",
                            value = angleY,
                            color = SpineBandGreen
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Ángulo Z
                        AngleRow(
                            label = "Eje Z (Lateral)",
                            value = angleZ,
                            color = SpineBandOrange
                        )
                    }
                }

                // Estadísticas de la Sesión
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
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = SpineBandCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sesión Actual",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpineBandNavy
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatColumn(
                                label = "Tiempo Total",
                                value = formatTime(sessionTime),
                                color = SpineBandNavy
                            )

                            StatColumn(
                                label = "Buena Postura",
                                value = formatTime(goodPostureTime),
                                color = SpineBandGreen
                            )

                            StatColumn(
                                label = "Mala Postura",
                                value = formatTime(badPostureTime),
                                color = SpineBandOrange
                            )
                        }
                    }
                }

                // Botón Calibrar
                Button(
                    onClick = {
                        // TODO: Llamar a API de calibración
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpineBandCyan,
                        contentColor = SpineBandWhite
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calibrar Sensor", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AngleRow(label: String, value: Float, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = SpineBandDarkGray
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format("%.1f°", value),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun StatColumn(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
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

fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}