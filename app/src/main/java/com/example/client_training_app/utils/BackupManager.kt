package com.example.client_training_app.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.example.client_training_app.data.database.DatabaseInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Záloha a obnova celé databáze aplikace do jednoho souboru.
 *
 * Záloha je kopie souboru databáze (po sloučení žurnálu WAL). Obnova soubor nejdřív zkontroluje (hlavička SQLite,
 * kontrola integrity, verze, očekávané tabulky), aktuální data uloží do interní pojistné kopie a teprve potom
 * databázi zavře a nahradí. Po obnově musí aplikace spustit proces znovu.
 */
class BackupManager(
    private val context: Context,
    private val dbName: String = DatabaseInstance.DB_NAME,
    private val checkpoint: () -> Unit = { DatabaseInstance.checkpoint(context) },
    private val closeDatabase: () -> Unit = { DatabaseInstance.close() },
    private val currentVersion: () -> Int = { DatabaseInstance.currentVersion(context) }
) {

    /** Co záloha obsahuje (pro potvrzení před obnovou). */
    data class Info(
        val dbVersion: Int,
        val clients: Int,
        val trainingUnits: Int,
        val workoutSessions: Int
    )

    sealed class Check {
        data class Ok(val info: Info) : Check()
        data class Invalid(val reason: String) : Check()
    }

    /** Uloží kopii databáze do zvoleného souboru. */
    suspend fun createBackup(target: Uri) = withContext(Dispatchers.IO) {
        checkpoint()
        val dbFile = context.getDatabasePath(dbName)
        val output = context.contentResolver.openOutputStream(target, "wt")
            ?: throw IOException("Do vybraného souboru nelze zapisovat")
        output.use { out -> dbFile.inputStream().use { it.copyTo(out) } }
    }

    /** Zkontroluje soubor se zálohou, nic nemění. */
    suspend fun inspect(source: Uri): Check = withContext(Dispatchers.IO) {
        val temp = tempFile()
        try {
            copyToTemp(source, temp)
            validate(temp)
        } catch (e: Exception) {
            Check.Invalid("Soubor se nepodařilo přečíst (${e.message})")
        } finally {
            deleteWithSidecars(temp)
        }
    }

    /** Nahradí aktuální databázi zálohou. Vyhodí výjimku, pokud je záloha neplatná, a v tom případě nic nezmění. */
    suspend fun restore(source: Uri) = withContext(Dispatchers.IO) {
        val temp = tempFile()
        try {
            copyToTemp(source, temp)
            val check = validate(temp)
            if (check is Check.Invalid) throw IllegalStateException(check.reason)
            // Po kontrole smažeme jen pomocné soubory SQLite, samotná kopie zůstává pro obnovu
            listOf("-wal", "-shm", "-journal").forEach { File(temp.path + it).delete() }

            val dbFile = context.getDatabasePath(dbName)
            // Pojistka: aktuální stav se před přepsáním uloží do interní zálohy
            runCatching {
                checkpoint()
                if (dbFile.exists()) {
                    dbFile.copyTo(File(context.filesDir, PRE_RESTORE_FILE), overwrite = true)
                }
            }

            closeDatabase()
            val safety = File(context.filesDir, PRE_RESTORE_FILE)
            try {
                deleteWithSidecars(dbFile)
                temp.copyTo(dbFile, overwrite = true)
            } catch (e: Exception) {
                // Nepovedlo se: vrátíme původní data z pojistné kopie
                if (safety.exists()) safety.copyTo(dbFile, overwrite = true)
                throw e
            }
        } finally {
            deleteWithSidecars(temp)
        }
    }

    // ---------- interní ----------

    private fun tempFile() = File(context.cacheDir, "restore_check.db")

    private fun copyToTemp(source: Uri, temp: File) {
        deleteWithSidecars(temp)
        val input = context.contentResolver.openInputStream(source)
            ?: throw IOException("Soubor nelze otevřít")
        input.use { inp -> temp.outputStream().use { inp.copyTo(it) } }
    }

    private fun deleteWithSidecars(file: File) {
        listOf("", "-wal", "-shm", "-journal").forEach { File(file.path + it).delete() }
    }

    private fun validate(file: File): Check {
        val header = ByteArray(16)
        val read = file.inputStream().use { it.read(header) }
        if (read < 16 || String(header, Charsets.US_ASCII) != "SQLite format 3\u0000") {
            return Check.Invalid("Soubor není záloha databáze")
        }

        val db = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
        try {
            val integrity = db.rawQuery("PRAGMA integrity_check", null).use {
                if (it.moveToFirst()) it.getString(0) else "?"
            }
            if (integrity != "ok") return Check.Invalid("Záloha je poškozená")

            val version = db.version
            if (version > currentVersion()) return Check.Invalid("Záloha pochází z novější verze aplikace")
            if (version < MIN_RESTORABLE_VERSION) return Check.Invalid("Záloha je ze starší verze aplikace, kterou nelze obnovit")

            val tables = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null).use { c ->
                buildSet { while (c.moveToNext()) add(c.getString(0)) }
            }
            if (!tables.containsAll(REQUIRED_TABLES)) return Check.Invalid("Soubor nevypadá jako záloha této aplikace")

            fun count(table: String) = db.rawQuery("SELECT COUNT(*) FROM $table", null).use {
                if (it.moveToFirst()) it.getInt(0) else 0
            }
            return Check.Ok(
                Info(
                    dbVersion = version,
                    clients = count("clients"),
                    trainingUnits = count("training_units"),
                    workoutSessions = count("workout_sessions")
                )
            )
        } finally {
            db.close()
        }
    }

    companion object {
        /** Nejstarší verze databáze, kterou umí aktuální aplikace zmigrovat (migrace 2→3→4). */
        private const val MIN_RESTORABLE_VERSION = 2
        private const val PRE_RESTORE_FILE = "pre_restore_backup.db"
        private val REQUIRED_TABLES = setOf("clients", "training_units", "workout_sessions", "exercises")
    }
}
