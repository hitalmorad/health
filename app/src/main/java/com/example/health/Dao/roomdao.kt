package com.example.health.Dao

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Dao
interface DoctorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(doctors: List<DoctorEntity>)

    @Query("SELECT * FROM doctors")
    suspend fun getAllDoctors(): List<DoctorEntity>

    @Query("DELETE FROM doctors")
    suspend fun clearAll()
}




@Database(entities = [DoctorEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun doctorDao(): DoctorDao
}




object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "health_app_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}

