package com.spineband.app.data.database.dao

import androidx.room.*
import com.spineband.app.data.database.entities.DailySummary
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {

    // Insertar o actualizar resumen
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: DailySummary): Long

    // Obtener resumen de un día
    @Query("SELECT * FROM daily_summaries WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getSummaryByDate(userId: Int, date: String): DailySummary?

    // Obtener resumen de un día como Flow
    @Query("SELECT * FROM daily_summaries WHERE userId = :userId AND date = :date LIMIT 1")
    fun getSummaryByDateFlow(userId: Int, date: String): Flow<DailySummary?>

    // Obtener resúmenes de última semana
    @Query("SELECT * FROM daily_summaries WHERE userId = :userId ORDER BY date DESC LIMIT 7")
    fun getWeeklySummaries(userId: Int): Flow<List<DailySummary>>

    // Obtener resúmenes de un rango de fechas
    @Query("SELECT * FROM daily_summaries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getSummariesByDateRange(userId: Int, startDate: String, endDate: String): Flow<List<DailySummary>>

    // Actualizar resumen
    @Update
    suspend fun updateSummary(summary: DailySummary)

    // Incrementar contador de alertas
    @Query("UPDATE daily_summaries SET totalAlerts = totalAlerts + 1 WHERE userId = :userId AND date = :date")
    suspend fun incrementAlerts(userId: Int, date: String)

    // Obtener total de minutos de buena postura (últimos 7 días)
    @Query("SELECT SUM(goodPostureMinutes) FROM daily_summaries WHERE userId = :userId ORDER BY date DESC LIMIT 7")
    suspend fun getTotalGoodPostureWeek(userId: Int): Int?

    // Obtener promedio semanal
    @Query("SELECT AVG(averageAngle) FROM daily_summaries WHERE userId = :userId ORDER BY date DESC LIMIT 7")
    suspend fun getWeeklyAverageAngle(userId: Int): Float?
}