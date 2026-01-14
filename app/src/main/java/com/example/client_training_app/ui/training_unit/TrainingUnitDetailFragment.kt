package com.example.client_training_app.ui.training_unit

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
import com.example.client_training_app.model.TemplateExercise
import com.example.client_training_app.model.toExercise // Import extension funkce
import com.example.client_training_app.ui.adapters.TrainingDetailAdapter
import kotlinx.coroutines.launch

class TrainingUnitDetailFragment : Fragment(R.layout.fragment_training_unit_detail) {

    private lateinit var binding: FragmentTrainingUnitDetailBinding
    private val args: TrainingUnitDetailFragmentArgs by navArgs()
    private lateinit var repository: TrainingUnitRepository
    private lateinit var adapter: TrainingDetailAdapter

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
        adapter = TrainingDetailAdapter(emptyList()) { exercise ->
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
                // Hlavička (Název, poznámka)
                binding.tvUnitName.text = unitWithExercises.trainingUnit.name
                if (!unitWithExercises.trainingUnit.note.isNullOrEmpty()) {
                    binding.tvUnitNote.text = unitWithExercises.trainingUnit.note
                    binding.tvUnitNote.visibility = View.VISIBLE
                } else {
                    binding.tvUnitNote.visibility = View.GONE
                }

                // Musíme převést databázový 'detail' na 'TemplateExercise', stejně jako v Editoru.
                val exerciseModels = unitWithExercises?.exercises?.map { detail ->
                    TemplateExercise(
                        exercise = detail.exercise.toExercise(),
                        order = detail.trainingData.orderIndex,
                        sets = detail.trainingData.sets ?: "3",
                        reps = detail.trainingData.reps ?: "10",
                        weight = detail.trainingData.weight,
                        rest = detail.trainingData.rest ?: "90",

                        // Doplníme ostatní pole
                        time = detail.trainingData.time,
                        distance = detail.trainingData.distance,
                        rir = detail.trainingData.rir,
                        //note = detail.trainingData.note,

                        // Flagy
                        isRepsEnabled = detail.trainingData.isRepsEnabled,
                        isWeightEnabled = detail.trainingData.isWeightEnabled,
                        isTimeEnabled = detail.trainingData.isTimeEnabled,
                        isDistanceEnabled = detail.trainingData.isDistanceEnabled,
                        isRirEnabled = detail.trainingData.isRirEnabled,
                        isRestEnabled = detail.trainingData.isRestEnabled
                    )
                }?.sortedBy { it.order }

                adapter.updateExercises(exerciseModels.orEmpty())
            }

        }
    }
        private fun setupButtons(unitId: String) {
            binding.fabEdit.setOnClickListener {
                val action = TrainingUnitDetailFragmentDirections
                    .actionTrainingUnitDetailFragmentToTrainingUnitEditorFragment(
                        clientId = null,
                        trainingUnitIdToEdit = unitId
                    )
                findNavController().navigate(action)
            }
        }
    }


