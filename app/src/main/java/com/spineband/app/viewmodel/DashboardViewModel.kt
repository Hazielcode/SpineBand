package com.spineband.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spineband.app.data.database.dao.PostureRecordDao
import com.spineband.app.data.database.entities.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(
    private val postureRecordDao: PostureRecordDao,
    private val userId: Int,
    private val esp32IP: String
) : ViewModel() {

    // Estado de conexión
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Datos actuales del ESP32
    private val _currentAngle = MutableStateFlow(0f)
    val currentAngle: StateFlow<Float> = _currentAngle.asStateFlow()

    private val _currentStatus = MutableStateFlow("Esperando datos...")
    val currentStatus: StateFlow<String> = _currentStatus.asStateFlow()

    // Datos para el gráfico (últimos 30 registros)
    private val _chartData = MutableStateFlow<List<PostureRecord>>(emptyList())
    val chartData: StateFlow<List<PostureRecord>> = _chartData.asStateFlow()

    // Historial de sesión (últimos 15 registros)
    private val _sessionHistory = MutableStateFlow<List<PostureRecord>>(emptyList())
    val sessionHistory: StateFlow<List<PostureRecord>> = _sessionHistory.asStateFlow()

    // Estadísticas del día
    private val _todayStats = MutableStateFlow<DashboardStats?>(null)
    val todayStats: StateFlow<DashboardStats?> = _todayStats.asStateFlow()

    // Tiempo en sesión actual
    private val _sessionDuration = MutableStateFlow(0L) // en segundos
    val sessionDuration: StateFlow<Long> = _sessionDuration.asStateFlow()

    // Alerta de mala postura
    private val _badPostureAlert = MutableStateFlow(false)
    val badPostureAlert: StateFlow<Boolean> = _badPostureAlert.asStateFlow()

    private var sessionId: String = generateSessionId()
    private var sessionStartTime: Long = System.currentTimeMillis()
    private var consecutiveBadPostureCount = 0

    init {
        startDataCollection()
        loadChartData()
        loadTodayStats()
        startSessionTimer()
    }

    // Iniciar recolección de datos del ESP32
    private fun startDataCollection() {
        viewModelScope.launch {
            while (true) {
                try {
                    // Simular lectura del ESP32 (reemplazar con llamada HTTP real)
                    val angle = fetchAngleFromESP32()

                    _currentAngle.value = angle
                    val status = determinePostureStatus(angle)
                    _currentStatus.value = status
                    val isGood = isGoodPosture(angle)

                    // Guardar en base de datos
                    val record = PostureRecord(
                        userId = userId,
                        angle = angle,
                        status = status,
                        isGoodPosture = isGood,
                        sessionId = sessionId,
                        timestamp = System.currentTimeMillis()
                    )
                    postureRecordDao.insert(record)

                    // Actualizar contador de mala postura
                    if (!isGood) {
                        consecutiveBadPostureCount++
                        if (consecutiveBadPostureCount >= 5) { // 5 lecturas malas = ~10 segundos
                            _badPostureAlert.value = true
                        }
                    } else {
                        consecutiveBadPostureCount = 0
                        _badPostureAlert.value = false
                    }

                    _isConnected.value = true

                } catch (e: Exception) {
                    _isConnected.value = false
                    _currentStatus.value = "Error de conexión"
                }

                delay(2000) // Actualizar cada 2 segundos
            }
        }
    }

    // Cargar datos para el gráfico
    private fun loadChartData() {
        viewModelScope.launch {
            postureRecordDao.getRecentRecords(userId, 30).collect { records ->
                _chartData.value = records.reversed() // Más antiguo primero para el gráfico
            }
        }
    }

    // Cargar historial de sesión
    fun loadSessionHistory() {
        viewModelScope.launch {
            postureRecordDao.getSessionRecords(userId, sessionId).collect { records ->
                _sessionHistory.value = records.take(15)
            }
        }
    }

    // Cargar estadísticas del día
    private fun loadTodayStats() {
        viewModelScope.launch {
            val startOfDay = getStartOfDayTimestamp()
            postureRecordDao.getTodayStats(userId, startOfDay).collect { stats ->
                stats?.let {
                    _todayStats.value = DashboardStats(
                        totalRecords = it.total,
                        goodPostureCount = it.goodCount,
                        badPostureCount = it.badCount,
                        averageAngle = it.avgAngle,
                        bestAngle = it.minAngle,
                        worstAngle = it.maxAngle,
                        goodPosturePercentage = if (it.total > 0)
                            (it.goodCount.toFloat() / it.total * 100) else 0f,
                        totalTimeMinutes = it.total * 2 / 60 // Cada lectura = 2 segundos
                    )
                }
            }
        }
    }

    // Timer de duración de sesión
    private fun startSessionTimer() {
        viewModelScope.launch {
            while (true) {
                val duration = (System.currentTimeMillis() - sessionStartTime) / 1000
                _sessionDuration.value = duration
                delay(1000)
            }
        }
    }

    // Calibrar sensor
    fun calibrate() {
        viewModelScope.launch {
            try {
                // Llamada HTTP al ESP32 para calibrar
                val response = calibrateESP32()
                if (response) {
                    _currentStatus.value = "Calibración exitosa"
                }
            } catch (e: Exception) {
                _currentStatus.value = "Error en calibración"
            }
        }
    }

    // Reiniciar sesión
    fun resetSession() {
        sessionId = generateSessionId()
        sessionStartTime = System.currentTimeMillis()
        _sessionDuration.value = 0
        consecutiveBadPostureCount = 0
        _badPostureAlert.value = false
    }

    // Descartar alerta de mala postura
    fun dismissAlert() {
        _badPostureAlert.value = false
        consecutiveBadPostureCount = 0
    }

    // === FUNCIONES HTTP (reemplazar con tu implementación real) ===

    private suspend fun fetchAngleFromESP32(): Float {
        // TODO: Implementar llamada HTTP real al ESP32
        // Por ahora simulamos datos
        return (10..40).random().toFloat()
    }

    private suspend fun calibrateESP32(): Boolean {
        // TODO: Implementar llamada HTTP POST al ESP32 /api/calibrate
        return true
    }

    // === HELPERS ===

    private fun getStartOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

// Data class para estadísticas del dashboard
data class DashboardStats(
    val totalRecords: Int,
    val goodPostureCount: Int,
    val badPostureCount: Int,
    val averageAngle: Float,
    val bestAngle: Float,
    val worstAngle: Float,
    val goodPosturePercentage: Float,
    val totalTimeMinutes: Int
)

// Factory para el ViewModel
class DashboardViewModelFactory(
    private val postureRecordDao: PostureRecordDao,
    private val userId: Int,
    private val esp32IP: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(postureRecordDao, userId, esp32IP) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}