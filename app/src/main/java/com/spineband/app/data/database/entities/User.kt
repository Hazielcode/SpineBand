package com.spineband.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val email: String,
    val passwordHash: String,  // Contraseña encriptada

    // Datos físicos
    val weight: Float? = null,      // kg
    val height: Float? = null,      // cm
    val age: Int? = null,
    val gender: String? = null,     // "M", "F", "Otro"

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
