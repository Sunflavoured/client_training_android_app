package com.example.client_training_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.databinding.ItemRecycleBinding
import com.example.client_training_app.model.Exercise

class ExerciseAdapter(
    // Používáme 'var', abychom mohli aktualizovat seznam dat
    private var exercises: List<Exercise>,
    // Lambda pro akci po kliknutí na položku
    private val onItemClick: (Exercise) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    // Vnitřní třída ViewHolder používající View Binding
    inner class ExerciseViewHolder(private val binding: ItemRecycleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Metoda pro navázání dat cvičení na UI prvky
        fun bind(exercise: Exercise) {
            binding.tvName.text = exercise.name
            binding.tvMuscleGroups.text = exercise.muscleGroups.joinToString(", ")


            // Nastavení posluchače kliknutí
            binding.root.setOnClickListener {
                onItemClick(exercise)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        // Inflate layoutu pomocí View Bindingu
        val binding = ItemRecycleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        // Navázání dat pro aktuální pozici
        holder.bind(exercises[position])
    }

    override fun getItemCount(): Int = exercises.size

    // Metoda pro aktualizaci seznamu dat
    fun updateExercises(newExercises: List<Exercise>) {
        exercises = newExercises
        // Poznámka: Pro optimalizaci používejte DiffUtil
        notifyDataSetChanged()
    }
}