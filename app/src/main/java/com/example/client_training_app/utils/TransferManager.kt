package com.example.client_training_app.utils

import android.content.Context
import android.net.Uri
import com.example.client_training_app.data.database.DatabaseInstance
import com.example.client_training_app.model.TrainingUnitExportPackage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.room.withTransaction

class TransferManager(private val context: Context) {

    private val db = DatabaseInstance.getDatabase(context)
    private val gson = Gson()

    // --- EXPORT ---
    suspend fun exportTrainingUnit(unitId: String): String? = withContext(Dispatchers.IO) {
        // 1. Načteme TrainingUnit
        val unit = db.trainingUnitDao().getUnitById(unitId) ?: return@withContext null

        // 2. Načteme cviky v tréninku (vazby)
        val exercisesInUnit = db.trainingUnitDao().getExercisesForUnit(unitId)

        // 3. Načteme definice cviků (aby klient věděl, co je to za cvik)
        // Musíme získat seznam ID cviků a vytáhnout je z ExerciseDao
        val exerciseIds = exercisesInUnit.map { it.exerciseId }
        val definitions = db.exerciseDao().getExercisesByIds(exerciseIds) // Tuto metodu musíme přidat do DAO

        // 4. Zabalíme to do balíčku
        val exportPackage = TrainingUnitExportPackage(
            unit = unit,
            exercisesInUnit = exercisesInUnit,
            exerciseDefinitions = definitions
        )

        // 5. Převedeme na JSON String
        return@withContext gson.toJson(exportPackage)
    }

    // --- IMPORT ---
    suspend fun importTrainingUnit(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Přečteme JSON ze souboru
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }

            // 2. Převedeme zpět na Objekt
            val pkg = gson.fromJson(jsonString, TrainingUnitExportPackage::class.java)

            // 3. Uložíme do databáze (Transakce zajistí, že se uloží buď vše, nebo nic)
            db.withTransaction {
                // 1. Uložíme definice cviků (aby klient měl cvik "Dřep")
                db.exerciseDao().upsertAll(pkg.exerciseDefinitions)

                // 2. Uložíme/Aktualizujeme hlavičku tréninku (Tady funguje to UUID!)
                db.trainingUnitDao().upsertUnit(pkg.unit)

                // 3. Vyčistíme starý seznam cviků pro tento trénink
                db.trainingUnitDao().deleteExercisesForUnit(pkg.unit.id)

                // 4. Vložíme nový seznam cviků
                // TADY JE TA POJISTKA: Resetujeme ID na 0.
                // Říkáme tím DB: "Považuj to za nové řádky a přiděl jim svá vlastní volná čísla."
                val safeExercises = pkg.exercisesInUnit.map {
                    it.copy(id = 0)
                }

                // Vložíme bezpečné entity
                db.trainingUnitDao().insertUnitExercises(safeExercises)
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}