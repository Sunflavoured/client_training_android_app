package com.example.client_training_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.data.database.ExerciseRepository
import com.example.client_training_app.databinding.FragmentExercisesBinding
import com.example.client_training_app.ui.exercises.ExerciseAdapter
import kotlinx.coroutines.launch

class ExerciseFragment : Fragment() {

    private var _binding: FragmentExercisesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ExerciseAdapter
    private lateinit var repository: ExerciseRepository

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

        setupRecyclerView() //funkce předávající id cviku TODO
        setupFAB() //nastavení floating button aby navigoval na fragment add exercise
        observeExercises() // funkce pro sledování Flow
    }

    private fun setupRecyclerView() {
        // Inicializujeme adapter s prázdným seznamem
        adapter = ExerciseAdapter(emptyList()) { exercise ->
            val action = ExerciseFragmentDirections
                .actionExercisesFragmentToExerciseDetailFragment(exercise.id)
            findNavController().navigate(action)
        }

        binding.exercisesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.exercisesRecyclerView.adapter = adapter
    }

    // Sledování Flow pro automatickou aktualizaci -> když proběhne změna ve flow, aktualizuje se seznam cviků
    private fun observeExercises() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllExercisesFlow().collect { exercises ->
                adapter.updateExercises(exercises)
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