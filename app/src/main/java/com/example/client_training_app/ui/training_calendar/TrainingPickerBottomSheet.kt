package com.example.client_training_app.ui.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.databinding.BottomSheetTrainingPickerBinding
import com.example.client_training_app.ui.adapters.TrainingUnitAdapter // Používáme tvůj existující adaptér
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TrainingPickerBottomSheet(
    private val availableUnits: List<TrainingUnitEntity>,
    private val onUnitSelected: (TrainingUnitEntity) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetTrainingPickerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetTrainingPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nastavíme RecyclerView
        binding.rvPickerTrainings.layoutManager = LinearLayoutManager(requireContext())

        // Použijeme tvůj TrainingUnitAdapter
        // Pozor: Adapter v konstruktoru očekává lambdu (onItemClick)
        val adapter = TrainingUnitAdapter { selectedUnit ->
            // Když uživatel klikne na trénink v seznamu:
            onUnitSelected(selectedUnit) // 1. Pošleme výběr zpět
            dismiss() // 2. Zavřeme dialog
        }

        binding.rvPickerTrainings.adapter = adapter
        adapter.submitList(availableUnits)
    }
}