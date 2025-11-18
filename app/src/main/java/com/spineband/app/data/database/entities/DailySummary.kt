package com.spineband.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_summaries",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]   // ← Solución al warning
)
data class DailySummary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,
    val date: String,  // "2025-11-17"

    // Estadísticas del día
    val goodPostureMinutes: Int = 0,
    val badPostureMinutes: Int = 0,
    val totalAlerts: Int = 0,
    val averageAngle: Float = 0f,

    // Metadata
    val lastUpdated: Long = System.currentTimeMillis()
)
