package com.example.client_training_app.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.data.database.ScheduledWorkoutDetail
import com.example.client_training_app.databinding.ItemScheduledWorkoutBinding

class ScheduledWorkoutAdapter(
    private val onItemClick: (ScheduledWorkoutDetail) -> Unit
) : ListAdapter<ScheduledWorkoutDetail, ScheduledWorkoutAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScheduledWorkoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemScheduledWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScheduledWorkoutDetail) {
            // Název tréninku bereme z propojené tabulky trainingUnit
            binding.tvWorkoutName.text = item.trainingUnit.name

            // Změna barvy nebo ikony podle toho, jestli je hotovo
            if (item.schedule.isCompleted) {
                binding.ivStatus.setColorFilter(Color.parseColor("#4CAF50")) // Zelená
                // Zde můžeš změnit i ikonu na fajfku
            } else {
                binding.ivStatus.setColorFilter(Color.GRAY) // Šedá
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ScheduledWorkoutDetail>() {
        override fun areItemsTheSame(oldItem: ScheduledWorkoutDetail, newItem: ScheduledWorkoutDetail): Boolean {
            return oldItem.schedule.id == newItem.schedule.id
        }

        override fun areContentsTheSame(oldItem: ScheduledWorkoutDetail, newItem: ScheduledWorkoutDetail): Boolean {
            return oldItem == newItem
        }
    }
}