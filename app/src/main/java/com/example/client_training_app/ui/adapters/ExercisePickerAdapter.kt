package com.example.client_training_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.databinding.ItemExercisePickerBinding // Nový Binding
import com.example.client_training_app.model.Exercise

class ExercisePickerAdapter(
    private var exercises: List<Exercise> = emptyList(),
    private val onExerciseClicked: (Exercise) -> Unit
) : RecyclerView.Adapter<ExercisePickerAdapter.ExercisePickerViewHolder>() {

    inner class ExercisePickerViewHolder(private val binding: ItemExercisePickerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: Exercise) {
            binding.tvExerciseName.text = exercise.name
            binding.tvExerciseMuscleGroups.text = exercise.muscleGroups.joinToString(", ")

            binding.root.setOnClickListener {
                onExerciseClicked(exercise)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExercisePickerViewHolder {
        val binding = ItemExercisePickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExercisePickerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExercisePickerViewHolder, position: Int) {
        holder.bind(exercises[position])
    }

    override fun getItemCount(): Int = exercises.size

    fun updateExercises(newExercises: List<Exercise>) {
        exercises = newExercises
        notifyDataSetChanged()
    }
}