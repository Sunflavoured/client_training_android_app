package com.example.client_training_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.data.entity.TrainingSessionEntity
import com.example.client_training_app.databinding.ItemTrainingSessionBinding

class TrainingSessionAdapter(
    private val onItemClick: (TrainingSessionEntity) -> Unit
) : ListAdapter<TrainingSessionEntity, TrainingSessionAdapter.TrainingViewHolder>(DiffCallback) {

    inner class TrainingViewHolder(private val binding: ItemTrainingSessionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(session: TrainingSessionEntity) {
            binding.tvTrainingName.text = session.name
            binding.cbCompleted.isChecked = session.isCompleted

            if (session.isCompleted) {
                binding.tvTrainingStatus.text = "Dokončeno"
                // Můžeme změnit barvu textu nebo styl
            } else {
                binding.tvTrainingStatus.text = "Naplánováno"
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
    companion object DiffCallback : DiffUtil.ItemCallback<TrainingSessionEntity>() {
        override fun areItemsTheSame(oldItem: TrainingSessionEntity, newItem: TrainingSessionEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TrainingSessionEntity, newItem: TrainingSessionEntity): Boolean {
            return oldItem == newItem
        }
    }
}