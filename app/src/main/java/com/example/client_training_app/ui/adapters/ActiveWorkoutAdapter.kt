package com.example.client_training_app.ui.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible // Důležité pro snadné .isVisible = true/false
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.R
import com.example.client_training_app.databinding.ItemActiveExerciseBinding
import com.example.client_training_app.model.ActiveExerciseUi
import com.example.client_training_app.model.ActiveSetUi

class ActiveWorkoutAdapter(
    private var exercises: List<ActiveExerciseUi>,
    private val onAddSetClicked: (exerciseIndex: Int) -> Unit
) : RecyclerView.Adapter<ActiveWorkoutAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemActiveExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: ActiveExerciseUi, exerciseIndex: Int) {
            binding.tvExerciseName.text = exercise.exerciseName
            binding.tvTarget.text = "Cíl: ${exercise.targetNote ?: "Bez poznámky"}"

            // 1. NASTAVENÍ VIDITELNOSTI HLAVIČEK (Nadpisy sloupců)
            // Používáme findViewById, protože v bindingu cardview nemusí být ID vidět přímo, pokud nejsou v data classu
            // Ale pokud používáš ViewBinding, měly by být dostupné:

            // Poznámka: Aby toto fungovalo, musíš přidat ID do item_active_exercise.xml (hWeight, hReps...)
            // Pokud ViewBinding hlásí chybu, zkus binding.root.findViewById<TextView>(R.id.hWeight)

            val hWeight = binding.root.findViewById<TextView>(R.id.hWeight)
            val hReps = binding.root.findViewById<TextView>(R.id.hReps)
            val hTime = binding.root.findViewById<TextView>(R.id.hTime)
            val hDistance = binding.root.findViewById<TextView>(R.id.hDistance)
            val hRir = binding.root.findViewById<TextView>(R.id.hRir)

            hWeight.isVisible = exercise.isWeightEnabled
            hReps.isVisible = exercise.isRepsEnabled
            hTime.isVisible = exercise.isTimeEnabled
            hDistance.isVisible = exercise.isDistanceEnabled
            hRir.isVisible = exercise.isRirEnabled

            // 2. Vykreslení řádků
            binding.llSetsContainer.removeAllViews()
            exercise.sets.forEach { setUi ->
                addSetRow(setUi, exercise) // Předáváme i exercise, abychom znali konfiguraci
            }

            binding.btnAddSet.setOnClickListener {
                onAddSetClicked(exerciseIndex)
            }
        }

        private fun addSetRow(setUi: ActiveSetUi, config: ActiveExerciseUi) {
            val inflater = LayoutInflater.from(binding.root.context)
            val setView = inflater.inflate(R.layout.item_active_set, binding.llSetsContainer, false)

            val tvSetNumber = setView.findViewById<TextView>(R.id.tvSetNumber)
            val etWeight = setView.findViewById<EditText>(R.id.etWeight)
            val etReps = setView.findViewById<EditText>(R.id.etReps)
            val etTime = setView.findViewById<EditText>(R.id.etTime)     // Nové
            val etDistance = setView.findViewById<EditText>(R.id.etDistance) // Nové
            val etRir = setView.findViewById<EditText>(R.id.etRir)
            val cbCompleted = setView.findViewById<CheckBox>(R.id.cbCompleted)

            // A) Nastavení VIDITELNOSTI INPUTŮ podle konfigurace cviku
            etWeight.isVisible = config.isWeightEnabled
            etReps.isVisible = config.isRepsEnabled
            etTime.isVisible = config.isTimeEnabled
            etDistance.isVisible = config.isDistanceEnabled
            etRir.isVisible = config.isRirEnabled

            // B) Vyplnění hodnot
            tvSetNumber.text = setUi.setNumber.toString()
            etWeight.setText(setUi.weight)
            etReps.setText(setUi.reps)
            etTime.setText(setUi.time)         // Nové
            etDistance.setText(setUi.distance) // Nové
            etRir.setText(setUi.rir)
            cbCompleted.isChecked = setUi.isCompleted

            // C) Ukládání hodnot (TextWatchers)
            if (config.isWeightEnabled) etWeight.addSimpleTextWatcher { setUi.weight = it }
            if (config.isRepsEnabled) etReps.addSimpleTextWatcher { setUi.reps = it }
            if (config.isTimeEnabled) etTime.addSimpleTextWatcher { setUi.time = it }         // Nové
            if (config.isDistanceEnabled) etDistance.addSimpleTextWatcher { setUi.distance = it } // Nové
            if (config.isRirEnabled) etRir.addSimpleTextWatcher { setUi.rir = it }

            // Barvičky a Checkbox
            cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                setUi.isCompleted = isChecked
                if (isChecked) setView.setBackgroundColor(0x224CAF50)
                else setView.setBackgroundColor(0x00000000)
            }
            if (setUi.isCompleted) setView.setBackgroundColor(0x224CAF50)

            binding.llSetsContainer.addView(setView)
        }
    }

    private fun EditText.addSimpleTextWatcher(afterChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { afterChanged(s.toString()) }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveExerciseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(exercises[position], position)
    }

    override fun getItemCount(): Int = exercises.size

    fun updateData(newData: List<ActiveExerciseUi>) {
        exercises = newData
        notifyDataSetChanged()
    }
}