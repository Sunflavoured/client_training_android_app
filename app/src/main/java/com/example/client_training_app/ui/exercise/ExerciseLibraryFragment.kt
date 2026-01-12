package com.example.client_training_app.ui.exercise

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.R
import com.example.client_training_app.data.repository.ExerciseRepository
import com.example.client_training_app.databinding.FragmentExercisesBinding
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.ui.adapters.ExerciseAdapter
import kotlinx.coroutines.launch

class ExerciseLibraryFragment : Fragment() {

    private var _binding: FragmentExercisesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ExerciseAdapter
    private lateinit var repository: ExerciseRepository

    // Pro uložení všech cviků a filtrovaných cviků
    private var allExercises: List<Exercise> = emptyList()
    private var filteredExercises: List<Exercise> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExercisesBinding.inflate(inflater, container, false)
        repository = ExerciseRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView() //funkce předávající id cviku
        setupFAB() //nastavení floating button aby navigoval na fragment add exercise
        observeExercises() // funkce pro sledování Flow
        setupSearchBar()
    }

    private fun setupRecyclerView() {
        // Inicializujeme adapter s prázdným seznamem
        adapter = ExerciseAdapter(emptyList()) { exercise ->
            val action = ExerciseLibraryFragmentDirections
                .actionExercisesFragmentToExerciseDetailFragment(exercise.id)
            findNavController().navigate(action)
        }

        binding.exercisesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.exercisesRecyclerView.adapter = adapter
    }

    // funkce pro nastavení search baru
    private fun setupSearchBar() {
        // Real-time search při psaní
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                filterExercises(query)

                // Zobrazit/skrýt tlačítko pro vymazání
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Tlačítko pro vymazání hledání
        binding.btnClearSearch.setOnClickListener {
            binding.searchEditText.text.clear()
        }

        // Tlačítko pro filtrování (prozatím zobrazí toast, později přidáme dialog)
        binding.btnFilter.setOnClickListener {
            // TODO: Otevřít dialog s filtry (kategorie, svalové skupiny)
            Toast.makeText(requireContext(), "Filtry brzy...", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ Funkce pro filtrování cviků
    private fun filterExercises(query: String) {
        filteredExercises = if (query.isEmpty()) {
            allExercises
        } else {
            allExercises.filter { exercise ->
                // Rozšířené filtrování (název + kategorie + svalové skupiny)
                exercise.name.contains(query, ignoreCase = true) ||
                        exercise.category.displayName.contains(query, ignoreCase = true) ||
                        exercise.muscleGroups.any { it.contains(query, ignoreCase = true) }
            }
        }
        updateUI()
    }

    // ✅ Aktualizace UI s filtrovanými cviky
    private fun updateUI() {
        if (filteredExercises.isEmpty()) {
            binding.exercisesRecyclerView.visibility = View.GONE
            binding.emptyStateTextView.visibility = View.VISIBLE
        } else {
            binding.exercisesRecyclerView.visibility = View.VISIBLE
            binding.emptyStateTextView.visibility = View.GONE
            adapter.updateExercises(filteredExercises)
        }
    }


    // Sledování Flow pro automatickou aktualizaci -> když proběhne změna ve flow, aktualizuje se seznam cviků
    private fun observeExercises() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllExercisesFlow().collect { exercises ->
                // 1. Aktualizujeme náš "master" seznam
                allExercises = exercises

                // 2. Znovu aplikujeme aktuální filtr (co je v search baru)
                // Tato funkce už zavolá updateUI(), který aktualizuje adaptér.
                filterExercises(binding.searchEditText.text.toString())
            }
        }
    }

    private fun setupFAB() {
        binding.fabAddExercise.setOnClickListener {
            findNavController().navigate(R.id.action_exercisesFragment_to_addExerciseFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}