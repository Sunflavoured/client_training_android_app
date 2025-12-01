package com.example.client_training_app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.R
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.databinding.ItemTrainingUnitBinding

// Zde používáme ListAdapter, který je efektivní s Flow
class TrainingUnitAdapter(
    private val onUnitClicked: (TrainingUnitEntity) -> Unit
) : ListAdapter<TrainingUnitEntity, TrainingUnitAdapter.UnitViewHolder>(UnitDiffCallback) {

    inner class UnitViewHolder(private val binding: ItemTrainingUnitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(unit: TrainingUnitEntity) {
            binding.tvUnitName.text = unit.name

            // Nastavení statusu (Globální vs. Klientský)
            if (unit.clientId == null) {
                binding.tvUnitStatus.text = "Globální šablona"
            } else {
                // Zde bys mohla volat databázi pro jméno klienta, ale pro zjednodušení dáme "Klientská"
                binding.tvUnitStatus.text = "Klientská šablona"
                // Nebo: resources.getString(R.string.client_template, unit.clientId)
            }

            // TODO: Získat počet cviků (vyžaduje složitější dotaz v DB, prozatím prázdné)
            binding.tvExerciseCount.visibility = View.GONE

            binding.root.setOnClickListener {
                onUnitClicked(unit)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val binding = ItemTrainingUnitBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UnitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object UnitDiffCallback : DiffUtil.ItemCallback<TrainingUnitEntity>() {
        override fun areItemsTheSame(oldItem: TrainingUnitEntity, newItem: TrainingUnitEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TrainingUnitEntity, newItem: TrainingUnitEntity): Boolean {
            // Kontrolujeme, jestli jsou stejné, aby se zbytečně nepřekreslovaly
            return oldItem == newItem
        }
    }
}