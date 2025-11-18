package com.spineband.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "posture_data",
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
data class PostureData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,

    // Ángulos del sensor
    val angleX: Float,
    val angleY: Float,
    val angleZ: Float,

    // Estado
    val isGoodPosture: Boolean,

    // Timestamp
    val timestamp: Long = System.currentTimeMillis(),
    val date: String,  // "2025-11-17"
    val time: String   // "11:30:45"
)
