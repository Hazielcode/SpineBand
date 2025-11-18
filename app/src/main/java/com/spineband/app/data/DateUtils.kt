package com.spineband.app.data

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Obtiene la fecha actual en formato "yyyy-MM-dd"
     */
    fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }

    /**
     * Obtiene la hora actual en formato "HH:mm:ss"
     */
    fun getCurrentTime(): String {
        return timeFormat.format(Date())
    }

    /**
     * Obtiene timestamp actual
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Convierte timestamp a fecha
     */
    fun timestampToDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Convierte timestamp a hora
     */
    fun timestampToTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    /**
     * Obtiene fecha de hace N días
     */
    fun getDateDaysAgo(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return dateFormat.format(calendar.time)
    }

    /**
     * Obtiene el nombre del día de la semana
     */
    fun getDayOfWeek(date: String): String {
        return try {
            val parsedDate = dateFormat.parse(date)
            val dayFormat = SimpleDateFormat("EEEE", Locale("es", "ES"))
            dayFormat.format(parsedDate ?: Date())
        } catch (e: Exception) {
            "Desconocido"
        }
    }

    /**
     * Formatea fecha para mostrar (ej: "Lun 17 Nov")
     */
    fun formatDateDisplay(date: String): String {
        return try {
            val parsedDate = dateFormat.parse(date)
            val displayFormat = SimpleDateFormat("EEE dd MMM", Locale("es", "ES"))
            displayFormat.format(parsedDate ?: Date())
        } catch (e: Exception) {
            date
        }
    }
}
