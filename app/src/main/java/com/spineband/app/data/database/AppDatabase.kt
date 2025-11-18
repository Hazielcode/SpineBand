package com.spineband.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.spineband.app.data.database.dao.*
import com.spineband.app.data.database.entities.*

@Database(
    entities = [
        User::class,
        Survey::class,
        PostureData::class,
        DailySummary::class,
        PostureRecord::class  // NUEVA ENTIDAD
    ],
    version = 2,  // INCREMENTADA DE 1 A 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs
    abstract fun userDao(): UserDao
    abstract fun surveyDao(): SurveyDao
    abstract fun postureDao(): PostureDao
    abstract fun summaryDao(): SummaryDao
    abstract fun postureRecordDao(): PostureRecordDao  // NUEVO DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spineband_database"
                )
                    .fallbackToDestructiveMigration() // Para desarrollo
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}