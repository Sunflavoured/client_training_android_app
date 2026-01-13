package com.example.client_training_app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.client_training_app.data.dao.ClientDao
import com.example.client_training_app.data.dao.ExerciseDao
import com.example.client_training_app.data.dao.ScheduledWorkoutDao
import com.example.client_training_app.data.dao.TrainingUnitDao
import com.example.client_training_app.data.dao.WorkoutSessionDao
import com.example.client_training_app.data.entity.ClientEntity
import com.example.client_training_app.data.entity.ExerciseEntity
import com.example.client_training_app.data.entity.MeasurementEntity
import com.example.client_training_app.data.entity.ScheduledWorkoutEntity
import com.example.client_training_app.data.entity.WorkoutSessionEntity
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.entity.TrainingUnitExerciseEntity
import com.example.client_training_app.data.entity.WorkoutSetResultEntity

@Database(
    entities = [
        ClientEntity::class,
        TrainingUnitEntity::class,
        TrainingUnitExerciseEntity::class,
        MeasurementEntity::class,
        WorkoutSessionEntity::class,
        ExerciseEntity::class,
        ScheduledWorkoutEntity::class,
        WorkoutSetResultEntity::class],

    version = 11, // <--- ZVÝŠENÍ VERZE
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun clientDao(): ClientDao // <--- PŘIDANÁ DAO pro Client

    abstract fun trainingUnitDao(): TrainingUnitDao
    abstract fun scheduledWorkoutDao(): ScheduledWorkoutDao

    abstract fun workoutSessionDao(): WorkoutSessionDao
}

