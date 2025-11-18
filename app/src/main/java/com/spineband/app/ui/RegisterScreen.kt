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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spineband.app.ui.theme.*
import com.spineband.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToStep2: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

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
                text = "Crear Cuenta",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy
            )

            Text(
                text = "Paso 1 de 3: Datos básicos",
                fontSize = 14.sp,
                color = SpineBandCyan,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            LinearProgressIndicator(
                progress = { 0.33f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                color = SpineBandCyan,
                trackColor = SpineBandLightGray
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                label = { Text("Nombre completo") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
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
                value = email,
                onValueChange = {
                    email = it.lowercase()
                    errorMessage = null
                },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Confirmar contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
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

            Button(
                onClick = {
                    scope.launch {
                        val error = viewModel.validateStep1(name, email, password, confirmPassword)
                        if (error != null) {
                            errorMessage = error
                            return@launch
                        }

                        isChecking = true
                        val emailAvailable = viewModel.isEmailAvailable(email)
                        isChecking = false

                        if (!emailAvailable) {
                            errorMessage = "Este email ya está registrado"
                            return@launch
                        }

                        viewModel.registerData.name = name
                        viewModel.registerData.email = email
                        viewModel.registerData.password = password

                        onNavigateToStep2()
                    }
                },
                enabled = !isChecking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpineBandCyan,
                    contentColor = SpineBandWhite
                )
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        color = SpineBandWhite,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Continuar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes cuenta?",
                    color = SpineBandDarkGray,
                    fontSize = 14.sp
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Inicia sesión",
                        color = SpineBandCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
