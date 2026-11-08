package com.example.client_training_app.data

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.client_training_app.data.database.AppDatabase
import com.example.client_training_app.data.database.DatabaseInstance
import com.example.client_training_app.data.entity.ClientEntity
import com.example.client_training_app.data.entity.ExerciseEntity
import com.example.client_training_app.data.entity.ScheduledWorkoutEntity
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity
import com.example.client_training_app.data.entity.WorkoutSessionEntity
import com.example.client_training_app.data.entity.WorkoutSetResultEntity
import com.example.client_training_app.data.repository.TrainingUnitRepository
import com.example.client_training_app.model.ActiveExerciseUi
import com.example.client_training_app.ui.active_workout.ActiveWorkoutViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Regresní testy: úprava nebo smazání šablony nesmí poškodit naplánované a odcvičené tréninky.
 * Běží nad databází v paměti, produkční data aplikace se nedotýkají.
 */
@RunWith(AndroidJUnit4::class)
class TemplateEditIntegrityTest {

    private lateinit var db: AppDatabase
    private lateinit var app: Application

    private val clientId = "client-1"
    private val unitId = "unit-1"
    private val scheduleId = 1L
    private val sessionId = "session-1"
    private val benchId = "ex-bench"
    private val squatId = "ex-squat"

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        app = context.applicationContext as Application
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        DatabaseInstance.setInstanceForTesting(db)
    }

    @After
    fun tearDown() {
        DatabaseInstance.setInstanceForTesting(null)
        db.close()
    }

    // ---------- pomocné funkce ----------

    private fun exercise(id: String, name: String) = ExerciseEntity(
        id = id, name = name, category = "STRENGTH", description = null,
        mediaType = "NONE", mediaUrl = null, muscleGroups = listOf("Hrudník"), isDefault = false
    )

    private fun unitExercise(exerciseId: String, order: Int) = TrainingUnitExerciseEntity(
        trainingUnitId = unitId, exerciseId = exerciseId, orderIndex = order,
        sets = "2", reps = "10", weight = "50", time = null, distance = null, rir = null, rest = null,
        isRepsEnabled = true, isWeightEnabled = true, isTimeEnabled = false,
        isDistanceEnabled = false, isRirEnabled = false, isRestEnabled = false
    )

    /** Klient, dva cviky, šablona se dvěma cviky, naplánovaný a odcvičený trénink s uloženými sériemi. */
    private fun seed(sessionLinkedToUnit: Boolean = true) = runBlocking {
        db.clientDao().insert(ClientEntity(clientId, "Test", "Klient", null, null, null, null))
        db.exerciseDao().insert(exercise(benchId, "Bench press"))
        db.exerciseDao().insert(exercise(squatId, "Dřep"))
        db.trainingUnitDao().saveTrainingUnitWithExercises(
            TrainingUnitEntity(unitId, "Push A", "poznámka", null),
            listOf(unitExercise(benchId, 1), unitExercise(squatId, 2))
        )
        db.scheduledWorkoutDao().insertScheduledWorkout(
            ScheduledWorkoutEntity(
                id = scheduleId, clientId = clientId, trainingUnitId = unitId,
                date = 1_700_000_000_000L, isCompleted = true
            )
        )
        val session = WorkoutSessionEntity(
            id = sessionId, scheduledWorkoutId = scheduleId, clientId = clientId,
            trainingUnitId = if (sessionLinkedToUnit) unitId else null,
            trainingName = "Push A", startTime = 1_700_000_000_000L, endTime = 1_700_000_900_000L
        )
        val sets = listOf(
            WorkoutSetResultEntity(sessionId = sessionId, exerciseId = benchId, setNumber = 1, reps = "8", weight = "60"),
            WorkoutSetResultEntity(sessionId = sessionId, exerciseId = benchId, setNumber = 2, reps = "7", weight = "60")
        )
        db.workoutSessionDao().saveCompletedWorkout(session, sets)
    }

    /** Stejný zápis, jaký provede editor šablony po stisku „Uložit“. */
    private fun editTemplateLikeEditor() = runBlocking {
        db.trainingUnitDao().updateTrainingUnitWithExercises(
            TrainingUnitEntity(unitId, "Push A (upraveno)", "nová poznámka", null),
            listOf(unitExercise(benchId, 1), unitExercise(squatId, 2))
        )
    }

    /** Spustí obrazovku aktivního tréninku tak, jak ji otevře kalendář, a počká na výsledek. */
    private fun openFromCalendar(unit: String = unitId): List<ActiveExerciseUi>? {
        val latch = CountDownLatch(1)
        var result: List<ActiveExerciseUi>? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val vm = ActiveWorkoutViewModel(app)
            vm.activeExercises.observeForever {
                result = it
                latch.countDown()
            }
            vm.startWorkout(unit, clientId, scheduleId)
        }
        latch.await(5, TimeUnit.SECONDS)
        return result
    }

    // ---------- testy ----------

    @Test
    fun editingTemplate_keepsSessionLinkSchedulesAndSets() {
        seed()
        editTemplateLikeEditor()

        runBlocking {
            assertEquals("Odkaz odcvičeného tréninku na šablonu zmizel", unitId, db.workoutSessionDao().getSessionByScheduleId(scheduleId)?.trainingUnitId)
            assertEquals(1, db.scheduledWorkoutDao().countForUnit(unitId))
            assertEquals(2, db.trainingUnitDao().getExercisesForUnit(unitId).size)
            assertEquals(2, db.workoutSessionDao().getSetsForSession(sessionId).size)
        }
    }

    @Test
    fun finishedWorkout_afterTemplateEdit_stillShowsSavedSets() {
        seed()
        editTemplateLikeEditor()

        val exercises = openFromCalendar()

        assertNotNull("Obrazovka tréninku zůstala prázdná", exercises)
        assertEquals(2, exercises!!.size)
        assertEquals("Bench press", exercises[0].exerciseName)
        assertEquals("60", exercises[0].sets[0].weight)
        assertEquals("8", exercises[0].sets[0].reps)
    }

    @Test
    fun finishedWorkout_whenSessionAlreadyLostTemplateLink_stillShowsSavedSets() {
        // Takhle vypadají data, která už byla dřív poškozena (odkaz na šablonu je null)
        seed(sessionLinkedToUnit = false)

        val exercises = openFromCalendar()

        assertNotNull("Obrazovka tréninku zůstala prázdná", exercises)
        assertEquals(2, exercises!!.size)
        assertEquals("60", exercises[0].sets[0].weight)
    }

    @Test
    fun deletingScheduledTemplate_removesPlanButKeepsHistory() {
        seed()
        val repository = TrainingUnitRepository(app)

        runBlocking { repository.deleteTrainingUnit(unitId) }

        runBlocking {
            assertNull(db.trainingUnitDao().getUnitById(unitId))
            assertEquals(0, db.scheduledWorkoutDao().countForUnit(unitId))
            assertEquals("Historie sérií musí zůstat", 2, db.workoutSessionDao().getSetsForSession(sessionId).size)
        }
    }

    @Test
    fun scheduleUsage_countsPlannedAndCompleted() {
        seed()
        runBlocking {
            db.scheduledWorkoutDao().insertScheduledWorkout(
                ScheduledWorkoutEntity(id = 2L, clientId = clientId, trainingUnitId = unitId, date = 1_800_000_000_000L, isCompleted = false)
            )
        }
        val usage = runBlocking { TrainingUnitRepository(app).getScheduleUsage(unitId) }

        assertEquals(2, usage.total)
        assertEquals(1, usage.completed)
    }
}
