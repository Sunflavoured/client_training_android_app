package com.example.client_training_app.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Krok 1: OPRAVA TABULKY 'exercises'
        // Musíme změnit sloupec 'description' na nullable.
        // V SQLite nemůžeme přímo měnit NOT NULL na NULL, musíme provést ALTER TABLE.

        // POZNÁMKA: V SQLite je změna NOT NULL na NULL složitá, ale pro jednoduchou nullable změnu
        // Room často akceptuje destrukční přístup na úrovni schématu.

        // Nejbezpečnější a nejčistší řešení je:
        // Vytvořit novou tabulku, zkopírovat data, smazat starou, přejmenovat novou.

        // Zkusme nejdříve jednodušší metodu (přejmenování + vytvoření nové):

        // 1. Přejmenuj starou tabulku exercises
        db.execSQL("ALTER TABLE exercises RENAME TO exercises_old")

        // 2. Vytvoř novou tabulku exercises s opraveným schématem (description může být NULL)
        // Předpokládám, že tvá ExerciseEntity verze 2 vypadá takto:
        db.execSQL(
            """
            CREATE TABLE exercises (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                description TEXT, -- Nyní NULLABLE!
                mediaType TEXT NOT NULL,
                mediaUrl TEXT,
                muscleGroups TEXT NOT NULL,
                isDefault INTEGER NOT NULL
            )
            """
        )

        // 3. Zkopíruj data ze staré do nové tabulky (sloupce musí odpovídat)
        db.execSQL(
            """
            INSERT INTO exercises (id, name, category, description, mediaType, mediaUrl, muscleGroups, isDefault)
            SELECT id, name, category, description, mediaType, mediaUrl, muscleGroups, isDefault FROM exercises_old
            """
        )

        // 4. Smaž starou tabulku
        db.execSQL("DROP TABLE exercises_old")

        // ----------------------------------------------------------------------------------

        // Krok 2: PŘIDÁNÍ TABULKY 'clients' (jak jsi měl)
        db.execSQL(
            """
            CREATE TABLE clients (
                id TEXT NOT NULL PRIMARY KEY,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                age INTEGER,
                weight REAL,
                notes TEXT NOT NULL
            )
            """
        )
    }
}

object DatabaseInstance {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "trainer_app_database"
            )
                // Kdekoliv zde v řetězci .build()
                //.addMigrations(MIGRATION_1_2) // <--- PŘIDÁNO SEM!
                .fallbackToDestructiveMigration() // při vývoji ruším migrace a vždy obnovuji databázi
                .build()
            INSTANCE = instance
            instance
        }
    }
}