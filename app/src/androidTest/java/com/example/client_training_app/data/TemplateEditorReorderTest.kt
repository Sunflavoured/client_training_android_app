package com.example.client_training_app.data

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.client_training_app.data.database.AppDatabase
import com.example.client_training_app.data.database.DatabaseInstance
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.model.ExerciseCategory
import com.example.client_training_app.model.MediaType
import com.example.client_training_app.ui.training_unit.TrainingUnitEditorViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Změna pořadí cviků v editoru tréninkové jednotky. Databáze v paměti. */
@RunWith(AndroidJUnit4::class)
class TemplateEditorReorderTest {

    private lateinit var db: AppDatabase
    private lateinit var app: Application

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        app = context.applicationContext as Application
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        DatabaseInstance.setInstanceForTesting(db)
    }

    @After
    fun tearDown() {
        DatabaseInstance.setInstanceForTesting(null)
        db.close()
    }

    private fun exercise(name: String) = Exercise(
        id = "ex-$name", name = name, category = ExerciseCategory.STRENGTH, description = null,
        mediaType = MediaType.NONE, mediaUrl = null, muscleGroups = emptyList()
    )

    /** Provede kroky na hlavním vlákně a vrátí snímky pořadí (jméno:číslo) po každém kroku. */
    private fun runSteps(vararg steps: (TrainingUnitEditorViewModel) -> Unit): List<List<String>> {
        val snapshots = mutableListOf<List<String>>()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val vm = TrainingUnitEditorViewModel(app)
            listOf("A", "B", "C").forEach { vm.addExercise(exercise(it)) }
            steps.forEach { step ->
                step(vm)
                snapshots.add(vm.templateExercises.value!!.map { "${it.exercise.name}:${it.order}" })
            }
        }
        return snapshots
    }

    private fun TrainingUnitEditorViewModel.itemAt(index: Int) = templateExercises.value!![index]

    @Test
    fun movingExercises_reordersAndRenumbers() {
        val snapshots = runSteps(
            { it.moveExercise(it.itemAt(0), 1) },   // A dolů -> B A C
            { it.moveExercise(it.itemAt(2), -1) }   // C nahoru -> B C A
        )

        assertEquals(listOf("B:1", "A:2", "C:3"), snapshots[0])
        assertEquals(listOf("B:1", "C:2", "A:3"), snapshots[1])
    }

    @Test
    fun movingBeyondTheEdges_doesNothing() {
        val snapshots = runSteps(
            { it.moveExercise(it.itemAt(0), -1) },  // první nahoru
            { it.moveExercise(it.itemAt(2), 1) }    // poslední dolů
        )

        assertEquals(listOf("A:1", "B:2", "C:3"), snapshots[0])
        assertEquals(listOf("A:1", "B:2", "C:3"), snapshots[1])
    }
}
