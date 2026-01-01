package com.example.client_training_app.ui.training

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // ✅ Inicializace ViewModelu
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager // ✅ Nutné pro RecyclerView
import com.example.client_training_app.R
import com.example.client_training_app.databinding.FragmentTrainingUnitEditorBinding
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.ui.adapters.UnitExerciseEditorAdapter // ✅ Náš nový adapter
import com.example.client_training_app.ui.training.TrainingUnitEditorViewModel // ✅ Náš nový ViewModel

class TrainingUnitEditorFragment : Fragment(R.layout.fragment_training_unit_editor) {

    private val args: TrainingUnitEditorFragmentArgs by navArgs()

    // 1. Inicializace ViewModelu (drží data o tréninku)
    private val viewModel: TrainingUnitEditorViewModel by viewModels()

    private lateinit var binding: FragmentTrainingUnitEditorBinding

    // 2. Definice Adapteru
    private lateinit var adapter: UnitExerciseEditorAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrainingUnitEditorBinding.bind(view)

        // Inicializace komponent
        setupRecyclerView()
        setupListeners()
        setupExerciseResultListener()

        // Sledování dat z ViewModelu
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = UnitExerciseEditorAdapter(
            onDataChanged = { updatedItem ->
                // Když uživatel změní text v políčku
                viewModel.updateTemplateExercise(updatedItem)
            },
            onDeleteClicked = { itemToDelete ->
                // Když klikne na koš
                viewModel.deleteTemplateExercise(itemToDelete)
            },
            // 🔥 TOTO CHYBĚLO: Co dělat při kliknutí na nastavení (3 tečky)
            onSettingsClicked = { itemToEdit ->
                // Otevřeme BottomSheet dialog
                val dialog = com.example.client_training_app.ui.training.ExerciseSettingsBottomSheet(
                    currentSettings = itemToEdit,
                    onSettingsChanged = { updatedSettings ->
                        // Když uživatel v dialogu klikne na "Použít", aktualizujeme ViewModel
                        viewModel.updateTemplateExercise(updatedSettings)
                    }
                )
                dialog.show(parentFragmentManager, "ExerciseSettingsBottomSheet")
            }
        )

        binding.rvAddedExercises.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TrainingUnitEditorFragment.adapter
            // Optimalizace pro RecyclerView, pokud se nemění jeho velikost
            setHasFixedSize(true)
            // Vypneme animace při změně (aby neblikaly inputy při psaní)
            (itemAnimator as? androidx.recyclerview.widget.SimpleItemAnimator)?.supportsChangeAnimations = false
        }
    }

    private fun observeViewModel() {
        // Jakmile se ve ViewModelu změní seznam cviků, adapter se aktualizuje
        viewModel.templateExercises.observe(viewLifecycleOwner) { exercises ->
            adapter.submitList(exercises.toList()) // .toList() vytváří kopii pro správné fungování DiffUtil
        }
    }

    private fun setupListeners() {
        binding.btnAddExercise.setOnClickListener {
            findNavController().navigate(R.id.action_trainingUnitEditorFragment_to_exercisePickerFragment)
        }

        binding.btnSaveUnit.setOnClickListener {
            val name = binding.etUnitName.text.toString()
            val note = binding.etUnitNote.text.toString()

            // Získáme clientId z argumentů (pokud je null, je to globální šablona)
            // args.clientId je definované v nav_graph
            val clientId = args.clientId

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Zadejte název tréninku", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Voláme uložení
            viewModel.saveTrainingUnit(name, note, clientId) {
                // onSuccess Lambda: Co se stane po uložení?
                Toast.makeText(requireContext(), "Trénink uložen!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Vrátíme se zpět (do Knihovny)
            }
        }
    }

    private fun setupExerciseResultListener() {
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle

        savedStateHandle?.getLiveData<Exercise>("selected_exercise")?.observe(viewLifecycleOwner) { exercise ->
            viewModel.addExercise(exercise)

            savedStateHandle.remove<Exercise>("selected_exercise")
        }
    }
}