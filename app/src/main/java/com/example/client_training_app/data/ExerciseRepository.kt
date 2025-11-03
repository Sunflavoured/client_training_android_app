package com.example.client_training_app.data

import android.content.Context
import com.example.client_training_app.R
import com.example.client_training_app.data.model.Exercise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class ExerciseRepository(private val context: Context) {
//načtení cviků z JSON souboru
    fun getExercises(): List<Exercise> {
        val inputStream = context.resources.openRawResource(R.raw.default_exercises)
        val reader = InputStreamReader(inputStream)
        val exerciseType = object : TypeToken<List<Exercise>>() {}.type
        return Gson().fromJson(reader, exerciseType)
    }
}
