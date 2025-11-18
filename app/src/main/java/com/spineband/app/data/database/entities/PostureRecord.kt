package com.spineband.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "posture_records",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["timestamp"])]
)
data class PostureRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,

    // Datos del sensor
    val angle: Float,              // Ángulo de inclinación
    val pitch: Float? = null,      // Para MPU6050 futuro
    val roll: Float? = null,       // Para MPU6050 futuro
    val yaw: Float? = null,        // Para MPU6050 futuro

    // Estado de la postura
    val status: String,            // "Excelente", "Buena", "Regular", "Mala"
    val isGoodPosture: Boolean,    // true/false para estadísticas rápidas

    // Timestamps
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String,         // ID de la sesión actual (para agrupar)

    // Metadata
    val calibrationOffset: Float = 0f,
    val notes: String? = null
)

// Helper para generar un sessionId único por día
fun generateSessionId(): String {
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date())
}

// Helper para determinar el estado según el ángulo
fun determinePostureStatus(angle: Float): String {
    return when {
        angle <= 15 -> "Excelente"
        angle <= 25 -> "Buena"
        angle <= 35 -> "Regular"
        else -> "Mala"
    }
}

// Helper para determinar si es buena postura
fun isGoodPosture(angle: Float): Boolean {
    return angle <= 25
}