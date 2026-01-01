package com.example.client_training_app.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.client_training_app.R
import com.example.client_training_app.data.dao.ExerciseDao
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.model.toEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseInstance {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "training_database"
            )
                // Callback
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Spustíme coroutinu na pozadí pro vložení dat
                        CoroutineScope(Dispatchers.IO).launch {
                            fillWithDefaultExercises(context)
                        }
                    }
                })
                // Pokud se mění schéma, tohle smaže data místo crashe (jen pro vývoj!)
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }

    // Funkce, která fyzicky vloží JSON data do SQL tabulky
    private suspend fun fillWithDefaultExercises(context: Context) {
        val database = getDatabase(context)
        val exerciseDao = database.exerciseDao()

        try {
            // 1. Načíst JSON
            val inputStream = context.resources.openRawResource(R.raw.default_exercises)
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            // 2. Parsovat Gsonem
            val type = object : TypeToken<List<Exercise>>() {}.type
            val exercises: List<Exercise> = Gson().fromJson(jsonString, type)

            // 3. Převést na Entity a VLOŽIT DO DB
            val entities = exercises.map { it.toEntity() }
            exerciseDao.insertAll(entities) // Předpokládám, že máš v DAO metodu @Insert insertAll(List<...>)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}