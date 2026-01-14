package com.example.client_training_app.ui.training_unit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.R
import com.example.client_training_app.data.repository.ExerciseRepository
import com.example.client_training_app.databinding.FragmentExercisePickerBinding
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.ui.adapters.ExerciseAdapter // POUŽÍVÁME HLAVNÍ ADAPTER
import kotlinx.coroutines.launch

class ExercisePickerFragment : Fragment(R.layout.fragment_exercise_picker) {

    private lateinit var binding: FragmentExercisePickerBinding
    private lateinit var adapter: ExerciseAdapter // Recyklujeme ExerciseAdapter
    private lateinit var repository: ExerciseRepository

    // Seznamy pro filtrování
    private var allExercises: List<Exercise> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentExercisePickerBinding.bind(view)
        repository = ExerciseRepository(requireContext())

        setupRecyclerView()
        setupSearchBar()
        loadExercises()
    }

    private fun setupRecyclerView() {
        // TADY JE TA MAGIE: Používáme stejný ExerciseAdapter,
        // ale s jinou akcí po kliknutí (Lambda expression)
        adapter = ExerciseAdapter(emptyList()) { selectedExercise ->
            onExerciseSelected(selectedExercise)
        }

        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExercises.adapter = adapter
    }

    private fun onExerciseSelected(exercise: Exercise) {
        // 1. Uložíme výsledek do savedStateHandle předchozího fragmentu
        findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_exercise", exercise)

        // 2. Vrátíme se zpět (zavřeme picker)
        findNavController().popBackStack()
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterExercises(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadExercises() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllExercisesFlow().collect { exercises ->
                allExercises = exercises
                // Hned po načtení aplikujeme aktuální filtr (nebo zobrazíme vše)
                filterExercises(binding.etSearch.text.toString())
            }
        }
    }

    private fun filterExercises(query: String) {
        val filteredList = if (query.isEmpty()) {
            allExercises
        } else {
            allExercises.filter { exercise ->
                exercise.name.contains(query, ignoreCase = true) ||
                        exercise.muscleGroups.any { it.contains(query, ignoreCase = true) }
                // Pokud máš kategorii, přidej ji sem taky
            }
        }

        // Aktualizace adaptéru
        adapter.updateExercises(filteredList)

        // Řešení prázdného stavu
        if (filteredList.isEmpty()) {
            binding.rvExercises.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvExercises.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }
}