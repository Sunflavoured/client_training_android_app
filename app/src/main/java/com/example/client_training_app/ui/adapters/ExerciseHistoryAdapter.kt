package com.example.client_training_app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.R
import com.example.client_training_app.model.ExerciseHistoryItem



class ExerciseHistoryAdapter : ListAdapter<ExerciseHistoryItem, ExerciseHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Tady si můžeš vytvořit layout item_exercise_history.xml
        // Pro rychlost použijeme simple_list_item_2 nebo si nadefinuj vlastní
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise_history_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        private val tvStats = itemView.findViewById<TextView>(R.id.tvStats)

        fun bind(item: ExerciseHistoryItem) {
            // 1. Formát data
            val date = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(item.startTime))
            tvDate.text = date

            // 2. Sestavení textu výkonu (Smart String Building)
            val parts = mutableListOf<String>()

            // A) Výkonnostní metriky (Váha, Opakování, Čas, Vzdálenost)
            if (!item.weight.isNullOrEmpty()) parts.add("${item.weight} kg")
            if (!item.reps.isNullOrEmpty()) parts.add("${item.reps} op.")
            if (!item.distance.isNullOrEmpty()) parts.add("${item.distance} km")
            // U času můžeme přidat "s" nebo formátovat minuty, pro začátek stačí "s"
            if (!item.time.isNullOrEmpty()) parts.add("${item.time} s")

            // Spojíme části pomocí " x " (např. "100 kg x 5 op." nebo "60 s")
            val performanceText = parts.joinToString("  x  ")

            // B) Finální text (Série + Výkon + RIR)
            val finalBuilder = StringBuilder()

            // Přidáme číslo série
            finalBuilder.append("Série ${item.setNumber}:  ")

            // Přidáme výkon (pokud nějaký je)
            if (performanceText.isNotEmpty()) {
                finalBuilder.append(performanceText)
            } else {
                finalBuilder.append("-") // Prázdná série
            }

            // Přidáme RIR (pokud byl vyplněn)
            if (!item.rir.isNullOrEmpty()) {
                finalBuilder.append("  (RIR ${item.rir})")
            }

            tvStats.text = finalBuilder.toString()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExerciseHistoryItem>() {
        override fun areItemsTheSame(oldItem: ExerciseHistoryItem, newItem: ExerciseHistoryItem) =
            oldItem.sessionId == newItem.sessionId && oldItem.setNumber == newItem.setNumber
        override fun areContentsTheSame(oldItem: ExerciseHistoryItem, newItem: ExerciseHistoryItem) = oldItem == newItem
    }
}