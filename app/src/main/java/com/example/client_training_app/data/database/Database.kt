package com.example.client_training_app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ExerciseEntity::class, ClientEntity::class, MeasurementEntity::class], // <--- PŘIDANÁ ClientEntity!
    version = 3, // <--- ZVÝŠENÍ VERZE
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun clientDao(): ClientDao // <--- PŘIDANÁ DAO pro Client
}