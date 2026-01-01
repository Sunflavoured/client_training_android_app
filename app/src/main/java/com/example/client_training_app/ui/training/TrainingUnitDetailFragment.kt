package com.example.client_training_app.ui.training

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.R
import com.example.client_training_app.data.repository.TrainingUnitRepository
import com.example.client_training_app.databinding.FragmentTrainingUnitDetailBinding
import com.example.client_training_app.model.toExercise // Import extension funkce
import com.example.client_training_app.ui.adapters.ExerciseAdapter
import kotlinx.coroutines.launch

class TrainingUnitDetailFragment : Fragment(R.layout.fragment_training_unit_detail) {

    private lateinit var binding: FragmentTrainingUnitDetailBinding
    private val args: TrainingUnitDetailFragmentArgs by navArgs()
    private lateinit var repository: TrainingUnitRepository
    private lateinit var adapter: ExerciseAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrainingUnitDetailBinding.bind(view)
        repository = TrainingUnitRepository(requireContext())

        val unitId = args.trainingUnitId

        setupRecyclerView() // 1. Nastavíme prázdný recyclerView
        loadData(unitId)    // 2. Načteme data
        setupButtons(unitId) // 3. Tlačítko pro editaci
    }

    private fun setupRecyclerView() {
        // Inicializujeme adaptér s prázdným seznamem
        // Kliknutí na cvik v detailu zatím nemusí nic dělat (nebo může otevřít detail cviku)
        adapter = ExerciseAdapter(emptyList()) { exercise ->
            // Volitelné: Otevřít detail samotného cviku
        }
        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExercises.adapter = adapter
    }

    private fun loadData(unitId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Získáme data z databáze (Unit + Seznam Entity cviků)
            val unitWithExercises = repository.getTrainingUnitWithExercises(unitId)

            if (unitWithExercises != null) {
                // A) Hlavička (Název, poznámka)
                binding.tvUnitName.text = unitWithExercises.trainingUnit.name
                if (!unitWithExercises.trainingUnit.note.isNullOrEmpty()) {
                    binding.tvUnitNote.text = unitWithExercises.trainingUnit.note
                    binding.tvUnitNote.visibility = View.VISIBLE
                } else {
                    binding.tvUnitNote.visibility = View.GONE
                }

                // B) Seznam cviků
                // POZOR: Musíme převést Entity na Model 'Exercise'
                val exerciseModels = unitWithExercises.exercises.map { entity ->
                    entity.toExercise()
                }

                adapter.updateExercises(exerciseModels)
            }
        }
    }

    private fun setupButtons(unitId: String) {
        // Tlačítko Editovat (tužka) - přepne nás do Editoru
        binding.fabEdit.setOnClickListener {
            val action = TrainingUnitDetailFragmentDirections
                .actionTrainingUnitDetailFragmentToTrainingUnitEditorFragment(
                    clientId = null, // nebo unit.clientId, pokud to máš v datech
                )
            findNavController().navigate(action)
        }
    }
}