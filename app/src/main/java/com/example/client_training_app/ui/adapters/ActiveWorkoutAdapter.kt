package com.example.client_training_app.ui.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu // Pro kontextové menu
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.R
import com.example.client_training_app.databinding.ItemActiveExerciseBinding
import com.example.client_training_app.model.ActiveExerciseUi
import com.example.client_training_app.model.ActiveSetUi

class ActiveWorkoutAdapter(
    private var exercises: List<ActiveExerciseUi>,
    private val onAddSetClicked: (exerciseIndex: Int) -> Unit,
    private val onDragStart: (RecyclerView.ViewHolder) -> Unit,
    private val onSubstituteClicked: (exerciseIndex: Int) -> Unit
) : RecyclerView.Adapter<ActiveWorkoutAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemActiveExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: ActiveExerciseUi, exerciseIndex: Int) {
            binding.tvExerciseName.text = exercise.exerciseName
            binding.tvTarget.text = "${exercise.targetNote ?: "Bez poznámky"}"

            // --- 1. DRAG HANDLE LOGIKA ---
            // Binding by měl mít přístup přímo k ivDragHandle, pokud je v XML
            binding.ivDragHandle.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    onDragStart(this) // Řekneme Fragmentu: "Začni přesunovat tento řádek"
                }
                false
            }

            // --- 2. MENU LOGIKA (Substitute) ---
            binding.ivMenu.setOnClickListener { view ->
                showPopupMenu(view, exerciseIndex)
            }

            // --- 3. NASTAVENÍ VIDITELNOSTI HLAVIČEK ---
            // ViewBinding negarantuje, že najde ID uvnitř include/merged layoutů nebo pokud jsou ID stejná.
            // Pro jistotu použijeme binding.root.findViewById, nebo binding.hWeight pokud je vidí.

            // Poznámka: Pokud máš v ItemActiveExerciseBinding ID jako hWeight, použij binding.hWeight
            // Pokud je nemůže najít, použij findViewById:
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

            // --- 4. VYKRESLENÍ ŘÁDKŮ SÉRIÍ ---
            binding.llSetsContainer.removeAllViews()
            exercise.sets.forEachIndexed { index, setUi ->
                val previousSet = if (index > 0) exercise.sets[index - 1] else null
                addSetRow(setUi, exercise, previousSet)
            }

            binding.btnAddSet.setOnClickListener {
                onAddSetClicked(exerciseIndex)
            }
        }

        private fun showPopupMenu(view: View, index: Int) {
            val popup = PopupMenu(view.context, view)
            popup.menu.add("Nahradit cvik (Substitute)")
            // popup.menu.add("Smazat cvik") // Můžeš přidat později

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Nahradit cvik (Substitute)" -> {
                        onSubstituteClicked(index)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        private fun addSetRow(
            setUi: ActiveSetUi,
            config: ActiveExerciseUi,
            previousSet: ActiveSetUi?
        ) {
            val inflater = LayoutInflater.from(binding.root.context)
            val setView = inflater.inflate(R.layout.item_active_set, binding.llSetsContainer, false)

            // Views
            val btnCopy = setView.findViewById<View>(R.id.btnCopyPrevious)
            val tvSetNumber = setView.findViewById<TextView>(R.id.tvSetNumber)
            val etWeight = setView.findViewById<EditText>(R.id.etWeight)
            val etReps = setView.findViewById<EditText>(R.id.etReps)
            val etTime = setView.findViewById<EditText>(R.id.etTime)
            val etDistance = setView.findViewById<EditText>(R.id.etDistance)
            val etRir = setView.findViewById<EditText>(R.id.etRir)

            // --- LOGIKA KOPÍROVACÍHO TLAČÍTKA ---
            if (previousSet != null) {
                btnCopy.visibility = View.VISIBLE
                btnCopy.setOnClickListener {
                    if (config.isWeightEnabled) {
                        etWeight.setText(previousSet.weight)
                        setUi.weight = previousSet.weight
                    }
                    if (config.isRepsEnabled) {
                        etReps.setText(previousSet.reps)
                        setUi.reps = previousSet.reps
                    }
                    if (config.isTimeEnabled) {
                        etTime.setText(previousSet.time)
                        setUi.time = previousSet.time
                    }
                    if (config.isDistanceEnabled) {
                        etDistance.setText(previousSet.distance)
                        setUi.distance = previousSet.distance
                    }
                }
            } else {
                btnCopy.visibility = View.INVISIBLE
            }

            // --- NASTAVENÍ HODNOT ---
            etWeight.isVisible = config.isWeightEnabled
            etReps.isVisible = config.isRepsEnabled
            etTime.isVisible = config.isTimeEnabled
            etDistance.isVisible = config.isDistanceEnabled
            etRir.isVisible = config.isRirEnabled

            tvSetNumber.text = setUi.setNumber.toString()
            etWeight.setText(setUi.weight)
            etReps.setText(setUi.reps)
            etTime.setText(setUi.time)
            etDistance.setText(setUi.distance)
            etRir.setText(setUi.rir)

            // --- TEXT WATCHERS (Ukládání) ---
            if (config.isWeightEnabled) etWeight.addSimpleTextWatcher { setUi.weight = it }
            if (config.isRepsEnabled) etReps.addSimpleTextWatcher { setUi.reps = it }
            if (config.isTimeEnabled) etTime.addSimpleTextWatcher { setUi.time = it }
            if (config.isDistanceEnabled) etDistance.addSimpleTextWatcher { setUi.distance = it }
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