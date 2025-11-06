package com.example.client_training_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.client_training_app.data.database.ExerciseRepository
import com.example.client_training_app.data.model.Exercise
import com.example.client_training_app.data.model.ExerciseCategory
import com.example.client_training_app.data.model.MediaType
import com.example.client_training_app.databinding.FragmentAddExerciseBinding
import kotlinx.coroutines.launch
import java.util.UUID

class AddExerciseFragment : Fragment() {
    private var _binding: FragmentAddExerciseBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ExerciseRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExerciseBinding.inflate(inflater, container, false)

        //  Initialize repository here
        repository = ExerciseRepository(requireContext())

        setupCategoryDropdown()
        setupSaveButton()
        return binding.root
    }

    private fun setupCategoryDropdown() {
        val categories = ExerciseCategory.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, categories)
        binding.actvCategory.setAdapter(adapter)

        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
            val selected = ExerciseCategory.values()[position]
            binding.tilMuscleGroups.visibility =
                if (selected == ExerciseCategory.STRENGTH) View.VISIBLE else View.GONE
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveExercise.setOnClickListener {
            val name = binding.etExerciseName.text.toString().trim()
            val categoryName = binding.actvCategory.text.toString().trim()
            val description = binding.etExerciseDescription.text.toString().trim()
            val muscleGroupsText = binding.etMuscleGroups.text.toString().trim()

            if (name.isEmpty() || categoryName.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Vyplňte všechna povinná pole", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = ExerciseCategory.values().find { it.displayName == categoryName }
                ?: ExerciseCategory.STRENGTH

            val muscleGroups =
                if (category == ExerciseCategory.STRENGTH && muscleGroupsText.isNotEmpty())
                    muscleGroupsText.split(",").map { it.trim() }
                else emptyList()

            val newExercise = Exercise(
                id = UUID.randomUUID().toString(),
                name = name,
                category = category,
                description = description,
                mediaType = MediaType.NONE,
                mediaUrl = null,
                muscleGroups = muscleGroups,
                isDefault = false
            )

            lifecycleScope.launch {
                repository.addExercise(newExercise)
                Toast.makeText(requireContext(), "Cvik uložen", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}