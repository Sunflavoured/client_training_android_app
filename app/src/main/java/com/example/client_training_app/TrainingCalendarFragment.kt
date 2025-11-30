package com.example.client_training_app

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.client_training_app.data.database.ClientRepository
import com.example.client_training_app.data.database.TrainingSessionEntity
import com.example.client_training_app.databinding.FragmentTrainingCalendarBinding
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
import com.example.client_training_app.ui.TrainingSessionAdapter
import java.util.UUID

class TrainingCalendarFragment : Fragment(R.layout.fragment_training_calendar) {

    private val args: TrainingCalendarFragmentArgs by navArgs()
    private lateinit var binding: FragmentTrainingCalendarBinding

    // Zde držíme aktuálně vybrané datum (na které uživatel kliknul)
    private var selectedDate: LocalDate? = null

    // Zde držíme všechna data z databáze, seskupená podle data
    // Klíč = Datum, Hodnota = Seznam tréninků v ten den
    private var trainingsByDate: Map<LocalDate, List<TrainingSessionEntity>> = emptyMap()

    // Definice adapteru
    private lateinit var trainingAdapter: TrainingSessionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrainingCalendarBinding.bind(view)

        // 1. Nastavení RecyclerView a Adapteru
        setupRecyclerView()

        // 2. Nastavení Kalendáře
        setupCalendar()

        // 3. Tlačítko Přidat (+)
        setupFab()

        // 4. Načítání dat
        loadTrainings()
    }

    private fun setupRecyclerView() {
        trainingAdapter = TrainingSessionAdapter { session ->
            // TODO: Kliknutí na trénink -> Otevřít detail tréninku (RunWorkoutFragment)
            android.widget.Toast.makeText(requireContext(), "Kliknuto: ${session.name}", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.rvTrainings.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvTrainings.adapter = trainingAdapter
    }

    private fun setupFab() {
        binding.fabAddTraining.setOnClickListener {
            val dateToAdd = selectedDate ?: java.time.LocalDate.now()

            // PROZATÍM: Rychlé přidání tréninku přes dialog (pro testování teček)
            // V budoucnu: Navigace na "AddTrainingFragment"
            showAddTrainingDialog(dateToAdd)
        }
    }

    private fun showAddTrainingDialog(date: LocalDate) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Nový trénink na ${date.dayOfMonth}. ${date.monthValue}.")

        val input = android.widget.EditText(requireContext())
        input.hint = "Název tréninku (např. Nohy)"
        builder.setView(input)

        builder.setPositiveButton("Uložit") { _, _ ->
            val name = input.text.toString()
            if (name.isNotEmpty()) saveTraining(name, date)
        }
        builder.setNegativeButton("Zrušit") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun saveTraining(name: String, date: LocalDate) {
        val repository = ClientRepository(requireContext())
        val timestamp = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val newSession = TrainingSessionEntity(
            id = UUID.randomUUID().toString(),
            clientId = args.clientId,
            dateInMillis = timestamp,
            name = name,
            isCompleted = false
        )

        viewLifecycleOwner.lifecycleScope.launch {
            repository.addTrainingSession(newSession) // Tuto metodu musíš mít v Repository!
            android.widget.Toast.makeText(requireContext(), "Trénink přidán!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    private fun loadTrainings() {
        val repository = ClientRepository(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            // Sledujeme změny v tabulce tréninků pro tohoto klienta
            repository.getTrainingSessionsFlow(args.clientId).collectLatest { sessions ->

                // Přeskupíme data: List<Entity> -> Map<LocalDate, List<Entity>>
                trainingsByDate = sessions.groupBy { it.dateInMillis.toLocalDate() }

                // Řekneme kalendáři, ať se překreslí (aby se objevily tečky)
                binding.calendarView.notifyCalendarChanged()

                // Pokud máme vybraný den, aktualizujeme seznam pod kalendářem
                if (selectedDate != null) {
                    updateAdapterForDate(selectedDate!!)
                }
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

        // Nastavení textu měsíce (např. Prosinec 2025)
        binding.tvCurrentMonth.text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}"

        // Scroll listener - aktualizuje nadpis měsíce při posouvání
        binding.calendarView.monthScrollListener = { month ->
            binding.tvCurrentMonth.text = "${month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.yearMonth.year}"
        }

        // Tlačítka pro posun měsíců
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

        // --- BINDER: Jak se má vykreslit každý den ---
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data // Uložíme si data dne do kontejneru
                val textView = container.textView
                val dotView = container.dotView

                textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    // 1. Barva textu (černá pro aktuální měsíc)
                    textView.setTextColor(Color.BLACK)

                    // 2. Výběr dne (Selected state)
                    if (data.date == selectedDate) {
                        // Pokud je den vybrán -> Modré pozadí, bílý text
                        textView.setBackgroundResource(R.drawable.bg_dot)
                        textView.setTextColor(Color.WHITE)
                    } else {
                        // Pokud není vybrán -> Žádné pozadí
                        textView.background = null
                    }

                    // 3. Tečka (Dot) - má tento den nějaký trénink?
                    val trainings = trainingsByDate[data.date]
                    if (trainings != null && trainings.isNotEmpty()) {
                        dotView.isVisible = true
                        // Pokud je den vybrán, tečka by měla být bílá (aby byla vidět na modrém), jinak modrá
                        // To můžeme ladit později, zatím nechme default
                    } else {
                        dotView.isVisible = false
                    }

                } else {
                    // Dny z minulého/příštího měsíce (šedé)
                    textView.setTextColor(Color.LTGRAY)
                    textView.background = null
                    dotView.isVisible = false
                }
            }
        }
    }

    // Funkce volaná při kliknutí na den
    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date

            // Překreslíme starý den (aby zmizelo označení)
            oldDate?.let { binding.calendarView.notifyDateChanged(it) }
            // Překreslíme nový den (aby se objevil kruh)
            binding.calendarView.notifyDateChanged(date)

            // Aktualizujeme nadpis a seznam
            updateAdapterForDate(date)
        }
    }

    private fun updateAdapterForDate(date: LocalDate) {
        binding.tvSelectedDate.text = "Tréninky pro: ${date.dayOfMonth}. ${date.monthValue}."

        val trainings = trainingsByDate[date] ?: emptyList()
        trainingAdapter.submitList(trainings)
    }

    // --- ViewHolder pro den ---
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.tvDayText)
        val dotView: View = view.findViewById(R.id.viewDot)
        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                // Kliknutí funguje jen pro dny aktuálního měsíce
                if (day.position == DayPosition.MonthDate) {
                    selectDate(day.date)
                }
            }
        }
    }
}

// Pomocná funkce mimo třídu
fun Long.toLocalDate(): LocalDate {
    return java.time.Instant.ofEpochMilli(this)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
}