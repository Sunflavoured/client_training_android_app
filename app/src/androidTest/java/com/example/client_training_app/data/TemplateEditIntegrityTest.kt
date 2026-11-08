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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Regresní testy: úprava nebo smazání šablony nesmí poškodit naplánované a odcvičené tréninky.
 * Odcvičený trénink je záznam, naplánovaný trénink sleduje šablonu.
 * Běží nad databází v paměti, produkční data aplikace se nedotýkají.
 */
@RunWith(AndroidJUnit4::class)
class TemplateEditIntegrityTest {

    private lateinit var db: AppDatabase
    private lateinit var app: Application

    private val clientId = "client-1"
    private val unitId = "unit-1"
    private val completedScheduleId = 1L
    private val plannedScheduleId = 2L
    private val sessionId = "session-1"
    private val benchId = "ex-bench"
    private val squatId = "ex-squat"
    private val deadliftId = "ex-deadlift"

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

    private fun unitExercise(exerciseId: String, order: Int, forUnit: String = unitId) = TrainingUnitExerciseEntity(
        trainingUnitId = forUnit, exerciseId = exerciseId, orderIndex = order,
        sets = "2", reps = "10", weight = "50", time = null, distance = null, rir = null, rest = null,
        isRepsEnabled = true, isWeightEnabled = true, isTimeEnabled = false,
        isDistanceEnabled = false, isRirEnabled = false, isRestEnabled = false
    )

    /**
     * Klient, tři cviky, šablona se dvěma cviky (bench, dřep) a odcvičený trénink podle ní:
     * bench 2 série, dřep 1 série. Volitelně i naplánovaný (zatím neodcvičený) trénink.
     */
    private fun seed(sessionLinkedToUnit: Boolean = true, withPlanned: Boolean = false) = runBlocking {
        db.clientDao().insert(ClientEntity(clientId, "Test", "Klient", null, null, null, null))
        db.exerciseDao().insert(exercise(benchId, "Bench press"))
        db.exerciseDao().insert(exercise(squatId, "Dřep"))
        db.exerciseDao().insert(exercise(deadliftId, "Mrtvý tah"))
        db.trainingUnitDao().saveTrainingUnitWithExercises(
            TrainingUnitEntity(unitId, "Push A", "poznámka", null),
            listOf(unitExercise(benchId, 1), unitExercise(squatId, 2))
        )
        db.scheduledWorkoutDao().insertScheduledWorkout(
            ScheduledWorkoutEntity(
                id = completedScheduleId, clientId = clientId, trainingUnitId = unitId,
                date = 1_700_000_000_000L, isCompleted = true
            )
        )
        if (withPlanned) {
            db.scheduledWorkoutDao().insertScheduledWorkout(
                ScheduledWorkoutEntity(
                    id = plannedScheduleId, clientId = clientId, trainingUnitId = unitId,
                    date = 1_800_000_000_000L, isCompleted = false
                )
            )
        }
        val session = WorkoutSessionEntity(
            id = sessionId, scheduledWorkoutId = completedScheduleId, clientId = clientId,
            trainingUnitId = if (sessionLinkedToUnit) unitId else null,
            trainingName = "Push A", startTime = 1_700_000_000_000L, endTime = 1_700_000_900_000L
        )
        val sets = listOf(
            WorkoutSetResultEntity(sessionId = sessionId, exerciseId = benchId, setNumber = 1, reps = "8", weight = "60"),
            WorkoutSetResultEntity(sessionId = sessionId, exerciseId = benchId, setNumber = 2, reps = "7", weight = "60"),
            WorkoutSetResultEntity(sessionId = sessionId, exerciseId = squatId, setNumber = 1, reps = "5", weight = "80")
        )
        db.workoutSessionDao().saveCompletedWorkout(session, sets)
    }

    /** Stejný zápis, jaký provede editor šablony po stisku „Uložit“. */
    private fun editTemplateLikeEditor(addDeadlift: Boolean = false, removeSquat: Boolean = false) = runBlocking {
        val exercises = mutableListOf(unitExercise(benchId, 1))
        if (!removeSquat) exercises.add(unitExercise(squatId, exercises.size + 1))
        if (addDeadlift) exercises.add(unitExercise(deadliftId, exercises.size + 1))
        db.trainingUnitDao().updateTrainingUnitWithExercises(
            TrainingUnitEntity(unitId, "Push A (upraveno)", "nová poznámka", null),
            exercises
        )
    }

