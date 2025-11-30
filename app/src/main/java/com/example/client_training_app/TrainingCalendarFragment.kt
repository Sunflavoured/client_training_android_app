package com.example.client_training_app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.client_training_app.databinding.FragmentTrainingCalendarBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class TrainingCalendarFragment : Fragment(R.layout.fragment_training_calendar) {

    private val args: TrainingCalendarFragmentArgs by navArgs() // Musíš přidat do nav_graph!
    private lateinit var binding: FragmentTrainingCalendarBinding

    // Zde budeme uchovávat načtené tréninky (data z DB)
    // Map<LocalDate, List<TrainingSessionEntity>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrainingCalendarBinding.bind(view)

        setupCalendar()

        // TODO: Načíst data z DB podle args.clientId
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12) // Rok zpět
        val endMonth = currentMonth.plusMonths(12)   // Rok dopředu
        val firstDayOfWeek = firstDayOfWeekFromLocale() // Automaticky Po nebo Ne podle jazyka

        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)

        // Nastavení textu měsíce (např. Prosinec 2025)
        binding.tvCurrentMonth.text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}"

        // BINDER: Tady se vykresluje každý den
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                container.textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    container.textView.setTextColor(resources.getColor(android.R.color.black, null))
                    // TODO: Zde zkontrolovat, jestli v tento den je trénink, a zobrazit tečku
                } else {
                    container.textView.setTextColor(resources.getColor(android.R.color.darker_gray, null))
                }
            }
        }
    }

    // ViewHolder pro jeden den v kalendáři
    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.tvDayText)
        val dotView: View = view.findViewById(R.id.viewDot)
        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                // TODO: Kliknutí na den -> zobrazit seznam tréninků pod kalendářem
            }
        }
    }
}