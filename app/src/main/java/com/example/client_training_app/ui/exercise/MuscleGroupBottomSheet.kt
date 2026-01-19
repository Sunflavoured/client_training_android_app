package com.example.client_training_app.ui.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.client_training_app.R
import com.example.client_training_app.model.MuscleGroup
import android.widget.CheckedTextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// Callback, který vrátí seznam vybraných stringů
class MuscleGroupBottomSheet(
    private val preSelectedMuscles: List<String>,
    private val onSelectionConfirmed: (List<String>) -> Unit
) : BottomSheetDialogFragment() {

    // Seznam všech možných svalů (můžeš to vytáhnout do nějakého Constant souboru)
    private val allMuscleGroups = MuscleGroup.getAllDisplayNames().sorted()


    // Pomocná třída pro adaptér
    data class MuscleItem(val name: String, var isSelected: Boolean)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_muscle_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvMuscleGroups)
        val btnConfirm = view.findViewById<View>(R.id.btnConfirmSelection)

        // Příprava dat (spojíme všechny svaly s informací, jestli jsou vybrané)
        val items = allMuscleGroups.map { name ->
            MuscleItem(name, preSelectedMuscles.contains(name))
        }

        // Nastavení adaptéru (viz níže - jednoduchý vnitřní adaptér)
        val adapter = MuscleGroupAdapter(items)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        btnConfirm.setOnClickListener {
            // Vyfiltrujeme jen ty, co mají isSelected == true
            val result = items.filter { it.isSelected }.map { it.name }
            onSelectionConfirmed(result)
            dismiss() // Zavřít BottomSheet
        }
    }

    // --- JEDNODUCHÝ ADAPTÉR PRO CHECKBOXY ---
    inner class MuscleGroupAdapter(private val items: List<MuscleItem>) :
        RecyclerView.Adapter<MuscleGroupAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val checkedTextView: CheckedTextView = itemView.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // Použijeme jednoduchý android layout pro checkbox
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_multiple_choice, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.checkedTextView.text = item.name
            holder.checkedTextView.isChecked = item.isSelected

            holder.itemView.setOnClickListener {
                // 1. Změníme vizuální stav (fajfku)
                holder.checkedTextView.toggle()

                // 2. Uložíme nový stav do datového modelu
                item.isSelected = holder.checkedTextView.isChecked
            }
        }

        override fun getItemCount() = items.size
    }
}