package com.example.client_training_app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.client_training_app.data.dao.ClientDao
import com.example.client_training_app.data.dao.ExerciseDao
import com.example.client_training_app.data.dao.TrainingUnitDao
import com.example.client_training_app.data.entity.ClientEntity
import com.example.client_training_app.data.entity.ExerciseEntity
import com.example.client_training_app.data.entity.MeasurementEntity
import com.example.client_training_app.data.entity.TrainingSessionEntity
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity

@Database(
    entities = [
        ClientEntity::class,
        TrainingUnitEntity::class,
        TrainingUnitExerciseEntity::class,
        MeasurementEntity::class,
        TrainingSessionEntity::class,
        ExerciseEntity::class],
    version = 6, // <--- ZVÝŠENÍ VERZE
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun clientDao(): ClientDao // <--- PŘIDANÁ DAO pro Client

    abstract fun trainingUnitDao(): TrainingUnitDao
}