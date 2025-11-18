package com.spineband.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spineband.app.data.AppSettings
import com.spineband.app.data.database.AppDatabase
import com.spineband.app.ui.*
import com.spineband.app.ui.theme.SpineBandTheme
import com.spineband.app.viewmodel.AuthViewModel
import com.spineband.app.viewmodel.EditProfileViewModel
import com.spineband.app.viewmodel.EditProfileViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpineBandTheme {
                SpineBandApp()
            }
        }
    }
}

@Composable
fun SpineBandApp() {
    val context = LocalContext.current
    val settings = remember { AppSettings(context) }
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    var currentIP by remember { mutableStateOf(settings.esp32IP) }

    // Observar usuario activo
    val currentUser by authViewModel.currentUser.collectAsState()

    // Determinar pantalla inicial
    val startDestination = if (currentUser != null) "dashboard" else "splash"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
               composable("splash") {
                SplashScreen(
                    onNavigateToNext = {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            composable("login") {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate("register")
                    },
                    onLoginSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
                )
            }

            composable("register") {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onNavigateToStep2 = {
                        navController.navigate("register_step2")
                    },
                    viewModel = authViewModel
                )
            }

            composable("register_step2") {
                RegisterStep2Screen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToSurvey = {
                        navController.navigate("survey")
                    },
                    viewModel = authViewModel
                )
            }

            composable("survey") {
                SurveyScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onSurveyComplete = {
                        navController.navigate("dashboard") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
                )
            }

            composable("dashboard") {
                DashboardScreen(
                    esp32IP = currentIP,
                    onNavigateToHistory = {
                        navController.navigate("history")
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onNavigateToProfile = {
                        navController.navigate("profile")
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    settings = settings,
                    onNavigateBack = {
                        currentIP = settings.esp32IP
                        navController.popBackStack()
                    }
                )
            }

            composable("history") {
                HistoryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("profile") {
                ProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEdit = {
                        navController.navigate("edit_profile")
                    },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
                )
            }

            // EditProfileScreen
            composable("edit_profile") {
                val database = remember { AppDatabase.getDatabase(context) }
                val currentUserId = currentUser?.id ?: return@composable

                val editViewModel: EditProfileViewModel = viewModel(
                    factory = EditProfileViewModelFactory(
                        userDao = database.userDao(),
                        userId = currentUserId
                    )
                )

                EditProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    viewModel = editViewModel
                )
            }
        }
    }
}