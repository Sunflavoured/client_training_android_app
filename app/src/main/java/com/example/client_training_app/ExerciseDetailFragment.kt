package com.example.client_training_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.client_training_app.data.database.ExerciseRepository
import com.example.client_training_app.databinding.FragmentExerciseDetailBinding

class ExerciseDetailFragment : Fragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = ExerciseDetailFragmentArgs.fromBundle(requireArguments())
        val exerciseId = args.exerciseId

        val repository = ExerciseRepository(requireContext())
        val exercise = repository.getExercises().find { it.id == exerciseId }

        exercise?.let {
            binding.exerciseName.text = it.name
            binding.exerciseCategory.text = it.category.displayName //protože je to enum list
            binding.exerciseDescription.text = it.description

            // Pokud jsou svalové skupiny prázdné, schovej celou kartu
            if (it.muscleGroups.isEmpty()) {
                binding.cardMuscleGroups.visibility = View.GONE
            } else {
                binding.cardMuscleGroups.visibility = View.VISIBLE
                binding.exerciseMuscleGroups.text = it.muscleGroups.joinToString(", ")
            }

            binding.exerciseIsDefault.text = if (it.isDefault) "Ano" else "Ne"
        } ?: run {
            binding.exerciseName.text = "Cvik nenalezen"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}