    /** Spustí obrazovku aktivního tréninku tak, jak ji otevře kalendář, a počká na výsledek. */
    private fun openFromCalendar(scheduleId: Long, unit: String = unitId): List<ActiveExerciseUi>? {
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

    // ---------- úprava šablony ----------

    @Test
    fun editingTemplate_keepsSessionLinkSchedulesAndSets() {
        seed()
        editTemplateLikeEditor()

        runBlocking {
            assertEquals("Odkaz odcvičeného tréninku na šablonu zmizel", unitId, db.workoutSessionDao().getSessionByScheduleId(completedScheduleId)?.trainingUnitId)
            assertEquals(1, db.scheduledWorkoutDao().countForUnit(unitId))
            assertEquals(2, db.trainingUnitDao().getExercisesForUnit(unitId).size)
            assertEquals(3, db.workoutSessionDao().getSetsForSession(sessionId).size)
        }
    }

    @Test
    fun finishedWorkout_isNotChangedWhenExerciseIsAddedToTemplate() {
        seed()
        editTemplateLikeEditor(addDeadlift = true)

        val exercises = openFromCalendar(completedScheduleId)

        assertNotNull("Obrazovka tréninku zůstala prázdná", exercises)
        assertEquals(listOf("Bench press", "Dřep"), exercises!!.map { it.exerciseName })
        assertEquals("60", exercises[0].sets[0].weight)
        assertEquals("8", exercises[0].sets[0].reps)
        assertEquals(2, exercises[0].sets.size)
        assertEquals("80", exercises[1].sets[0].weight)
    }

    @Test
    fun finishedWorkout_isNotChangedWhenExerciseIsRemovedFromTemplate() {
        seed()
        editTemplateLikeEditor(removeSquat = true)

        val exercises = openFromCalendar(completedScheduleId)

        assertNotNull(exercises)
        assertEquals(listOf("Bench press", "Dřep"), exercises!!.map { it.exerciseName })
    }

    @Test
    fun finishedWorkout_whenSessionAlreadyLostTemplateLink_stillShowsSavedSets() {
        // Takhle vypadají data, která už byla dřív poškozena (odkaz na šablonu je null)
        seed(sessionLinkedToUnit = false)

        val exercises = openFromCalendar(completedScheduleId)

        assertNotNull("Obrazovka tréninku zůstala prázdná", exercises)
        assertEquals(listOf("Bench press", "Dřep"), exercises!!.map { it.exerciseName })
        assertEquals("60", exercises[0].sets[0].weight)
    }

    @Test
    fun plannedWorkout_followsTemplateChanges() {
        seed(withPlanned = true)
        editTemplateLikeEditor(addDeadlift = true)

        val exercises = openFromCalendar(plannedScheduleId)

        assertNotNull(exercises)
        assertEquals(listOf("Bench press", "Dřep", "Mrtvý tah"), exercises!!.map { it.exerciseName })
    }

    // ---------- smazání šablony ----------

    @Test
    fun deletingTemplateWithHistory_archivesItAndKeepsCompletedWorkoutsInCalendar() {
        seed(withPlanned = true)
        val repository = TrainingUnitRepository(app)

        runBlocking { repository.deleteTrainingUnit(unitId) }

        runBlocking {
            // naplánovaný trénink zmizel, odcvičený zůstal
            val details = db.scheduledWorkoutDao().getAllScheduledWorkoutsWithDetails(clientId).first()
            assertEquals(listOf(completedScheduleId), details.map { it.schedule.id })
            assertEquals("Push A", details[0].trainingUnit.name)
            // šablona je jen skrytá
            assertTrue(db.trainingUnitDao().getUnitById(unitId)!!.isArchived)
            assertTrue(db.trainingUnitDao().getAllTrainingUnits().first().isEmpty())
            assertTrue(db.trainingUnitDao().getAvailableUnitsForClient(clientId).first().isEmpty())
            // historie je celá
            assertEquals(unitId, db.workoutSessionDao().getSessionByScheduleId(completedScheduleId)?.trainingUnitId)
            assertEquals(3, db.workoutSessionDao().getSetsForSession(sessionId).size)
        }
        // odcvičený trénink jde dál otevřít
        assertEquals(listOf("Bench press", "Dřep"), openFromCalendar(completedScheduleId)!!.map { it.exerciseName })
    }

    @Test
    fun deletingTemplateWithoutHistory_removesItCompletely() {
        seed()
        val otherUnit = "unit-2"
        runBlocking {
            db.trainingUnitDao().saveTrainingUnitWithExercises(
                TrainingUnitEntity(otherUnit, "Pull", null, null),
                listOf(unitExercise(benchId, 1, forUnit = otherUnit))
            )
            db.scheduledWorkoutDao().insertScheduledWorkout(
                ScheduledWorkoutEntity(id = 3L, clientId = clientId, trainingUnitId = otherUnit, date = 1_900_000_000_000L, isCompleted = false)
            )
        }

        runBlocking { TrainingUnitRepository(app).deleteTrainingUnit(otherUnit) }

        runBlocking {
            assertNull(db.trainingUnitDao().getUnitById(otherUnit))
            assertEquals(0, db.scheduledWorkoutDao().countForUnit(otherUnit))
            // ostatní šablona a její historie zůstaly
            assertEquals(1, db.scheduledWorkoutDao().countForUnit(unitId))
        }
    }

    @Test
    fun scheduleUsage_countsPlannedAndCompleted() {
        seed(withPlanned = true)

        val usage = runBlocking { TrainingUnitRepository(app).getScheduleUsage(unitId) }

        assertEquals(2, usage.total)
        assertEquals(1, usage.completed)
    }
}
