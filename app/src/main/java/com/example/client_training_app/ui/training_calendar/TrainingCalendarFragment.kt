package com.example.client_training_app.ui.training_calendar

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.R
import com.example.client_training_app.data.database.ScheduledWorkoutDetail
import com.example.client_training_app.databinding.FragmentTrainingCalendarBinding
import com.example.client_training_app.ui.adapters.ScheduledWorkoutAdapter
import com.example.client_training_app.ui.training.TrainingPickerBottomSheet
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class TrainingCalendarFragment : Fragment(R.layout.fragment_training_calendar) {

    private val args: TrainingCalendarFragmentArgs by navArgs()

    // 1. Používáme ViewModel
    private val viewModel: TrainingCalendarViewModel by viewModels()

    private lateinit var binding: FragmentTrainingCalendarBinding

    // Zde držíme aktuálně vybrané datum
    private var selectedDate: LocalDate? = null

    // 2. Data jsou nyní typu ScheduledWorkoutDetail (Mapování Datum -> Seznam detailů)
    private var trainingsByDate: Map<LocalDate, List<ScheduledWorkoutDetail>> = emptyMap()

    // 3. Nový adaptér
    private lateinit var scheduleAdapter: ScheduledWorkoutAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrainingCalendarBinding.bind(view)

        setupRecyclerView()
        setupCalendar()
        setupFab()

        // 4. Načtení dat a sledování změn
        loadData()
    }

    private fun setupRecyclerView() {
        // Inicializace adaptéru s lambdou pro kliknutí
        scheduleAdapter = ScheduledWorkoutAdapter { detail ->
            // TODO: Po kliknutí otevřít detail naplánovaného tréninku nebo Active Workout
            Toast.makeText(requireContext(), "Start: ${detail.trainingUnit.name}", Toast.LENGTH_SHORT).show()
        }

        binding.rvTrainings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTrainings.adapter = scheduleAdapter
    }

    private fun loadData() {
        // Nejdříve řekneme ViewModelu, ať načte data pro tohoto klienta
        viewModel.loadEvents(args.clientId)

        viewLifecycleOwner.lifecycleScope.launch {
            // Sledujeme flow 'events' z ViewModelu
            viewModel.events.collectLatest { eventsMap ->
                trainingsByDate = eventsMap

                // Překreslíme kalendář (tečky)
                binding.calendarView.notifyCalendarChanged()

                // Pokud je vybrán nějaký den, aktualizujeme seznam pod kalendářem
                selectedDate?.let { date ->
                    updateAdapterForDate(date)
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabAddTraining.setOnClickListener {
            val dateToAdd = selectedDate ?: LocalDate.now()
            // Místo starého dialogu voláme výběr tréninku
            showTrainingPicker(dateToAdd)
        }
    }

    // 5. Nová metoda pro zobrazení BottomSheetu
    private fun showTrainingPicker(date: LocalDate) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Získáme seznam dostupných tréninků
            viewModel.getAvailableTrainingUnits(args.clientId).collect { units ->

                if (units.isEmpty()) {
                    Toast.makeText(requireContext(), "Nemáte žádné uložené tréninky k naplánování.", Toast.LENGTH_SHORT).show()
                    return@collect
                }

                // Vytvoříme a zobrazíme Bottom Sheet
                val bottomSheet = TrainingPickerBottomSheet(
                    availableUnits = units,
                    onUnitSelected = { selectedUnit ->
                        // Uložíme do kalendáře přes ViewModel
                        viewModel.scheduleWorkout(args.clientId, selectedUnit.id, date)
                        Toast.makeText(
                            requireContext(),
                            "Naplánováno: ${selectedUnit.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
                bottomSheet.show(parentFragmentManager, "TrainingPicker")
            }
        }
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(24)
        val endMonth = currentMonth.plusMonths(24)
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)

        binding.tvCurrentMonth.text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}"

        binding.calendarView.monthScrollListener = { month ->
            binding.tvCurrentMonth.text = "${month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.yearMonth.year}"
        }

        binding.btnNextMonth.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.plusMonths(1))
            }
        }
        binding.btnPrevMonth.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.minusMonths(1))
            }
        }

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.textView
                val dotView = container.dotView

                textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    textView.setTextColor(Color.BLACK)

                    // Výběr dne
                    if (data.date == selectedDate) {
                        textView.setBackgroundResource(R.drawable.bg_dot) // Ujisti se, že tento drawable existuje
                        textView.setTextColor(Color.WHITE)
                    } else {
                        textView.background = null
                    }

                    // Tečka (Dot) - kontrolujeme novou mapu
                    val trainings = trainingsByDate[data.date]
                    if (!trainings.isNullOrEmpty()) {
                        dotView.isVisible = true
                        // Volitelné: Změna barvy tečky pokud je splněno
                        // val allCompleted = trainings.all { it.schedule.isCompleted }
                        // dotView.setBackgroundResource(...)
                    } else {
                        dotView.isVisible = false
                    }

                } else {
                    textView.setTextColor(Color.LTGRAY)
                    textView.background = null
                    dotView.isVisible = false
                }
            }
        }
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date

            oldDate?.let { binding.calendarView.notifyDateChanged(it) }
            binding.calendarView.notifyDateChanged(date)

            updateAdapterForDate(date)
        }
    }

    private fun updateAdapterForDate(date: LocalDate) {
        binding.tvSelectedDate.text = "Tréninky pro: ${date.dayOfMonth}. ${date.monthValue}."

        // Vytáhneme seznam pro daný den (nebo prázdný)
        val trainings = trainingsByDate[date] ?: emptyList()

        // Pošleme do adaptéru
        scheduleAdapter.submitList(trainings)
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.tvDayText)
        val dotView: View = view.findViewById(R.id.viewDot)
        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                if (day.position == DayPosition.MonthDate) {
                    selectDate(day.date)
                }
            }
        }
    }
}