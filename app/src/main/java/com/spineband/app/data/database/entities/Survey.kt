package com.spineband.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "surveys",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Survey(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int,

    // Respuestas de la encuesta
    val hoursSeated: String,        // "0-2", "3-5", "6-8", "8+"
    val worksWithPC: Boolean,
    val hasBackPain: String,        // "Nunca", "A veces", "Frecuente", "Siempre"
    val doesExercise: String,       // "Nunca", "1-2/semana", "3-4/semana", "Diario"
    val motivation: String,         // Texto libre

    val completedAt: Long = System.currentTimeMillis()
)
