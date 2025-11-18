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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spineband.app.ui.theme.*
import com.spineband.app.viewmodel.AuthState
import com.spineband.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(
    onNavigateBack: () -> Unit,
    onSurveyComplete: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var hoursSeated by remember { mutableStateOf<String?>(null) }
    var worksWithPC by remember { mutableStateOf(false) }
    var hasBackPain by remember { mutableStateOf<String?>(null) }
    var doesExercise by remember { mutableStateOf<String?>(null) }
    var motivation by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val authState by viewModel.authState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onSurveyComplete()
                viewModel.resetAuthState()
            }
            is AuthState.Error -> {
                errorMessage = (authState as AuthState.Error).message
            }
            else -> {}
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
                .padding(32.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Encuesta Inicial",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )

            Text(
                text = "Paso 3 de 3: Cuéntanos sobre ti",
                fontSize = 14.sp,
                color = SpineBandCyan,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                color = SpineBandCyan,
                trackColor = SpineBandLightGray
            )

            Text(
                text = "1. ¿Cuántas horas pasas sentado al día?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SpineBandNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("0-2 horas", "3-5 horas", "6-8 horas", "Más de 8 horas").forEach { option ->
                    FilterChip(
                        selected = hoursSeated == option,
                        onClick = {
                            hoursSeated = option
                            errorMessage = null
                        },
                        label = { Text(option) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpineBandCyan,
                            selectedLabelColor = SpineBandWhite,
                            containerColor = SpineBandWhite,
                            labelColor = SpineBandBlack
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = hoursSeated == option,
                            borderColor = SpineBandGray,
                            selectedBorderColor = SpineBandCyan,
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            Text(
                text = "2. ¿Trabajas frente a una computadora?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SpineBandNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = worksWithPC,
                    onClick = { worksWithPC = true },
                    label = { Text("Sí") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpineBandCyan,
                        selectedLabelColor = SpineBandWhite,
                        containerColor = SpineBandWhite,
                        labelColor = SpineBandBlack
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = worksWithPC,
                        borderColor = SpineBandGray,
                        selectedBorderColor = SpineBandCyan,
                        borderWidth = 1.dp
                    )
                )

                FilterChip(
                    selected = !worksWithPC,
                    onClick = { worksWithPC = false },
                    label = { Text("No") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpineBandCyan,
                        selectedLabelColor = SpineBandWhite,
                        containerColor = SpineBandWhite,
                        labelColor = SpineBandBlack
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = !worksWithPC,
                        borderColor = SpineBandGray,
                        selectedBorderColor = SpineBandCyan,
                        borderWidth = 1.dp
                    )
                )
            }

            Text(
                text = "3. ¿Has tenido dolor de espalda?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SpineBandNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Nunca", "A veces", "Frecuentemente", "Siempre").forEach { option ->
                    FilterChip(
                        selected = hasBackPain == option,
                        onClick = {
                            hasBackPain = option
                            errorMessage = null
                        },
                        label = { Text(option) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpineBandCyan,
                            selectedLabelColor = SpineBandWhite,
                            containerColor = SpineBandWhite,
                            labelColor = SpineBandBlack
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = hasBackPain == option,
                            borderColor = SpineBandGray,
                            selectedBorderColor = SpineBandCyan,
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            Text(
                text = "4. ¿Haces ejercicio regularmente?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SpineBandNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Nunca", "1-2 veces/semana", "3-4 veces/semana", "Diario").forEach { option ->
                    FilterChip(
                        selected = doesExercise == option,
                        onClick = {
                            doesExercise = option
                            errorMessage = null
                        },
                        label = { Text(option) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpineBandCyan,
                            selectedLabelColor = SpineBandWhite,
                            containerColor = SpineBandWhite,
                            labelColor = SpineBandBlack
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = doesExercise == option,
                            borderColor = SpineBandGray,
                            selectedBorderColor = SpineBandCyan,
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            Text(
                text = "5. ¿Por qué quieres mejorar tu postura?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SpineBandNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = motivation,
                onValueChange = {
                    motivation = it
                    errorMessage = null
                },
                placeholder = { Text("Escribe tu respuesta...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = 16.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpineBandCyan,
                    unfocusedBorderColor = SpineBandGray,
                    focusedLabelColor = SpineBandCyan,
                    unfocusedLabelColor = SpineBandDarkGray,
                    cursorColor = SpineBandCyan,
                    focusedTextColor = SpineBandBlack,
                    unfocusedTextColor = SpineBandBlack,
                    focusedPlaceholderColor = SpineBandDarkGray,
                    unfocusedPlaceholderColor = SpineBandDarkGray
                )
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = SpineBandRed,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    enabled = authState !is AuthState.Loading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SpineBandCyan
                    )
                ) {
                    Text("Atrás", fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        if (hoursSeated == null || hasBackPain == null ||
                            doesExercise == null || motivation.isBlank()) {
                            errorMessage = "Por favor completa todas las preguntas"
                            return@Button
                        }

                        viewModel.registerData.hoursSeated = hoursSeated!!
                        viewModel.registerData.worksWithPC = worksWithPC
                        viewModel.registerData.hasBackPain = hasBackPain!!
                        viewModel.registerData.doesExercise = doesExercise!!
                        viewModel.registerData.motivation = motivation.trim()

                        viewModel.completeRegistration()
                    },
                    enabled = authState !is AuthState.Loading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpineBandCyan,
                        contentColor = SpineBandWhite
                    )
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            color = SpineBandWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Finalizar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
