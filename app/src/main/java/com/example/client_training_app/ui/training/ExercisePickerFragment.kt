package com.example.client_training_app.ui.training

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.R
import com.example.client_training_app.data.repository.ExerciseRepository // Měníme z database na repository
import com.example.client_training_app.databinding.FragmentExercisePickerBinding // Nový layout
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.ui.adapters.ExercisePickerAdapter // Nový adapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// POZNÁMKA: Tento fragment bude fungovat jako samostatná obrazovka pro výběr
class ExercisePickerFragment : Fragment(R.layout.fragment_exercise_picker) {

    private lateinit var binding: FragmentExercisePickerBinding
    private lateinit var adapter: ExercisePickerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentExercisePickerBinding.bind(view)

        setupRecyclerView()
        loadExercises()

        // TODO: Zde se přidá SearchBar
    }

    private fun setupRecyclerView() {
        // Při kliknutí na cvik: musíme cvik vrátit zpět do TrainingUnitEditorFragmentu
        adapter = ExercisePickerAdapter { exercise ->
            // 🔥 ODESLÁNÍ DAT ZPĚT DO EDITORU 🔥

            // Nastavíme výsledek s klíčem "selected_exercise"
            findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_exercise", exercise)

            // Vrátíme se zpět do předchozího fragmentu (Editoru)
            findNavController().popBackStack()
        }

        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExercises.adapter = adapter
    }

    private fun loadExercises() {
        // Používáme Repository z nové složky
        val repository = ExerciseRepository(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllExercisesFlow().collectLatest { exercises ->
                adapter.updateExercises(exercises)
                // TODO: Zobrazit empty state, pokud seznam prázdný
            }
        }
    }
}