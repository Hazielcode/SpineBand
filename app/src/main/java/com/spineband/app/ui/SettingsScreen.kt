package com.spineband.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spineband.app.data.AppSettings
import com.spineband.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onNavigateBack: () -> Unit
) {

    // VARIABLES CORREGIDAS ✔✔✔
    var esp32IP by remember { mutableStateOf(settings.esp32IP) }
    var sensitivity by remember { mutableStateOf(settings.sensitivityLevel) } // FLOAT ✔
    var checkInterval by remember { mutableStateOf(settings.checkInterval.toString()) }
    var notificationsEnabled by remember { mutableStateOf(settings.notificationsEnabled) }
    var vibrationEnabled by remember { mutableStateOf(settings.vibrationEnabled) }
    var showSaveMessage by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

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

            TopAppBar(
                title = {
                    Text(
                        "Configuración",
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // ------------------------------
                // ESP32 CONFIG
                // ------------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpineBandWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Wifi,
                                contentDescription = null,
                                tint = SpineBandCyan
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Conexión ESP32",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpineBandNavy
                            )
                        }

                        OutlinedTextField(
                            value = esp32IP,
                            onValueChange = { esp32IP = it },
                            label = { Text("Dirección IP") },
                            placeholder = { Text("192.168.1.100") },
                            leadingIcon = { Icon(Icons.Default.Router, null) },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "IP actual: $esp32IP",
                            fontSize = 12.sp,
                            color = SpineBandDarkGray
                        )
                    }
                }

                // ------------------------------
                // SENSIBILIDAD
                // ------------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpineBandWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = null,
                                tint = SpineBandCyan
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Sensibilidad",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpineBandNavy
                            )
                        }

                        Text(
                            text = "Nivel: ${sensitivity.toInt()}°",
                            fontSize = 14.sp,
                            color = SpineBandDarkGray
                        )

                        // SLIDER CORREGIDO ✔✔✔
                        Slider(
                            value = sensitivity,
                            onValueChange = { sensitivity = it },
                            valueRange = 5f..30f,
                            steps = 24,
                            colors = SliderDefaults.colors(
                                thumbColor = SpineBandCyan,
                                activeTrackColor = SpineBandCyan,
                                inactiveTrackColor = SpineBandLightGray
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Baja (5°)", fontSize = 12.sp, color = SpineBandDarkGray)
                            Text("Alta (30°)", fontSize = 12.sp, color = SpineBandDarkGray)
                        }
                    }
                }

                // ------------------------------
                // INTERVALO DE CHEQUEO
                // ------------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpineBandWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, null, tint = SpineBandCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Intervalo de Chequeo",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpineBandNavy
                            )
                        }

                        OutlinedTextField(
                            value = checkInterval,
                            onValueChange = {
                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                    checkInterval = it
                                }
                            },
                            label = { Text("Segundos") },
                            leadingIcon = { Icon(Icons.Default.Schedule, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ------------------------------
                // NOTIFICACIONES
                // ------------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpineBandWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, null, tint = SpineBandCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Notificaciones",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SpineBandNavy
                                )
                                Text(
                                    "Alertas de mala postura",
                                    fontSize = 12.sp,
                                    color = SpineBandDarkGray
                                )
                            }
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                }

                // ------------------------------
                // MENSAJE DE GUARDADO
                // ------------------------------
                if (showSaveMessage) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SpineBandGreen.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = SpineBandGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Configuración guardada",
                                color = SpineBandGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // ------------------------------
                // BOTÓN GUARDAR (CORREGIDO)
                // ------------------------------
                Button(
                    onClick = {
                        settings.esp32IP = esp32IP
                        settings.sensitivityLevel = sensitivity
                        settings.checkInterval = checkInterval.toFloatOrNull() ?: 2f
                        settings.notificationsEnabled = notificationsEnabled
                        settings.vibrationEnabled = vibrationEnabled
                        showSaveMessage = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpineBandCyan,
                        contentColor = SpineBandWhite
                    )
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

