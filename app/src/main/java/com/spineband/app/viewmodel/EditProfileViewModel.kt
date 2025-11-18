package com.spineband.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spineband.app.data.database.dao.UserDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userDao: UserDao,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userDao.getUserById(userId)?.let { user ->
                    _uiState.value = EditProfileUiState(
                        name = user.name,
                        email = user.email,
                        age = user.age?.toString() ?: "",
                        weight = user.weight?.toString() ?: "",
                        height = user.height?.toString() ?: "",
                        gender = user.gender ?: ""
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateField(field: ProfileField, value: String) {
        _uiState.value = when (field) {
            ProfileField.NAME -> _uiState.value.copy(name = value)
            ProfileField.EMAIL -> _uiState.value.copy(email = value)
            ProfileField.AGE -> _uiState.value.copy(age = value)
            ProfileField.WEIGHT -> _uiState.value.copy(weight = value)
            ProfileField.HEIGHT -> _uiState.value.copy(height = value)
            ProfileField.GENDER -> _uiState.value.copy(gender = value)
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentState = _uiState.value

                // Validación
                if (currentState.name.isBlank() || currentState.email.isBlank()) {
                    return@launch
                }

                val user = userDao.getUserById(userId)?.copy(
                    name = currentState.name,
                    email = currentState.email,
                    age = currentState.age.toIntOrNull(),
                    weight = currentState.weight.toFloatOrNull(),
                    height = currentState.height.toFloatOrNull(),
                    gender = currentState.gender.ifBlank { null }
                )

                user?.let {
                    userDao.updateUser(it)
                    _saveSuccess.value = true
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}

data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val age: String = "",
    val weight: String = "",
    val height: String = "",
    val gender: String = ""
)

enum class ProfileField {
    NAME, EMAIL, AGE, WEIGHT, HEIGHT, GENDER
}

class EditProfileViewModelFactory(
    private val userDao: UserDao,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditProfileViewModel(userDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}