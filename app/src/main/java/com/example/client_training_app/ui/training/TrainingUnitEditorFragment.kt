package com.example.client_training_app.ui.training

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.client_training_app.R
import com.example.client_training_app.databinding.FragmentTrainingUnitEditorBinding
import androidx.navigation.fragment.findNavController
import com.example.client_training_app.model.Exercise

class TrainingUnitEditorFragment : Fragment(R.layout.fragment_training_unit_editor) {

    // Argumenty (získáme clientId, pokud existuje)
    // Pokud ti to svítí červeně, udělej Rebuild Project po úpravě nav_graph.xml
    private val args: TrainingUnitEditorFragmentArgs by navArgs()

    private lateinit var binding: FragmentTrainingUnitEditorBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrainingUnitEditorBinding.bind(view)

        setupListeners()
        setupExerciseResultListener()

        // Zde budeme inicializovat RecyclerView pro cviky
    }

    private fun setupListeners() {
        binding.btnAddExercise.setOnClickListener { // Používáme nové ID btnAddExercise
            // Navigujeme na Picker
            findNavController().navigate(R.id.action_trainingUnitEditorFragment_to_exercisePickerFragment)
        }

        binding.btnSaveUnit.setOnClickListener {
            // TODO: Uložit celou jednotku do DB
        }
    }

    private fun setupExerciseResultListener() {
        // Získáme handle (rukojeť) navigačního grafu
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle

        // Posloucháme, jestli dorazil výsledek s klíčem "selected_exercise"
        savedStateHandle?.getLiveData<Exercise>("selected_exercise")?.observe(viewLifecycleOwner) { exercise ->
            // Jakmile cvik dorazí, přidáme ho do seznamu pro editaci
            addExerciseToUnitEditor(exercise)
            // Po použití je dobré data z handle odstranit
            savedStateHandle.remove<Exercise>("selected_exercise")
        }
    }
    private fun addExerciseToUnitEditor(exercise: Exercise) {
        // TODO: Zde budeme cvik přidávat do našeho RecyclerView Adapteru
        Toast.makeText(requireContext(), "Cvik ${exercise.name} připraven k editaci!", Toast.LENGTH_SHORT).show()
        // V dalším kroku: zde vytvoříme TemplateExerciseEntity a přidáme ji do adapteru
    }
}