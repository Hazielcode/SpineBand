package com.spineband.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spineband.app.data.database.dao.PostureRecordDao
import com.spineband.app.data.database.dao.PostureStats
import com.spineband.app.data.database.entities.PostureRecord
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// Data class para estadísticas de un día
data class DayStats(
    val date: Date,
    val dayName: String,      // "Lun", "Mar", etc.
    val dateString: String,    // "25/11"
    val goodPostureMinutes: Int,
    val badPostureMinutes: Int,
    val totalMinutes: Int,
    val goodPosturePercentage: Int,
    val alertCount: Int,      // Número de transiciones a mala postura
    val bestAngle: Float,
    val worstAngle: Float,
    val averageAngle: Float,
    val records: List<PostureRecord> = emptyList()
)

// Data class para resumen semanal
data class WeekSummary(
    val startDate: Date,
    val endDate: Date,
    val totalGoodMinutes: Int,
    val totalBadMinutes: Int,
    val totalMinutes: Int,
    val goodPosturePercentage: Int,
    val totalAlerts: Int,
    val bestDay: DayStats?,
    val worstDay: DayStats?,
    val dailyStats: List<DayStats>,
    val trend: String // "improving", "stable", "declining"
)

// Data class para estadísticas de todo el tiempo
data class AllTimeStats(
    val totalDays: Int,
    val totalHours: Int,
    val totalRecords: Int,
    val overallGoodPosturePercentage: Int,
    val bestDayEver: DayStats?,
    val worstDayEver: DayStats?,
    val firstUseDate: Date?,
    val averageSessionDuration: Int, // minutos
    val totalAlerts: Int
)

