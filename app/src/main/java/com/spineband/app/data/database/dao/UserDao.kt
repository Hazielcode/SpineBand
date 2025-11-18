package com.spineband.app.data.database.dao

import androidx.room.*
import com.spineband.app.data.database.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // Insertar usuario
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    // Obtener usuario por email
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    // Obtener usuario por ID
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    // Obtener usuario activo (el que está logueado)
    @Query("SELECT * FROM users WHERE isActive = 1 LIMIT 1")
    fun getActiveUser(): Flow<User?>

    // Actualizar usuario
    @Update
    suspend fun updateUser(user: User)

    // Desactivar todos los usuarios (logout)
    @Query("UPDATE users SET isActive = 0")
    suspend fun deactivateAllUsers()

    // Activar usuario (login)
    @Query("UPDATE users SET isActive = 1 WHERE id = :userId")
    suspend fun activateUser(userId: Int)

    // Verificar si existe email
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun emailExists(email: String): Int

    // Eliminar usuario
    @Delete
    suspend fun deleteUser(user: User)
}