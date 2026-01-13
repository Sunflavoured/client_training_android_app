package com.example.client_training_app.ui.training_calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.client_training_app.data.database.ScheduledWorkoutDetail
import com.example.client_training_app.data.entity.ScheduledWorkoutEntity
import com.example.client_training_app.data.repository.ScheduleRepository
import com.example.client_training_app.data.repository.TrainingUnitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TrainingCalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val scheduleRepository = ScheduleRepository(application)
    private val unitRepository = TrainingUnitRepository(application)

    // Mapa: Datum -> Seznam naplánovaných tréninků (pro Kalendář)
    private val _events = MutableStateFlow<Map<LocalDate, List<ScheduledWorkoutDetail>>>(emptyMap())
    val events: StateFlow<Map<LocalDate, List<ScheduledWorkoutDetail>>> = _events

    // Načtení dat pro klienta
    fun loadEvents(clientId: String) {
        viewModelScope.launch {
            scheduleRepository.getAllSchedulesWithDetailsFlow(clientId).collect { list ->
                // Převedeme List na Mapu podle LocalDate
                val grouped = list.groupBy {
                    Instant.ofEpochMilli(it.schedule.date)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
                _events.value = grouped
            }
        }
    }

    // Přidání tréninku do kalendáře
    fun scheduleWorkout(clientId: String, unitId: String, date: LocalDate) {
        viewModelScope.launch {
            val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val newSchedule = ScheduledWorkoutEntity(
                clientId = clientId,
                trainingUnitId = unitId,
                date = timestamp,
                isCompleted = false
            )
            scheduleRepository.scheduleWorkout(newSchedule)
        }
    }

    // Získání dostupných tréninků pro výběr (Dialog)
    // Kombinujeme globální tréninky + tréninky tohoto klienta
    fun getAvailableTrainingUnits(clientId: String) =
        unitRepository.getAvailableUnitsForClientFlow(clientId)
    // Předpokládám, že takovou metodu v Repository/DAO vytvoříš (Select * where clientId = NULL OR clientId = :id)
}