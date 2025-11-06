package com.example.client_training_app.data.database

import android.content.Context
import androidx.room.Room

object DatabaseInstance {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "trainer_app_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}