package com.spineband.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spineband.app.data.PasswordUtils
import com.spineband.app.data.database.AppDatabase
import com.spineband.app.data.database.entities.User
import com.spineband.app.data.database.entities.Survey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterData(
    // Paso 1
    var name: String = "",
    var email: String = "",
    var password: String = "",

    // Paso 2
    var weight: Float? = null,
    var height: Float? = null,
    var age: Int? = null,
    var gender: String? = null,

    // Paso 3
    var hoursSeated: String = "",
    var worksWithPC: Boolean = false,
    var hasBackPain: String = "",
    var doesExercise: String = "",
    var motivation: String = ""
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: Int) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()
    private val surveyDao = database.surveyDao()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val registerData = RegisterData()

    init {
        checkActiveUser()
    }

    private fun checkActiveUser() {
        viewModelScope.launch {
            userDao.getActiveUser().collect { user ->
                _currentUser.value = user
            }
        }
    }

    // LOGIN
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                // Validaciones
                if (email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Por favor completa todos los campos")
                    return@launch
                }

                if (!PasswordUtils.isValidEmail(email)) {
                    _authState.value = AuthState.Error("Email inválido")
                    return@launch
                }

                // Buscar usuario
                val user = userDao.getUserByEmail(email.trim().lowercase())

                if (user == null) {
                    _authState.value = AuthState.Error("Usuario no encontrado")
                    return@launch
                }

                // Verificar contraseña
                if (!PasswordUtils.verifyPassword(password, user.passwordHash)) {
                    _authState.value = AuthState.Error("Contraseña incorrecta")
                    return@launch
                }

                // Login exitoso
                userDao.deactivateAllUsers()
                userDao.activateUser(user.id)
                _authState.value = AuthState.Success(user.id)

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error al iniciar sesión: ${e.message}")
            }
        }
    }

    // REGISTRO COMPLETO
    fun completeRegistration() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                // Crear usuario
                val user = User(
                    name = registerData.name.trim(),
                    email = registerData.email.trim().lowercase(),
                    passwordHash = PasswordUtils.hashPassword(registerData.password),
                    weight = registerData.weight,
                    height = registerData.height,
                    age = registerData.age,
                    gender = registerData.gender,
                    isActive = true
                )

                // Desactivar otros usuarios
                userDao.deactivateAllUsers()

                // Insertar usuario
                val userId = userDao.insertUser(user).toInt()

                // Crear encuesta
                val survey = Survey(
                    userId = userId,
                    hoursSeated = registerData.hoursSeated,
                    worksWithPC = registerData.worksWithPC,
                    hasBackPain = registerData.hasBackPain,
                    doesExercise = registerData.doesExercise,
                    motivation = registerData.motivation
                )

                surveyDao.insertSurvey(survey)

                _authState.value = AuthState.Success(userId)

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error al registrar: ${e.message}")
            }
        }
    }

    // VALIDAR PASO 1
    fun validateStep1(name: String, email: String, password: String, confirmPassword: String): String? {
        if (name.isBlank()) return "Por favor ingresa tu nombre"
        if (email.isBlank()) return "Por favor ingresa tu email"
        if (!PasswordUtils.isValidEmail(email)) return "Email inválido"
        if (password.isBlank()) return "Por favor ingresa una contraseña"
        if (!PasswordUtils.isValidPassword(password)) return "La contraseña debe tener al menos 6 caracteres"
        if (password != confirmPassword) return "Las contraseñas no coinciden"

        return null // Sin errores
    }

    // VERIFICAR EMAIL DISPONIBLE
    suspend fun isEmailAvailable(email: String): Boolean {
        return userDao.emailExists(email.trim().lowercase()) == 0
    }

    // LOGOUT
    fun logout() {
        viewModelScope.launch {
            userDao.deactivateAllUsers()
            _currentUser.value = null
            _authState.value = AuthState.Idle
        }
    }

    // RESET STATE
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
