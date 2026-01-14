package com.example.client_training_app.ui.training_unit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.client_training_app.databinding.BottomSheetExerciseSettingsBinding
import com.example.client_training_app.model.TemplateExercise
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ExerciseSettingsBottomSheet(
    private val currentSettings: TemplateExercise,
    private val onSettingsChanged: (TemplateExercise) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetExerciseSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetExerciseSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Načíst aktuální stav
        binding.cbReps.isChecked = currentSettings.isRepsEnabled
        binding.cbWeight.isChecked = currentSettings.isWeightEnabled
        binding.cbTime.isChecked = currentSettings.isTimeEnabled
        binding.cbDistance.isChecked = currentSettings.isDistanceEnabled
        binding.cbRir.isChecked = currentSettings.isRirEnabled
        binding.cbRest.isChecked = currentSettings.isRestEnabled
        binding.tvTitle.text = "Nastavení: ${currentSettings.exercise.name}"

        // 2. Uložit změny
        binding.btnApply.setOnClickListener {
            val updated = currentSettings.copy(
                isRepsEnabled = binding.cbReps.isChecked,
                isWeightEnabled = binding.cbWeight.isChecked,
                isTimeEnabled = binding.cbTime.isChecked,
                isDistanceEnabled = binding.cbDistance.isChecked,
                isRirEnabled = binding.cbRir.isChecked,
                isRestEnabled = binding.cbRest.isChecked
            )
            onSettingsChanged(updated)
            dismiss()
        }
    }
}