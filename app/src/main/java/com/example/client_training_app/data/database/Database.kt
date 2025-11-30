package com.example.client_training_app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ClientEntity::class, MeasurementEntity::class, TrainingSessionEntity::class,ExerciseEntity::class],
    version = 4, // <--- ZVÝŠENÍ VERZE
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun clientDao(): ClientDao // <--- PŘIDANÁ DAO pro Client
}