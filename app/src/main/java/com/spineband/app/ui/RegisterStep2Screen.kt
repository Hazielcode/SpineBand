package com.spineband.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spineband.app.ui.theme.*
import com.spineband.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterStep2Screen(
    onNavigateBack: () -> Unit,
    onNavigateToSurvey: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Datos Físicos",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )

            Text(
                text = "Paso 2 de 3: Información personal",
                fontSize = 14.sp,
                color = SpineBandCyan,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            LinearProgressIndicator(
                progress = { 0.66f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                color = SpineBandCyan,
                trackColor = SpineBandLightGray
            )

            OutlinedTextField(
                value = weight,
                onValueChange = {
                    if (it.isEmpty() || it.toFloatOrNull() != null) {
                        weight = it
                        errorMessage = null
                    }
                },
                label = { Text("Peso (kg) - Opcional") },
                leadingIcon = {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpineBandCyan,
                    unfocusedBorderColor = SpineBandGray,
                    focusedLabelColor = SpineBandCyan,
                    unfocusedLabelColor = SpineBandDarkGray,
                    cursorColor = SpineBandCyan,
                    focusedTextColor = SpineBandBlack,
                    unfocusedTextColor = SpineBandBlack
                )
            )

            OutlinedTextField(
                value = height,
                onValueChange = {
                    if (it.isEmpty() || it.toFloatOrNull() != null) {
                        height = it
                        errorMessage = null
                    }
                },
                label = { Text("Altura (cm) - Opcional") },
                leadingIcon = {
                    Icon(Icons.Default.Height, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpineBandCyan,
                    unfocusedBorderColor = SpineBandGray,
                    focusedLabelColor = SpineBandCyan,
                    unfocusedLabelColor = SpineBandDarkGray,
                    cursorColor = SpineBandCyan,
                    focusedTextColor = SpineBandBlack,
                    unfocusedTextColor = SpineBandBlack
                )
            )

            OutlinedTextField(
                value = age,
                onValueChange = {
                    if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 1..120)) {
                        age = it
                        errorMessage = null
                    }
                },
                label = { Text("Edad - Opcional") },
                leadingIcon = {
                    Icon(Icons.Default.Cake, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpineBandCyan,
                    unfocusedBorderColor = SpineBandGray,
                    focusedLabelColor = SpineBandCyan,
                    unfocusedLabelColor = SpineBandDarkGray,
                    cursorColor = SpineBandCyan,
                    focusedTextColor = SpineBandBlack,
                    unfocusedTextColor = SpineBandBlack
                )
            )

            Text(
                text = "Género - Opcional",
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
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = selectedGender == "M",
                    onClick = { selectedGender = if (selectedGender == "M") null else "M" },
                    label = { Text("Masculino") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpineBandCyan,
                        selectedLabelColor = SpineBandWhite,
                        containerColor = SpineBandWhite,
                        labelColor = SpineBandBlack
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedGender == "M",
                        borderColor = SpineBandGray,
                        selectedBorderColor = SpineBandCyan,
                        borderWidth = 1.dp
                    )
                )

                FilterChip(
                    selected = selectedGender == "F",
                    onClick = { selectedGender = if (selectedGender == "F") null else "F" },
                    label = { Text("Femenino") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpineBandCyan,
                        selectedLabelColor = SpineBandWhite,
                        containerColor = SpineBandWhite,
                        labelColor = SpineBandBlack
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedGender == "F",
                        borderColor = SpineBandGray,
                        selectedBorderColor = SpineBandCyan,
                        borderWidth = 1.dp
                    )
                )

                FilterChip(
                    selected = selectedGender == "Otro",
                    onClick = { selectedGender = if (selectedGender == "Otro") null else "Otro" },
                    label = { Text("Otro") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpineBandCyan,
                        selectedLabelColor = SpineBandWhite,
                        containerColor = SpineBandWhite,
                        labelColor = SpineBandBlack
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedGender == "Otro",
                        borderColor = SpineBandGray,
                        selectedBorderColor = SpineBandCyan,
                        borderWidth = 1.dp
                    )
                )
            }

            Text(
                text = "Esta información es opcional y nos ayuda a personalizar tu experiencia",
                fontSize = 12.sp,
                color = SpineBandDarkGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
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

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
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
                        viewModel.registerData.weight = weight.toFloatOrNull()
                        viewModel.registerData.height = height.toFloatOrNull()
                        viewModel.registerData.age = age.toIntOrNull()
                        viewModel.registerData.gender = selectedGender

                        onNavigateToSurvey()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpineBandCyan,
                        contentColor = SpineBandWhite
                    )
                ) {
                    Text("Continuar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
