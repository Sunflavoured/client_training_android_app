package com.example.client_training_app.ui.training_unit

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.R
import com.example.client_training_app.data.repository.TrainingUnitRepository
import com.example.client_training_app.databinding.FragmentTrainingUnitLibraryBinding // Nový layout
import com.example.client_training_app.ui.adapters.TrainingUnitAdapter // Nový adaptér
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class TrainingUnitLibraryFragment : Fragment(R.layout.fragment_training_unit_library) {

    private lateinit var binding: FragmentTrainingUnitLibraryBinding
    private lateinit var adapter: TrainingUnitAdapter
    private val viewModel: TrainingUnitEditorViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrainingUnitLibraryBinding.bind(view)

        setupRecyclerView()
        loadTrainingUnits()
        setupFab()
        setupSwipeToDelete()
    }

    private fun setupRecyclerView() {
        adapter = TrainingUnitAdapter { unit ->
            //  Navigace do DETAILU (náhled)
            val action = TrainingUnitLibraryFragmentDirections
                .actionTrainingUnitLibraryFragmentToTrainingUnitDetailFragment(
                    trainingUnitId = unit.id
                )
            findNavController().navigate(action)
        }

        binding.rvTrainingUnits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTrainingUnits.adapter = adapter
    }

    private fun loadTrainingUnits() {
        val repository = TrainingUnitRepository(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllUnitsFlow().collectLatest { units ->
                adapter.submitList(units)

                // Logika pro Empty State
                if (units.isEmpty()) {
                    binding.rvTrainingUnits.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvTrainingUnits.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                }
            }
        }
    }

    private fun setupSwipeToDelete() {
        // Barva pozadí (Červená)
        val deleteColor = android.graphics.Color.parseColor("#E53935") // Červená
        val deleteIcon = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_24) // Tvoje ikona koše
        val paint = android.graphics.Paint()

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            // --- 1. VYKRESLOVÁNÍ POZADÍ A IKONY ---
            override fun onChildDraw(
                c: android.graphics.Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val cornerRadius = 40f
                // Pokud swipujeme doleva (dX je záporné)
                if (dX < 0) {

                    // 1. Nakreslíme červené pozadí
                    paint.color = deleteColor
                    // Obdélník: od pravého okraje (itemView.right) doleva o dX
                    val background = android.graphics.RectF(
                        itemView.right.toFloat() + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )
                    c.drawRoundRect(background, cornerRadius, cornerRadius, paint)
                    // 2. Nakreslíme ikonu koše
                    if (deleteIcon != null) {
                        val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
                        val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
                        val iconBottom = iconTop + deleteIcon.intrinsicHeight

                        // Ikona bude u pravého okraje
                        val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
                        val iconRight = itemView.right - iconMargin

                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        deleteIcon.draw(c)
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            // --- 2. LOGIKA PO SWIPNUTÍ (DIALOG) ---
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val unitToDelete = adapter.currentList[position]
                adapter.notifyItemChanged(position)
                // Zobrazíme Dialog
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Smazat trénink?")
                    .setMessage("Opravdu chcete trvale odstranit trénink '${unitToDelete.name}'?")
                    .setPositiveButton("Smazat") { _, _ ->
                        // Uživatel potvrdil -> Smažeme z DB
                        viewModel.deleteTrainingUnit(unitToDelete)

                        com.google.android.material.snackbar.Snackbar.make(
                            binding.root,
                            "Trénink byl smazán",
                            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Zrušit") { dialog, _ ->
                        // Teprve až když řekne ANO, mažeme
                        viewModel.deleteTrainingUnit(unitToDelete)

                        com.google.android.material.snackbar.Snackbar.make(
                            binding.root,
                            "Trénink byl smazán",
                            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Zrušit", null) // Nemusíme dělat nic, řádek už je zpátky
                    .show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvTrainingUnits)
    }

    private fun setupFab() {
        binding.fabAddUnit.setOnClickListener {
            // Navigace do Editoru pro vytvoření NOVÉ, globální jednotky
            // clientId je null, protože je globální
            val action = TrainingUnitLibraryFragmentDirections
                .actionTrainingUnitLibraryFragmentToTrainingUnitEditorFragment(clientId = null)
            findNavController().navigate(action)
        }
    }
}