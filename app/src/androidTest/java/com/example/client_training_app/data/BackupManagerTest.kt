package com.example.client_training_app.data

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.client_training_app.data.database.AppDatabase
import com.example.client_training_app.data.entity.ClientEntity
import com.example.client_training_app.utils.BackupManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Záloha a obnova databáze. Používá samostatnou databázi `backup_manager_test_db`,
 * produkční data aplikace se nedotýkají.
 */
@RunWith(AndroidJUnit4::class)
class BackupManagerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dbName = "backup_manager_test_db"
    private val workDir = File(context.cacheDir, "backup_test")

    private var db: AppDatabase? = null
    private var version = 0
    private lateinit var manager: BackupManager

    private fun openDb(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .allowMainThreadQueries()
            .build()
            .also { db = it }

    private fun client(id: String) = ClientEntity(id, "Test", id, null, null, null, null)

    private fun managerWith(currentVersion: () -> Int) = BackupManager(
        context = context,
        dbName = dbName,
        checkpoint = { db!!.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() } },
        closeDatabase = { db?.close(); db = null },
        currentVersion = currentVersion
    )

    @Before
    fun setUp() {
        context.deleteDatabase(dbName)
        workDir.deleteRecursively()
        workDir.mkdirs()
        openDb()
        version = db!!.openHelper.readableDatabase.version
        manager = managerWith { version }
    }

    @After
    fun tearDown() {
        db?.close()
        context.deleteDatabase(dbName)
        workDir.deleteRecursively()
    }

    @Test
    fun backupThenRestore_bringsBackTheBackedUpData() = runBlocking {
        db!!.clientDao().insert(client("c1"))
        val file = File(workDir, "backup.db")
        manager.createBackup(Uri.fromFile(file))

        db!!.clientDao().insert(client("c2"))
        assertEquals(2, db!!.clientDao().getAllClients().first().size)

        manager.restore(Uri.fromFile(file))

        openDb()
        val clients = db!!.clientDao().getAllClients().first()
        assertEquals(listOf("c1"), clients.map { it.lastName })
        // pojistná kopie původního stavu (před obnovou) vznikla
        assertTrue(File(context.filesDir, "pre_restore_backup.db").exists())
    }

    @Test
    fun inspect_reportsWhatTheBackupContains() = runBlocking {
        db!!.clientDao().insert(client("c1"))
        db!!.clientDao().insert(client("c2"))
        val file = File(workDir, "backup.db")
        manager.createBackup(Uri.fromFile(file))

        val check = manager.inspect(Uri.fromFile(file))

        assertTrue(check is BackupManager.Check.Ok)
        val info = (check as BackupManager.Check.Ok).info
        assertEquals(2, info.clients)
        assertEquals(version, info.dbVersion)
    }

    @Test
    fun inspect_rejectsFileThatIsNotADatabase() = runBlocking {
        val file = File(workDir, "notes.txt").apply { writeText("to není databáze") }

        val check = manager.inspect(Uri.fromFile(file))

        assertTrue(check is BackupManager.Check.Invalid)
    }

    @Test
    fun inspect_rejectsBackupFromNewerAppVersion() = runBlocking {
        val file = File(workDir, "backup.db")
        manager.createBackup(Uri.fromFile(file))

        // aplikace, která zná jen starší verzi databáze, nesmí novější zálohu obnovit
        val olderApp = managerWith { version - 1 }
        val check = olderApp.inspect(Uri.fromFile(file))

        assertTrue(check is BackupManager.Check.Invalid)
    }

    @Test
    fun restoreOfInvalidFile_changesNothing() = runBlocking {
        db!!.clientDao().insert(client("c1"))
        val garbage = File(workDir, "garbage.db").apply { writeText("rozbitý soubor") }

        val result = runCatching { manager.restore(Uri.fromFile(garbage)) }

        assertTrue(result.isFailure)
        // databáze se nezavřela a data jsou na místě
        assertEquals(1, db!!.clientDao().getAllClients().first().size)
    }
}
