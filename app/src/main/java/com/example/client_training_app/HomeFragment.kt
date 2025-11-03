package com.example.client_training_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.client_training_app.data.ExerciseRepository
import com.example.client_training_app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupClickListeners()
        loadExerciseCount()
        return binding.root

    }

    private fun setupClickListeners() {

        // Navigate to Profiles
        binding.cardProfiles.setOnClickListener {
            findNavController().navigate(R.id.profilesFragment)
        }

        // Navigate to Exercises
        binding.cardExercises.setOnClickListener {
            findNavController().navigate(R.id.exercisesFragment)
        }
    }

    //FUNKCE: Načte počet cviků
    private fun loadExerciseCount() {
        val repository = ExerciseRepository(requireContext())
        val exercises = repository.getExercises()
        val exerciseCount = exercises.size

        // Zobraz počet v UI
        binding.tvExerciseCount.text = exerciseCount.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}