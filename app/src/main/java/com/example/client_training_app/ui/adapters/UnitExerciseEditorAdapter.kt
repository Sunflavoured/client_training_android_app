package com.example.client_training_app.ui.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.databinding.ItemUnitExerciseEditorBinding
import com.example.client_training_app.model.TemplateExercise

class UnitExerciseEditorAdapter(
    private val onDataChanged: (TemplateExercise) -> Unit,
    private val onDeleteClicked: (TemplateExercise) -> Unit,
    private val onSettingsClicked: (TemplateExercise) -> Unit
) : ListAdapter<TemplateExercise, UnitExerciseEditorAdapter.ExerciseViewHolder>(TemplateExerciseDiffCallback) {

    inner class ExerciseViewHolder(private val binding: ItemUnitExerciseEditorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Watchers
        private var setsTextWatcher: TextWatcher? = null
        private var repsTextWatcher: TextWatcher? = null
        private var weightTextWatcher: TextWatcher? = null
        private var timeTextWatcher: TextWatcher? = null
        private var distanceTextWatcher: TextWatcher? = null
        private var rirTextWatcher: TextWatcher? = null
        private var restTextWatcher: TextWatcher? = null

        fun bind(templateExercise: TemplateExercise) {
            binding.tvExerciseName.text = templateExercise.exercise.name

            // 1. Odebereme staré Listenery
            clearTextWatchers()

            // 2. Nastavíme VIDITELNOST (Upravená ID kontejnerů z nového XML)
            binding.containerReps.visibility = if (templateExercise.isRepsEnabled) View.VISIBLE else View.GONE
            binding.containerWeight.visibility = if (templateExercise.isWeightEnabled) View.VISIBLE else View.GONE
            binding.containerTime.visibility = if (templateExercise.isTimeEnabled) View.VISIBLE else View.GONE
            binding.containerDistance.visibility = if (templateExercise.isDistanceEnabled) View.VISIBLE else View.GONE
            binding.containerRir.visibility = if (templateExercise.isRirEnabled) View.VISIBLE else View.GONE
            binding.containerRest.visibility = if (templateExercise.isRestEnabled) View.VISIBLE else View.GONE

            // 3. Nastavíme HODNOTY - 🔥 OPRAVA SKÁKAJÍCÍHO KURZORU 🔥
            // Funkce setTextSilent zkontroluje, jestli se text liší, než ho přepíše
            setTextSilent(binding.etSets, templateExercise.sets.orEmpty())
            setTextSilent(binding.etReps, templateExercise.reps.orEmpty())
            setTextSilent(binding.etWeight, templateExercise.weight.orEmpty())
            setTextSilent(binding.etTime, templateExercise.time.orEmpty())
            setTextSilent(binding.etDistance, templateExercise.distance.orEmpty())
            setTextSilent(binding.etRir, templateExercise.rir.orEmpty())
            setTextSilent(binding.etRest, templateExercise.rest.orEmpty())

            // 4. Listenery pro tlačítka
            binding.ivDeleteExercise.setOnClickListener { onDeleteClicked(templateExercise) }
            binding.ivSettings.setOnClickListener { onSettingsClicked(templateExercise) }

            // 5. TextWatchers (Logika beze změny)
            setsTextWatcher = createWatcher { s -> if (s != templateExercise.sets) onDataChanged(templateExercise.copy(sets = s)) }
            binding.etSets.addTextChangedListener(setsTextWatcher)

            repsTextWatcher = createWatcher { s -> if (s != templateExercise.reps) onDataChanged(templateExercise.copy(reps = s)) }
            binding.etReps.addTextChangedListener(repsTextWatcher)

            weightTextWatcher = createWatcher { s ->
                val value = s.takeIf { it.isNotBlank() }
                if (value != templateExercise.weight) onDataChanged(templateExercise.copy(weight = value))
            }
            binding.etWeight.addTextChangedListener(weightTextWatcher)

            timeTextWatcher = createWatcher { s ->
                val value = s.takeIf { it.isNotBlank() }
                if (value != templateExercise.time) onDataChanged(templateExercise.copy(time = value))
            }
            binding.etTime.addTextChangedListener(timeTextWatcher)

            distanceTextWatcher = createWatcher { s ->
                val value = s.takeIf { it.isNotBlank() }
                if (value != templateExercise.distance) onDataChanged(templateExercise.copy(distance = value))
            }
            binding.etDistance.addTextChangedListener(distanceTextWatcher)

            rirTextWatcher = createWatcher { s ->
                val value = s.takeIf { it.isNotBlank() }
                if (value != templateExercise.rir) onDataChanged(templateExercise.copy(rir = value))
            }
            binding.etRir.addTextChangedListener(rirTextWatcher)

            restTextWatcher = createWatcher { s ->
                val value = s.takeIf { it.isNotBlank() }
                if (value != templateExercise.rest) onDataChanged(templateExercise.copy(rest = value))
            }
            binding.etRest.addTextChangedListener(restTextWatcher)
        }

        private fun clearTextWatchers() {
            setsTextWatcher?.let { binding.etSets.removeTextChangedListener(it) }
            repsTextWatcher?.let { binding.etReps.removeTextChangedListener(it) }
            weightTextWatcher?.let { binding.etWeight.removeTextChangedListener(it) }
            timeTextWatcher?.let { binding.etTime.removeTextChangedListener(it) }
            distanceTextWatcher?.let { binding.etDistance.removeTextChangedListener(it) }
            rirTextWatcher?.let { binding.etRir.removeTextChangedListener(it) }
            restTextWatcher?.let { binding.etRest.removeTextChangedListener(it) }
        }

        // Pomocná funkce pro zkrácení kódu
        private fun createWatcher(onChanged: (String) -> Unit): TextWatcher {
            return object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    onChanged(s.toString())
                }
            }
        }
        // 🔥 POMOCNÁ FUNKCE PROTI SKÁKÁNÍ KURZORU 🔥
        private fun setTextSilent(editText: android.widget.EditText, value: String) {
            if (editText.text.toString() != value) {
                editText.setText(value)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemUnitExerciseEditorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object TemplateExerciseDiffCallback : DiffUtil.ItemCallback<TemplateExercise>() {
        override fun areItemsTheSame(oldItem: TemplateExercise, newItem: TemplateExercise) = oldItem.exercise.id == newItem.exercise.id
        override fun areContentsTheSame(oldItem: TemplateExercise, newItem: TemplateExercise) = oldItem == newItem
    }
}