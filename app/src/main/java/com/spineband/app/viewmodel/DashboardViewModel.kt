package com.spineband.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spineband.app.data.SpineBandApi
import com.spineband.app.data.database.dao.PostureRecordDao
import com.spineband.app.data.database.entities.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class DashboardViewModel(
    private val postureRecordDao: PostureRecordDao,
    private val userId: Int,
    private val esp32IP: String
) : ViewModel() {

    // Instancia del API
    private val api = SpineBandApi("http://$esp32IP")

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

    // ========== CONEXIÓN REAL CON ESP32 ==========

    private fun startDataCollection() {
        viewModelScope.launch {
            while (true) {
                try {
                    // Obtener datos del ESP32
                    val data = api.getPostureData()

                    if (data != null) {
                        _currentAngle.value = data.angle
                        _currentStatus.value = data.postureStatus
                        val isGood = data.isGoodPosture

                        // Guardar en base de datos
                        val record = PostureRecord(
                            userId = userId,
                            angle = data.angle,
                            status = data.postureStatus,
                            isGoodPosture = isGood,
                            sessionId = sessionId,
                            timestamp = System.currentTimeMillis()
                        )
                        postureRecordDao.insert(record)

                        // Actualizar contador de mala postura
                        if (!isGood) {
                            consecutiveBadPostureCount++
                            if (consecutiveBadPostureCount >= 5) {
                                _badPostureAlert.value = true
                            }
                        } else {
                            consecutiveBadPostureCount = 0
                            _badPostureAlert.value = false
                        }

                        _isConnected.value = true
                    } else {
                        _isConnected.value = false
                        _currentStatus.value = "Error de conexión"
                    }

                } catch (e: Exception) {
                    _isConnected.value = false
                    _currentStatus.value = "Error de conexión"
                }

                delay(1000) // Actualizar cada 1 segundo (más rápido que antes)
            }
        }
    }

    // Cargar datos para el gráfico
    private fun loadChartData() {
        viewModelScope.launch {
            postureRecordDao.getRecentRecords(userId, 30).collect { records ->
                _chartData.value = records.reversed()
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
                        totalTimeMinutes = it.total * 2 / 60
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
                val success = api.calibrate()
                _currentStatus.value = if (success) {
                    "✅ Calibración exitosa"
                } else {
                    "❌ Error en calibración"
                }
            } catch (e: Exception) {
                _currentStatus.value = "❌ Error en calibración"
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

    // === HELPERS ===

    private fun generateSessionId(): String {
        return "SESSION_${System.currentTimeMillis()}"
    }

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
