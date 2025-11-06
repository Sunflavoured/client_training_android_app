package com.example.client_training_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.data.database.ExerciseRepository
import com.example.client_training_app.databinding.FragmentExercisesBinding
import com.example.client_training_app.ui.exercises.ExerciseAdapter

class ExerciseFragment : Fragment() {

    private var _binding: FragmentExercisesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExercisesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFAB()
    }
        private fun setupRecyclerView() {
            // Uložení cviků z ExerciseRepository do proměnné
            val repository = ExerciseRepository(requireContext())
            val exercises = repository.getExercises()

            // Skrz ExerciseAdapter vložíme proměnnou exercises do recycler view
            val adapter = ExerciseAdapter(exercises) { exercise ->
                val action = ExerciseFragmentDirections
                    .actionExercisesFragmentToExerciseDetailFragment(exercise.id)
                findNavController().navigate(action)
            }

        binding.exercisesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.exercisesRecyclerView.adapter = adapter
    }
    // ← NOVÁ FUNKCE: Nastavení FAB
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