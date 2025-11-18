package com.spineband.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spineband.app.ui.theme.*
import com.spineband.app.viewmodel.EditProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Mostrar mensaje de éxito
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("¡Perfil actualizado exitosamente!")
            }
            viewModel.resetSaveSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveProfile() },
                        enabled = !isLoading
                    ) {
                        Text("GUARDAR", color = SpineBandWhite)
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
                .background(SpineBandWhite)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = SpineBandCyan
                )
            } else {
                EditProfileContent(
                    uiState = uiState,
                    onFieldUpdate = viewModel::updateField
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    uiState: com.spineband.app.viewmodel.EditProfileUiState,
    onFieldUpdate: (com.spineband.app.viewmodel.ProfileField, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Name Field
        EditField(
            value = uiState.name,
            onValueChange = { onFieldUpdate(com.spineband.app.viewmodel.ProfileField.NAME, it) },
            label = "Nombre Completo",
            icon = Icons.Default.Person,
            turquoise = SpineBandCyan
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Field
        EditField(
            value = uiState.email,
            onValueChange = { onFieldUpdate(com.spineband.app.viewmodel.ProfileField.EMAIL, it) },
            label = "Email",
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            turquoise = SpineBandCyan
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Age Field
        EditField(
            value = uiState.age,
            onValueChange = { onFieldUpdate(com.spineband.app.viewmodel.ProfileField.AGE, it) },
            label = "Edad",
            icon = Icons.Default.DateRange,
            keyboardType = KeyboardType.Number,
            turquoise = SpineBandCyan
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gender Dropdown
        var expanded by remember { mutableStateOf(false) }
        val genderOptions = listOf(
            "M" to "Masculino",
            "F" to "Femenino",
            "Otro" to "Otro"
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = genderOptions.find { it.first == uiState.gender }?.second ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Género") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = SpineBandCyan)
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpineBandCyan,
                    focusedLabelColor = SpineBandCyan
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genderOptions.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onFieldUpdate(com.spineband.app.viewmodel.ProfileField.GENDER, value)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Physical Info Section
        Text(
            "Información Física",
            style = MaterialTheme.typography.titleMedium,
            color = SpineBandNavy
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Weight Field
            EditField(
                value = uiState.weight,
                onValueChange = { onFieldUpdate(com.spineband.app.viewmodel.ProfileField.WEIGHT, it) },
                label = "Peso (kg)",
                icon = Icons.Default.FitnessCenter,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
                turquoise = SpineBandCyan
            )

            // Height Field
            EditField(
                value = uiState.height,
                onValueChange = { onFieldUpdate(com.spineband.app.viewmodel.ProfileField.HEIGHT, it) },
                label = "Altura (cm)",
                icon = Icons.Default.Height,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
                turquoise = SpineBandCyan
            )
        }
    }
}

@Composable
fun EditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    turquoise: androidx.compose.ui.graphics.Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = turquoise)
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = turquoise,
            focusedLabelColor = turquoise
        ),
        singleLine = true
    )
}