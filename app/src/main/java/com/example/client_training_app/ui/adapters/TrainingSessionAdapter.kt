package com.example.client_training_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.data.entity.WorkoutSessionEntity
import com.example.client_training_app.databinding.ItemTrainingSessionBinding

class TrainingSessionAdapter(
    private val onItemClick: (WorkoutSessionEntity) -> Unit
) : ListAdapter<WorkoutSessionEntity, TrainingSessionAdapter.TrainingViewHolder>(DiffCallback) {

    inner class TrainingViewHolder(private val binding: ItemTrainingSessionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(session: WorkoutSessionEntity) {
            binding.tvTrainingName.text = session.trainingName
            val isFinished = session.endTime != null
            binding.cbCompleted.isChecked = isFinished

            if (isFinished) {
                binding.tvTrainingStatus.text = "Dokončeno"
                binding.tvTrainingStatus.setTextColor(android.graphics.Color.GREEN) // Volitelné
            } else {
                binding.tvTrainingStatus.text = "Probíhá" // Nebo "Nedokončeno"
                binding.tvTrainingStatus.setTextColor(android.graphics.Color.RED)
            }

            binding.root.setOnClickListener {
                onItemClick(session)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainingViewHolder {
        val binding = ItemTrainingSessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TrainingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrainingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Objekt pro porovnávání změn v seznamu (pro animace)
    companion object DiffCallback : DiffUtil.ItemCallback<WorkoutSessionEntity>() {
        override fun areItemsTheSame(oldItem: WorkoutSessionEntity, newItem: WorkoutSessionEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WorkoutSessionEntity, newItem: WorkoutSessionEntity): Boolean {
            return oldItem == newItem
        }
    }
}