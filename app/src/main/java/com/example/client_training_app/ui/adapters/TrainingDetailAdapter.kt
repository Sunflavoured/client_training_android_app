package com.example.client_training_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.databinding.ItemRecycleBinding
import com.example.client_training_app.model.TemplateExercise

class TrainingDetailAdapter(
    // ZMĚNA: Místo List<Exercise> přijímáme List<TemplateExercise>
    private var items: List<TemplateExercise>,
    private val onItemClick: (TemplateExercise) -> Unit
) : RecyclerView.Adapter<TrainingDetailAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemRecycleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TemplateExercise) {
            // 1. Název cviku (z vnořeného objektu exercise)
            binding.tvName.text = item.exercise.name


            // 2. Sestavení detailního popisu (Série, Opakování, Váha...)
            // Místo svalových skupin teď zobrazíme plán tréninku
            val detailsBuilder = StringBuilder()

            // Série a Opakování (např. "4 x 10")
            if (item.sets.isNotEmpty()) {
                detailsBuilder.append("${item.sets} série")
            }

            // Logika pro zobrazení opakování nebo času
            if (item.isTimeEnabled && !item.time.isNullOrEmpty()) {
                detailsBuilder.append(" x ${item.time}s")
            } else if (item.reps?.isNotEmpty() == true) {
                detailsBuilder.append(" x ${item.reps} op.")
            }

            // Váha
            if (item.isWeightEnabled && !item.weight.isNullOrEmpty()) {
                detailsBuilder.append(" | ${item.weight} kg")
            }

            // Vzdálenost
            if (item.isDistanceEnabled && !item.distance.isNullOrEmpty()) {
                detailsBuilder.append(" | Vzdálenost: ${item.distance}")
            }

            // Pauza (Rest)
            if (item.isRestEnabled && !item.rest.isNullOrEmpty()) {
                detailsBuilder.append(" | Pauza: ${item.rest}s")
            }

            // RIR
            if (item.isRirEnabled && !item.rir.isNullOrEmpty()) {
                detailsBuilder.append(" | RIR: ${item.rir}")
            }

            /* Poznámka (pokud existuje, dáme ji na nový řádek) TODO - přidání poznámky k exercise template
            if (!item.description.isNullOrEmpty()) {
                detailsBuilder.append("\nPozn: ${item.note}")
            }*/

            // Výsledek vložíme do druhého TextView (původně tvMuscleGroups)
            binding.tvMuscleGroups.text = detailsBuilder.toString()

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layoutu pomocí View Bindingu
        val binding = ItemRecycleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    // Metoda pro aktualizaci seznamu dat
    fun updateExercises(newItems: List<TemplateExercise>) {
        items = newItems
        notifyDataSetChanged()
    }
}