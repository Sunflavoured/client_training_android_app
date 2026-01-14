package com.example.client_training_app.data.database

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.client_training_app.R
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
                // .fallbackToDestructiveMigration() // Smaže DB při změně verze
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    // 1. Volá se při prvním vytvoření souboru DB
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d("DatabaseInstance", "onCreate: Plním databázi defaultními daty")
                        CoroutineScope(Dispatchers.IO).launch {
                            fillWithDefaultExercises(context)
                        }
                    }

                    // 2. Volá se, když proběhne destruktivní migrace (zvýšení verze + smazání dat)
                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        Log.d("DatabaseInstance", "onDestructiveMigration: Plním databázi po migraci")
                        CoroutineScope(Dispatchers.IO).launch {
                            fillWithDefaultExercises(context)
                        }
                    }

                    // 3. (Volitelné) Pojistka: Volá se při každém otevření
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Můžeme zkontrolovat, zda je tabulka prázdná, a pokud ano, doplnit data
                        // To ale vyžaduje přístup k DAO, který uvnitř callbacku na 'db' nemáme přímo,
                        // takže to necháme na onCreate/onDestructiveMigration.
                    }
                })
                .build()
            INSTANCE = instance
            instance
        }
    }

    private suspend fun fillWithDefaultExercises(context: Context) {

        val database = getDatabase(context)
        val exerciseDao = database.exerciseDao()

        // Rychlá kontrola: Pokud už tam data jsou, nic nedělej (aby se neduplikovaly)
        if (exerciseDao.getExerciseCount() > 0) {
            Log.d("DatabaseInstance", "Databáze už obsahuje cviky, přeskakuji import.")
            return
        }

        try {
            Log.d("DatabaseInstance", "Začínám import JSONu...")
            val inputStream = context.resources.openRawResource(R.raw.default_exercises)
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            val type = object : TypeToken<List<Exercise>>() {}.type
            val exercises: List<Exercise> = Gson().fromJson(jsonString, type)

            Log.d("DatabaseInstance", "Načteno ${exercises.size} cviků z JSONu. Ukládám...")

            val entities = exercises.map { it.toEntity() }
            exerciseDao.insertAll(entities)

            Log.d("DatabaseInstance", "Hotovo! Cviky uloženy.")

        } catch (e: Exception) {
            // TOTO JE DŮLEŽITÉ: Podívej se do Logcatu (vyhledat "DatabaseInstance" nebo "System.err")
            Log.e("DatabaseInstance", "Chyba při importu cviků!", e)
            e.printStackTrace()
        }
    }
}