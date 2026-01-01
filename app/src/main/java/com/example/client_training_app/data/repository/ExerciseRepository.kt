package com.example.client_training_app.data.repository

import android.content.Context
import com.example.client_training_app.data.dao.ExerciseDao
import com.example.client_training_app.data.database.DatabaseInstance
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.model.toEntity
import com.example.client_training_app.model.toExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepository(context: Context) {

    private val exerciseDao: ExerciseDao = DatabaseInstance.getDatabase(context).exerciseDao()

    // UŽ ŽÁDNÉ manual loadDefaultExercises()!

    // Vrátí Flow se všemi cviky (Teď už jsou VŠECHNY v DB)
    fun getAllExercisesFlow(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises().map { entities ->
            entities.map { it.toExercise() }
        }
    }

    // Najdi cvik podle id (hledá jen v DB)
    suspend fun getExerciseById(id: String): Exercise? {
        val entity = exerciseDao.getExerciseById(id)
        return entity?.toExercise()
    }

    // Přidej nový custom cvik
    suspend fun addExercise(exercise: Exercise) {
        exerciseDao.insert(exercise.toEntity())
    }

    // Uprav existující cvik
    suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.update(exercise.toEntity())
    }

    // Smaž cvik
    suspend fun deleteExercise(exercise: Exercise) {
        // Tady si můžeš nechat pojistku, aby nešlo smazat defaultní
        if (!exercise.isDefault) {
            exerciseDao.delete(exercise.toEntity())
        }
    }
}