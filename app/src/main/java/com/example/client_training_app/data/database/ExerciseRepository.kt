package com.example.client_training_app.data.database

import android.content.Context
import com.example.client_training_app.R
import com.example.client_training_app.model.Exercise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepository(private val context: Context) {

    private val exerciseDao: ExerciseDao = DatabaseInstance.getDatabase(context).exerciseDao()
    private val gson = Gson()

    // Načti default cviky z JSON
    private fun loadDefaultExercises(): List<Exercise> {
        val inputStream = context.resources.openRawResource(R.raw.default_exercises)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Exercise>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    // Vrátí Flow se všemi cviky (default + custom)
    fun getAllExercisesFlow(): Flow<List<Exercise>> {
        val defaultExercises = loadDefaultExercises()

        return exerciseDao.getAllExercises().map { customEntities ->
            val customExercises = customEntities.map { it.toExercise() }
            defaultExercises + customExercises  // Spojí default + custom
        }
    }

    //najdi cvik podle id
    suspend fun getExerciseById(id: String): Exercise? {
        // Zkusíme najít v custom cvicích
        val customExercise = exerciseDao.getExerciseById(id)
        if (customExercise != null) {
            return customExercise.toExercise()
        }
        // Pokud nenajdeme, hledáme v default cvicích
        return loadDefaultExercises().find { it.id == id }
    }

    // Přidej nový custom cvik
    suspend fun addExercise(exercise: Exercise) {
        exerciseDao.insert(exercise.toEntity())
    }

    // Uprav existující cvik
    suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.update(exercise.toEntity())
    }

    // Smaž cvik (pouze custom, ne default!)
    suspend fun deleteExercise(exercise: Exercise) {
        if (!exercise.isDefault) {
            exerciseDao.delete(exercise.toEntity())
        }
    }
}