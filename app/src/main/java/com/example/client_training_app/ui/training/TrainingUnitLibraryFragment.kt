package com.example.client_training_app.ui.training

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.R
import com.example.client_training_app.data.entity.TrainingUnitEntity
import com.example.client_training_app.data.repository.TrainingUnitRepository
import com.example.client_training_app.databinding.FragmentTrainingUnitLibraryBinding // Nový layout
import com.example.client_training_app.ui.adapters.TrainingUnitAdapter // Nový adaptér
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.example.client_training_app.ui.training.TrainingUnitLibraryFragmentDirections

class TrainingUnitLibraryFragment : Fragment(R.layout.fragment_training_unit_library) {

    private lateinit var binding: FragmentTrainingUnitLibraryBinding
    private lateinit var adapter: TrainingUnitAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrainingUnitLibraryBinding.bind(view)

        setupRecyclerView()
        loadTrainingUnits()
        setupFab()
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
            repository.getGlobalUnitsFlow().collectLatest { units ->
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