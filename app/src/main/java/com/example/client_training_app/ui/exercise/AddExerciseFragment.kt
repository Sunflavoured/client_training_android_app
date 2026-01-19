package com.example.client_training_app.ui.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.client_training_app.R
import com.example.client_training_app.data.repository.ExerciseRepository
import com.example.client_training_app.databinding.FragmentAddExerciseBinding
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.model.ExerciseCategory
import com.example.client_training_app.model.MediaType
import kotlinx.coroutines.launch
import java.util.UUID

class AddExerciseFragment : Fragment() {
    private var _binding: FragmentAddExerciseBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ExerciseRepository
    private val selectedMuscleGroups = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExerciseBinding.inflate(inflater, container, false)

        repository = ExerciseRepository(requireContext())

        setupCategoryDropdown()
        setupMuscleGroupsDropdown()  // ← NOVÁ FUNKCE
        setupSaveButton()

        return binding.root
    }

    private fun setupCategoryDropdown() {
        val categories = ExerciseCategory.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, categories)
        binding.actvCategory.setAdapter(adapter)

        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
            val selected = ExerciseCategory.values()[position]

            // Zobraz/skryj pole se svalovými skupinami
            binding.tilMuscleGroups.visibility =
                if (selected == ExerciseCategory.STRENGTH) View.VISIBLE else View.GONE

            // Vyčisti vybrané svalové skupiny
            if (selected != ExerciseCategory.STRENGTH) {
                selectedMuscleGroups.clear()
                updateMuscleGroupsDisplay()
            }
        }
    }

    // Nastavení Bottomsheetu pro výběr svalových partií
    private fun setupMuscleGroupsDropdown() {
        // Nastavení vzhledu "dropdownu"
        binding.actvMuscleGroups.keyListener = null
        binding.actvMuscleGroups.isFocusable = false
        binding.actvMuscleGroups.isClickable = true

        val showBottomSheet = {
            // Vytvoříme a zobrazíme BottomSheet
            val bottomSheet = MuscleGroupBottomSheet(
                preSelectedMuscles = selectedMuscleGroups // Pošleme tam, co už máme vybráno
            ) { newSelection ->
                // TOTO JE CALLBACK - Zavolá se, když uživatel klikne "Potvrdit"

                selectedMuscleGroups.clear()
                selectedMuscleGroups.addAll(newSelection)

                updateMuscleGroupsDisplay()
            }

            bottomSheet.show(parentFragmentManager, "MuscleGroupBottomSheet")
        }

        // Kliknutí na Textové pole i na Ikonku šipky spustí to samé
        binding.actvMuscleGroups.setOnClickListener { showBottomSheet() }
        binding.tilMuscleGroups.setEndIconOnClickListener { showBottomSheet() }
        binding.tilMuscleGroups.setOnClickListener { showBottomSheet() } // Pojistka
    }



    // Aktualizuj text v "dropdownu"
    private fun updateMuscleGroupsDisplay() {
        binding.actvMuscleGroups.setText(
            if (selectedMuscleGroups.isEmpty()) {
                "Vyberte svalové skupiny"
            } else {
                selectedMuscleGroups.joinToString(", ")
            }
        )
    }

    private fun setupSaveButton() {
        binding.btnSaveExercise.setOnClickListener {
            val name = binding.etExerciseName.text.toString().trim()
            val categoryName = binding.actvCategory.text.toString().trim()
            val description = binding.etExerciseDescription.text.toString().trim()

            // Validace
            if (name.isEmpty() || categoryName.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Vyplňte všechna povinná pole", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = ExerciseCategory.values().find { it.displayName == categoryName }
                ?: ExerciseCategory.STRENGTH

            // Pro STRENGTH kontrola svalových skupin
            if (category == ExerciseCategory.STRENGTH && selectedMuscleGroups.isEmpty()) {
                Toast.makeText(requireContext(), "Vyberte alespoň jednu svalovou skupinu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newExercise = Exercise(
                id = UUID.randomUUID().toString(),
                name = name,
                category = category,
                description = description,
                mediaType = MediaType.NONE,
                mediaUrl = null,
                muscleGroups = if (category == ExerciseCategory.STRENGTH) selectedMuscleGroups.toList() else emptyList(),
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