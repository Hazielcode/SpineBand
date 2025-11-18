package com.spineband.app.data.database.dao

import androidx.room.*
import com.spineband.app.data.database.entities.PostureRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PostureRecordDao {

    // Insertar nuevo registro
    @Insert
    suspend fun insert(record: PostureRecord)

    // Obtener últimos N registros para el gráfico
    @Query("""
        SELECT * FROM posture_records 
        WHERE userId = :userId 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun getRecentRecords(userId: Int, limit: Int = 30): Flow<List<PostureRecord>>

    // Obtener registros de hoy
    @Query("""
        SELECT * FROM posture_records 
        WHERE userId = :userId 
        AND timestamp >= :startOfDay 
        ORDER BY timestamp DESC
    """)
    fun getTodayRecords(userId: Int, startOfDay: Long): Flow<List<PostureRecord>>

    // Obtener registros de una sesión específica
    @Query("""
        SELECT * FROM posture_records 
        WHERE userId = :userId 
        AND sessionId = :sessionId 
        ORDER BY timestamp DESC
    """)
    fun getSessionRecords(userId: Int, sessionId: String): Flow<List<PostureRecord>>

    // Estadísticas del día
    @Query("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN isGoodPosture = 1 THEN 1 ELSE 0 END) as goodCount,
            SUM(CASE WHEN isGoodPosture = 0 THEN 1 ELSE 0 END) as badCount,
            AVG(angle) as avgAngle,
            MIN(angle) as minAngle,
            MAX(angle) as maxAngle
        FROM posture_records 
        WHERE userId = :userId 
        AND timestamp >= :startOfDay
    """)
    fun getTodayStats(userId: Int, startOfDay: Long): Flow<PostureStats?>

    // Eliminar registros antiguos (más de 30 días)
    @Query("""
        DELETE FROM posture_records 
        WHERE timestamp < :oldTimestamp
    """)
    suspend fun deleteOldRecords(oldTimestamp: Long)

    // Eliminar todos los registros de un usuario
    @Query("DELETE FROM posture_records WHERE userId = :userId")
    suspend fun deleteUserRecords(userId: Int)
}

// Data class para estadísticas
data class PostureStats(
    val total: Int,
    val goodCount: Int,
    val badCount: Int,
    val avgAngle: Float,
    val minAngle: Float,
    val maxAngle: Float
)