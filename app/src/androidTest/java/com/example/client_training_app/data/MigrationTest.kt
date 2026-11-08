package com.example.client_training_app.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.client_training_app.data.database.AppDatabase
import com.example.client_training_app.data.database.DatabaseInstance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Migrace databáze se zachováním dat. Používá exportovaná schémata z `app/schemas`. */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDb = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate3To4_keepsDataAndMarksExistingTemplatesAsNotArchived() {
        helper.createDatabase(testDb, 3).apply {
            execSQL("INSERT INTO clients (id, firstName, lastName) VALUES ('c1', 'Test', 'Klient')")
            execSQL("INSERT INTO training_units (id, name, note, clientId) VALUES ('u1', 'Push A', 'poznámka', NULL)")
            execSQL("INSERT INTO training_units (id, name, note, clientId) VALUES ('u2', 'Pull B', NULL, 'c1')")
            close()
        }

        val db = helper.runMigrationsAndValidate(testDb, 4, true, DatabaseInstance.MIGRATION_3_4)

        db.query("SELECT id, name, note, clientId, isArchived FROM training_units ORDER BY id").use { cursor ->
            assertEquals(2, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertEquals("Push A", cursor.getString(1))
            assertEquals("poznámka", cursor.getString(2))
            assertEquals(0, cursor.getInt(4))
            assertTrue(cursor.moveToNext())
            assertEquals("c1", cursor.getString(3))
            assertEquals(0, cursor.getInt(4))
        }
    }
}
