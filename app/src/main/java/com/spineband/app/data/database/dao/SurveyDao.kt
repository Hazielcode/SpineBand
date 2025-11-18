package com.spineband.app.data.database.dao

import androidx.room.*
import com.spineband.app.data.database.entities.Survey
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveyDao {

    // Insertar encuesta
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: Survey): Long

    // Obtener encuesta de un usuario
    @Query("SELECT * FROM surveys WHERE userId = :userId LIMIT 1")
    suspend fun getSurveyByUserId(userId: Int): Survey?

    // Verificar si usuario completó encuesta
    @Query("SELECT COUNT(*) FROM surveys WHERE userId = :userId")
    suspend fun hasSurvey(userId: Int): Int

    // Obtener encuesta como Flow
    @Query("SELECT * FROM surveys WHERE userId = :userId LIMIT 1")
    fun getSurveyFlow(userId: Int): Flow<Survey?>

    // Actualizar encuesta
    @Update
    suspend fun updateSurvey(survey: Survey)
}