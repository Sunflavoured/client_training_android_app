package com.example.client_training_app.ui.active_workout

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.R
import com.example.client_training_app.databinding.FragmentActiveWorkoutBinding
import com.example.client_training_app.ui.adapters.ActiveWorkoutAdapter
import java.util.Locale

class ActiveWorkoutFragment : Fragment(R.layout.fragment_active_workout) {

    private val args: ActiveWorkoutFragmentArgs by navArgs()
    private val viewModel: ActiveWorkoutViewModel by viewModels()
    private lateinit var binding: FragmentActiveWorkoutBinding

    // Zde deklarujeme adaptér, inicializujeme ho ale až v onViewCreated
    private lateinit var adapter: ActiveWorkoutAdapter

    // Timer proměnné
    private var seconds = 0
    private var running = true
    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (running) {
                seconds++
                updateTimerUI()
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentActiveWorkoutBinding.bind(view)

        // 1. Spuštění tréninku ve ViewModelu (jen při prvním vytvoření)
        if (savedInstanceState == null) {
            val scheduleId = if (args.scheduledWorkoutId == -1L) null else args.scheduledWorkoutId
            viewModel.startWorkout(args.trainingUnitId, args.clientId, scheduleId)
        }

        // 2. Nastavení UI
        setupRecyclerView()
        setupButtons()

        // 3. Sledování změn dat
        observeViewModel()

        // 4. Spuštění stopek
        handler.post(timerRunnable)
    }

    private fun setupRecyclerView() {
        // TADY JE TA IMPLEMENTACE ADAPTÉRU
        adapter = ActiveWorkoutAdapter(
            exercises = emptyList(), // Začínáme s prázdným seznamem, data přijdou z ViewModelu
            onAddSetClicked = { exerciseIndex ->
                // Uživatel klikl na "+", řekneme to ViewModelu
                viewModel.addSet(exerciseIndex)

                // Poznámka: Zde nemusíme volat adapter.notify...,
                // protože ViewModel aktualizuje LiveData a spustí se observeViewModel()
            }
        )

        binding.rvActiveExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActiveExercises.adapter = adapter

        // DŮLEŽITÉ PRO FORMULÁŘE:
        // Tímto řekneme RecyclerView, aby si pamatoval posledních 20 řádků v paměti,
        // i když odskrolují pryč. Díky tomu se nevymažou data, co uživatel napsal do EditTextu,
        // když posune obrazovku dolů a zase nahoru.
        binding.rvActiveExercises.setItemViewCacheSize(20)
    }

    private fun setupButtons() {
        binding.btnFinishWorkout.setOnClickListener {
            // Tlačítko Dokončit
            viewModel.finishWorkout()
        }
    }

    private fun observeViewModel() {
        // Sledujeme seznam cviků
        viewModel.activeExercises.observe(viewLifecycleOwner) { exercises ->
            // Jakmile se změní data (načtení nebo přidání série), pošleme je do adaptéru
            adapter.updateData(exercises)

            // Pokud bys chtěla dynamický název, musela bys ho posílat z ViewModelu.
            // Zatím necháme statický nebo vezmeme název první položky jako placeholder
            if (exercises.isNotEmpty() && binding.tvWorkoutTitle.text == "Načítám trénink...") {
                binding.tvWorkoutTitle.text = "Aktivní trénink"
            }
        }

        // Sledujeme, jestli je trénink hotový
        viewModel.isFinished.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                Toast.makeText(requireContext(), "Trénink úspěšně uložen! 💪", Toast.LENGTH_LONG).show()
                // Vrátíme se zpět (do kalendáře)
                findNavController().popBackStack()
            }
        }
    }

    private fun updateTimerUI() {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        val timeString = if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
        }
        binding.tvTimer.text = timeString
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Zastavíme stopky, abychom neplýtvali baterií na pozadí
        running = false
        handler.removeCallbacks(timerRunnable)
    }
}