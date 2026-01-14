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
            binding.tvTarget.text = "${exercise.targetNote ?: "Bez poznámky"}"

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
            exercise.sets.forEachIndexed { index, setUi ->
                // Zjistíme, jestli existuje předchozí série
                val previousSet = if (index > 0) exercise.sets[index - 1] else null

                // Předáme previousSet do metody addSetRow
                addSetRow(setUi, exercise, previousSet)
            }

            binding.btnAddSet.setOnClickListener {
                onAddSetClicked(exerciseIndex)
            }
        }

        private fun addSetRow(
            setUi: ActiveSetUi,
            config: ActiveExerciseUi,
            previousSet: ActiveSetUi?
        ) {
            val inflater = LayoutInflater.from(binding.root.context)
            val setView = inflater.inflate(R.layout.item_active_set, binding.llSetsContainer, false)

            // Views
            val btnCopy = setView.findViewById<View>(R.id.btnCopyPrevious) // <--- NOVÉ
            val tvSetNumber = setView.findViewById<TextView>(R.id.tvSetNumber)
            val etWeight = setView.findViewById<EditText>(R.id.etWeight)
            val etReps = setView.findViewById<EditText>(R.id.etReps)
            val etTime = setView.findViewById<EditText>(R.id.etTime)
            val etDistance = setView.findViewById<EditText>(R.id.etDistance)
            val etRir = setView.findViewById<EditText>(R.id.etRir)


            // --- 1. LOGIKA KOPÍROVACÍHO TLAČÍTKA ---
            if (previousSet != null) {
                btnCopy.visibility = View.VISIBLE
                btnCopy.setOnClickListener {
                    // A) Přepsat data v UI (EditTexty)
                    // Zkopírujeme jen to, co je pro daný cvik povolené
                    if (config.isWeightEnabled) etWeight.setText(previousSet.weight)
                    if (config.isRepsEnabled) etReps.setText(previousSet.reps)
                    if (config.isTimeEnabled) etTime.setText(previousSet.time)
                    if (config.isDistanceEnabled) etDistance.setText(previousSet.distance)
                    // RIR obvykle nekopírujeme, protože se mění s únavou, ale můžeš:
                    // if (config.isRirEnabled) etRir.setText(previousSet.rir)

                    // B) Přepsat data v Modelu (ActiveSetUi)
                    // TextWatcher by to měl chytit, ale pro jistotu to nastavíme i přímo,
                    // aby to bylo atomické a rychlé.
                    setUi.weight = previousSet.weight
                    setUi.reps = previousSet.reps
                    setUi.time = previousSet.time
                    setUi.distance = previousSet.distance
                    // setUi.rir = previousSet.rir

                    // Volitelné: Zobrazit Toast nebo malou animaci
                    // Toast.makeText(setView.context, "Zkopírováno", Toast.LENGTH_SHORT).show()
                }
            } else {
                // První série nemá z čeho kopírovat
                btnCopy.visibility = View.INVISIBLE
            }

            // --- 2. ZBYTEK PŮVODNÍHO KÓDU ---
            // A) Viditelnost inputů
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

            // C) Ukládání hodnot (TextWatchers)
            if (config.isWeightEnabled) etWeight.addSimpleTextWatcher { setUi.weight = it }
            if (config.isRepsEnabled) etReps.addSimpleTextWatcher { setUi.reps = it }
            if (config.isTimeEnabled) etTime.addSimpleTextWatcher { setUi.time = it }         // Nové
            if (config.isDistanceEnabled) etDistance.addSimpleTextWatcher { setUi.distance = it } // Nové
            if (config.isRirEnabled) etRir.addSimpleTextWatcher { setUi.rir = it }

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