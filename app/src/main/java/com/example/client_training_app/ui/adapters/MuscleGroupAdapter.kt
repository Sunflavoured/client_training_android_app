package com.example.client_training_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.databinding.ItemMuscleGroupBinding

class MuscleGroupAdapter(
    private val allGroups: List<String>,
    // Použijeme Set pro rychlé ověření, jestli je položka vybraná
    private val selectedGroups: MutableSet<String>
) : RecyclerView.Adapter<MuscleGroupAdapter.MuscleViewHolder>() {

    inner class MuscleViewHolder(val binding: ItemMuscleGroupBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(muscleName: String) {
            binding.cbMuscle.text = muscleName
            // Nastaví checkbox na zaškrtnutý, pokud je v setu
            binding.cbMuscle.isChecked = selectedGroups.contains(muscleName)

            // Při změně stavu checkboxu aktualizujeme náš set
            binding.cbMuscle.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedGroups.add(muscleName)
                } else {
                    selectedGroups.remove(muscleName)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MuscleViewHolder {
        val binding = ItemMuscleGroupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MuscleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MuscleViewHolder, position: Int) {
        holder.bind(allGroups[position])
    }

    override fun getItemCount() = allGroups.size

    // Funkce, kterou zavoláme pro získání finálního výběru
    fun getSelectedGroups(): List<String> {
        return selectedGroups.toList()
    }
}