class HistoryViewModel(
    private val postureRecordDao: PostureRecordDao,
    private val userId: Int
) : ViewModel() {

    // Estados observables
    private val _weekSummary = MutableStateFlow<WeekSummary?>(null)
    val weekSummary: StateFlow<WeekSummary?> = _weekSummary.asStateFlow()

    private val _monthlyStats = MutableStateFlow<List<DayStats>>(emptyList())
    val monthlyStats: StateFlow<List<DayStats>> = _monthlyStats.asStateFlow()

    private val _allTimeStats = MutableStateFlow<AllTimeStats?>(null)
    val allTimeStats: StateFlow<AllTimeStats?> = _allTimeStats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    private val _selectedDayStats = MutableStateFlow<DayStats?>(null)
    val selectedDayStats: StateFlow<DayStats?> = _selectedDayStats.asStateFlow()

    init {
        loadWeekSummary()
        loadMonthlyStats()
        loadAllTimeStats()
    }

    // ========== CARGAR RESUMEN SEMANAL (Últimos 7 días) ==========
    private fun loadWeekSummary() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val endDate = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, -6) // 7 días atrás
            val startDate = calendar.time

            val weekStats = mutableListOf<DayStats>()
            var totalGoodMinutes = 0
            var totalBadMinutes = 0
            var totalAlerts = 0

            // Iterar por cada día de la semana
            for (i in 0..6) {
                calendar.time = startDate
                calendar.add(Calendar.DAY_OF_YEAR, i)

                val dayStats = loadDayStats(calendar.time)
                weekStats.add(dayStats)

                totalGoodMinutes += dayStats.goodPostureMinutes
                totalBadMinutes += dayStats.badPostureMinutes
                totalAlerts += dayStats.alertCount
            }

            val totalMinutes = totalGoodMinutes + totalBadMinutes
            val goodPercentage = if (totalMinutes > 0) {
                ((totalGoodMinutes.toFloat() / totalMinutes) * 100).roundToInt()
            } else 0

            // Encontrar mejor y peor día
            val bestDay = weekStats.maxByOrNull { it.goodPosturePercentage }
            val worstDay = weekStats.minByOrNull { it.goodPosturePercentage }

            // Determinar tendencia
            val firstHalf = weekStats.take(3).map { it.goodPosturePercentage }.average()
            val secondHalf = weekStats.takeLast(3).map { it.goodPosturePercentage }.average()
            val trend = when {
                secondHalf > firstHalf + 5 -> "improving"
                secondHalf < firstHalf - 5 -> "declining"
                else -> "stable"
            }

            _weekSummary.value = WeekSummary(
                startDate = startDate,
                endDate = endDate,
                totalGoodMinutes = totalGoodMinutes,
                totalBadMinutes = totalBadMinutes,
                totalMinutes = totalMinutes,
                goodPosturePercentage = goodPercentage,
                totalAlerts = totalAlerts,
                bestDay = bestDay,
                worstDay = worstDay,
                dailyStats = weekStats,
                trend = trend
            )

            _isLoading.value = false
        }
    }

    // ========== CARGAR ESTADÍSTICAS DE UN DÍA ESPECÍFICO ==========
    private suspend fun loadDayStats(date: Date): DayStats {
        val calendar = Calendar.getInstance()
        calendar.time = date

        // Inicio del día
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // Fin del día
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis

        // Obtener registros del día
        val records = postureRecordDao.getRecordsByDateRange(userId, startOfDay, endOfDay)

        // Calcular estadísticas
        val goodRecords = records.filter { it.isGoodPosture }
        val badRecords = records.filter { !it.isGoodPosture }

        // Asumiendo que cada registro representa ~1 segundo de monitoreo (se guarda cada 1s)
        val goodMinutes = (goodRecords.size) / 60
        val badMinutes = (badRecords.size) / 60
        val totalMinutes = goodMinutes + badMinutes

        val goodPercentage = if (totalMinutes > 0) {
            ((goodMinutes.toFloat() / totalMinutes) * 100).roundToInt()
        } else 0

        // Contar alertas (transiciones de buena a mala postura)
        var alertCount = 0
        for (i in 1 until records.size) {
            if (records[i-1].isGoodPosture && !records[i].isGoodPosture) {
                alertCount++
            }
        }

        // Ángulos
        val angles = records.map { it.angle }
        val bestAngle = angles.minOrNull() ?: 0f
        val worstAngle = angles.maxOrNull() ?: 0f
        val averageAngle = if (angles.isNotEmpty()) angles.average().toFloat() else 0f

        // Formato de fecha
        val dayFormat = SimpleDateFormat("EEE", Locale("es", "ES"))
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

        return DayStats(
            date = date,
            dayName = dayFormat.format(date).replaceFirstChar { it.uppercase() },
            dateString = dateFormat.format(date),
            goodPostureMinutes = goodMinutes,
            badPostureMinutes = badMinutes,
            totalMinutes = totalMinutes,
            goodPosturePercentage = goodPercentage,
            alertCount = alertCount,
            bestAngle = bestAngle,
            worstAngle = worstAngle,
            averageAngle = averageAngle,
            records = records
        )
    }

    // ========== CARGAR ESTADÍSTICAS DEL MES ==========
    private fun loadMonthlyStats() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.time = _selectedDate.value

            // Primer día del mes
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val firstDay = calendar.time

            // Último día del mes
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val lastDay = calendar.time

            val monthStats = mutableListOf<DayStats>()

            // Cargar estadísticas de cada día del mes
            calendar.time = firstDay
            while (!calendar.time.after(lastDay) && !calendar.time.after(Date())) {
                val dayStats = loadDayStats(calendar.time)
                if (dayStats.totalMinutes > 0) { // Solo días con datos
                    monthStats.add(dayStats)
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            _monthlyStats.value = monthStats
        }
    }

    // ========== CARGAR ESTADÍSTICAS DE TODO EL TIEMPO ==========
    private fun loadAllTimeStats() {
        viewModelScope.launch {
            val allRecords = postureRecordDao.getAllUserRecords(userId)

            if (allRecords.isEmpty()) {
                _allTimeStats.value = null
                return@launch
            }

            // Agrupar por días
            val recordsByDay = allRecords.groupBy { record ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = record.timestamp
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.time
            }

            val allDayStats = recordsByDay.map { (date, _) ->
                loadDayStats(date)
            }

            val totalDays = allDayStats.size
            val totalMinutes = allDayStats.sumOf { it.totalMinutes }
            val totalHours = totalMinutes / 60
            val totalGoodMinutes = allDayStats.sumOf { it.goodPostureMinutes }
            val overallGoodPercentage = if (totalMinutes > 0) {
                ((totalGoodMinutes.toFloat() / totalMinutes) * 100).roundToInt()
            } else 0

            val bestDay = allDayStats.maxByOrNull { it.goodPosturePercentage }
            val worstDay = allDayStats.minByOrNull { it.goodPosturePercentage }
            val totalAlerts = allDayStats.sumOf { it.alertCount }
            val avgSessionDuration = if (totalDays > 0) totalMinutes / totalDays else 0

            val firstRecord = allRecords.minByOrNull { it.timestamp }
            val firstUseDate = firstRecord?.let { Date(it.timestamp) }

            _allTimeStats.value = AllTimeStats(
                totalDays = totalDays,
                totalHours = totalHours,
                totalRecords = allRecords.size,
                overallGoodPosturePercentage = overallGoodPercentage,
                bestDayEver = bestDay,
                worstDayEver = worstDay,
                firstUseDate = firstUseDate,
                averageSessionDuration = avgSessionDuration,
                totalAlerts = totalAlerts
            )
        }
    }

    // ========== FUNCIONES PÚBLICAS ==========

    fun selectDate(date: Date) {
        _selectedDate.value = date
        loadMonthlyStats()
        viewModelScope.launch {
            _selectedDayStats.value = loadDayStats(date)
        }
    }

    fun refreshData() {
        _isLoading.value = true
        loadWeekSummary()
        loadMonthlyStats()
        loadAllTimeStats()
    }

    fun exportDataToCSV(): String {
        // TODO: Implementar exportación CSV
        return ""
    }
}

// Factory para el ViewModel
class HistoryViewModelFactory(
    private val postureRecordDao: PostureRecordDao,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(postureRecordDao, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}