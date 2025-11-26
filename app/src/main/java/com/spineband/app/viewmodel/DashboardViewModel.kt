package com.spineband.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spineband.app.data.SpineBandApi
import com.spineband.app.data.database.dao.PostureRecordDao
import com.spineband.app.data.database.entities.*
import com.spineband.app.utils.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class DashboardViewModel(
    private val postureRecordDao: PostureRecordDao,
    private val userId: Int,
    private val esp32IP: String,
    private val context: Context  // NUEVO: Añadir Context para notificaciones
) : ViewModel() {

    // Instancia del API
    private val api = SpineBandApi("http://$esp32IP")

    // NUEVO: Helper para notificaciones y vibración
    private val notificationHelper = NotificationHelper(context)

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

    // NUEVO: Control de alertas
    private val _alertsEnabled = MutableStateFlow(true)
    val alertsEnabled: StateFlow<Boolean> = _alertsEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private var sessionId: String = generateSessionId()
    private var sessionStartTime: Long = System.currentTimeMillis()
    private var consecutiveBadPostureCount = 0

    // NUEVO: Variables para control de tiempo de mala postura
    private var badPostureStartTime: Long = 0
    private var lastAlertTime: Long = 0

    // NUEVO: Configuraciones de alerta
    private val ALERT_THRESHOLD_SECONDS = 10  // Alertar después de 10 segundos de mala postura
    private val ALERT_COOLDOWN_MS = 30000     // No alertar más de una vez cada 30 segundos

    init {
        startDataCollection()
        loadChartData()
        loadTodayStats()
        startSessionTimer()
    }

    // ========== CONEXIÓN REAL CON ESP32 CON ALERTAS MEJORADAS ==========

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

                        // LÓGICA DE ALERTAS MEJORADA CON TIEMPO
                        if (!isGood) {
                            // Mala postura detectada
                            if (consecutiveBadPostureCount == 0) {
                                // Primera vez que se detecta mala postura
                                badPostureStartTime = System.currentTimeMillis()
                            }
                            consecutiveBadPostureCount++

                            // Calcular cuánto tiempo lleva en mala postura
                            val badPostureDuration = (System.currentTimeMillis() - badPostureStartTime) / 1000

                            // Verificar si han pasado 10 segundos de mala postura continua
                            if (badPostureDuration >= ALERT_THRESHOLD_SECONDS) {
                                val currentTime = System.currentTimeMillis()

                                // Verificar cooldown para no molestar demasiado
                                if (currentTime - lastAlertTime > ALERT_COOLDOWN_MS) {
                                    _badPostureAlert.value = true

                                    // ACTIVAR ALERTA CON VIBRACIÓN Y SONIDO
                                    if (_alertsEnabled.value) {
                                        triggerPostureAlert(data.angle, badPostureDuration.toInt())
                                    }

                                    lastAlertTime = currentTime
                                }
                            }
                        } else {
                            // Buena postura - resetear contadores
                            if (consecutiveBadPostureCount > 0) {
                                consecutiveBadPostureCount = 0
                                _badPostureAlert.value = false
                                badPostureStartTime = 0

                                // Cancelar notificación si existe
                                notificationHelper.cancelNotification()
                            }
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

                delay(1000) // Actualizar cada 1 segundo
            }
        }
    }

    // NUEVA FUNCIÓN: Disparar alerta con vibración y sonido
    private fun triggerPostureAlert(angle: Float, duration: Int) {
        viewModelScope.launch {
            // Mostrar notificación con vibración y sonido
            notificationHelper.showPostureAlert(angle, duration)

            // Vibración adicional si está habilitada
            if (_vibrationEnabled.value) {
                // Patrón de vibración de advertencia (3 vibraciones cortas)
                repeat(3) {
                    notificationHelper.vibrateQuick()
                    delay(300)
                }
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
                        totalTimeMinutes = it.total / 60  // Cada registro es 1 segundo
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

    // Calibrar sensor - MEJORADO CON VIBRACIÓN
    fun calibrate() {
        viewModelScope.launch {
            try {
                val success = api.calibrate()
                _currentStatus.value = if (success) {
                    // Vibración corta de confirmación
                    notificationHelper.vibrateQuick()
                    "✅ Calibración exitosa"
                } else {
                    "❌ Error en calibración"
                }
            } catch (e: Exception) {
                _currentStatus.value = "❌ Error en calibración"
            }
        }
    }

    // Reiniciar sesión - MEJORADO
    fun resetSession() {
        sessionId = generateSessionId()
        sessionStartTime = System.currentTimeMillis()
        _sessionDuration.value = 0
        consecutiveBadPostureCount = 0
        _badPostureAlert.value = false
        badPostureStartTime = 0
        lastAlertTime = 0
        // Cancelar cualquier notificación activa
        notificationHelper.cancelNotification()
    }

    // Descartar alerta de mala postura - MEJORADO
    fun dismissAlert() {
        _badPostureAlert.value = false
        // No reseteamos el contador aquí para mantener el tracking
        // Solo cancelamos la notificación visual
        notificationHelper.cancelNotification()
    }

    // NUEVAS FUNCIONES DE CONFIGURACIÓN
    fun toggleAlerts(enabled: Boolean) {
        _alertsEnabled.value = enabled
        if (!enabled) {
            // Si se desactivan las alertas, cancelar cualquier notificación activa
            notificationHelper.cancelNotification()
            _badPostureAlert.value = false
        }
    }

    fun toggleVibration(enabled: Boolean) {
        _vibrationEnabled.value = enabled
    }

    fun toggleSound(enabled: Boolean) {
        _soundEnabled.value = enabled
    }

    // NUEVA FUNCIÓN: Obtener estadísticas de alertas del día
    fun getTodayAlertCount(): Int {
        // Esta función puede ser expandida para contar alertas desde la BD
        return 0 // Por ahora retornamos 0, pero puedes implementar el conteo real
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

    // NUEVA FUNCIÓN: Limpiar recursos cuando el ViewModel se destruye
    override fun onCleared() {
        super.onCleared()
        // Cancelar cualquier notificación pendiente
        notificationHelper.cancelNotification()
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

// Factory para el ViewModel - ACTUALIZADO CON CONTEXT
class DashboardViewModelFactory(
    private val postureRecordDao: PostureRecordDao,
    private val userId: Int,
    private val esp32IP: String,
    private val context: Context  // NUEVO: Añadir Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(postureRecordDao, userId, esp32IP, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}