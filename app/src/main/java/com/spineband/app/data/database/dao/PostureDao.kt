package com.spineband.app.data.database.dao

import androidx.room.*
import com.spineband.app.data.database.entities.PostureData
import kotlinx.coroutines.flow.Flow

@Dao
interface PostureDao {

    // Insertar dato de postura
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostureData(postureData: PostureData): Long

    // Obtener datos de un día específico
    @Query("SELECT * FROM posture_data WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun getPostureDataByDate(userId: Int, date: String): Flow<List<PostureData>>

    // Obtener últimos N registros
    @Query("SELECT * FROM posture_data WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentPostureData(userId: Int, limit: Int = 100): Flow<List<PostureData>>

    // Contar registros de buena postura en un día
    @Query("SELECT COUNT(*) FROM posture_data WHERE userId = :userId AND date = :date AND isGoodPosture = 1")
    suspend fun countGoodPostureByDate(userId: Int, date: String): Int

    // Contar registros de mala postura en un día
    @Query("SELECT COUNT(*) FROM posture_data WHERE userId = :userId AND date = :date AND isGoodPosture = 0")
    suspend fun countBadPostureByDate(userId: Int, date: String): Int

    // Obtener promedio de ángulos por día
    @Query("SELECT AVG(ABS(angleX)) FROM posture_data WHERE userId = :userId AND date = :date")
    suspend fun getAverageAngleByDate(userId: Int, date: String): Float?

    // Obtener datos de un rango de fechas
    @Query("SELECT * FROM posture_data WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getPostureDataByDateRange(userId: Int, startDate: String, endDate: String): Flow<List<PostureData>>

    // Eliminar datos antiguos (más de 30 días)
    @Query("DELETE FROM posture_data WHERE timestamp < :cutoffTime")
    suspend fun deleteOldData(cutoffTime: Long)

    // Obtener datos por hora (para gráficos)
    @Query("SELECT * FROM posture_data WHERE userId = :userId AND date = :date AND time LIKE :hour || '%' ORDER BY timestamp")
    suspend fun getPostureDataByHour(userId: Int, date: String, hour: String): List<PostureData>
}