package com.spineband.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spineband.app.ui.theme.*
import com.spineband.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = SpineBandRed
                    )
                ) {
                    Text("Cerrar Sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SpineBandCyan,
                    titleContentColor = SpineBandWhite,
                    navigationIconContentColor = SpineBandWhite
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar y nombre
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(SpineBandCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser?.name?.firstOrNull()?.uppercase() ?: "U",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpineBandWhite
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentUser?.name ?: "Usuario",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpineBandNavy
                )

                Text(
                    text = currentUser?.email ?: "",
                    fontSize = 14.sp,
                    color = SpineBandDarkGray
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Información personal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SpineBandWhite
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Información Personal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpineBandNavy,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        ProfileInfoRow(
                            icon = Icons.Default.FitnessCenter,
                            label = "Peso",
                            value = currentUser?.weight?.let { "${it.toInt()} kg" } ?: "No especificado"
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = SpineBandLightGray
                        )

                        ProfileInfoRow(
                            icon = Icons.Default.Height,
                            label = "Altura",
                            value = currentUser?.height?.let { "${it.toInt()} cm" } ?: "No especificado"
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = SpineBandLightGray
                        )

                        ProfileInfoRow(
                            icon = Icons.Default.Cake,
                            label = "Edad",
                            value = currentUser?.age?.let { "$it años" } ?: "No especificado"
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = SpineBandLightGray
                        )

                        ProfileInfoRow(
                            icon = Icons.Default.Person,
                            label = "Género",
                            value = when (currentUser?.gender) {
                                "M" -> "Masculino"
                                "F" -> "Femenino"
                                "Otro" -> "Otro"
                                else -> "No especificado"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Información de cuenta
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SpineBandWhite
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Información de Cuenta",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpineBandNavy,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        ProfileInfoRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Miembro desde",
                            value = formatDate(currentUser?.createdAt ?: 0L)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón de cerrar sesión
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpineBandRed,
                        contentColor = SpineBandWhite
                    )
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = SpineBandCyan,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = SpineBandDarkGray
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SpineBandNavy
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}