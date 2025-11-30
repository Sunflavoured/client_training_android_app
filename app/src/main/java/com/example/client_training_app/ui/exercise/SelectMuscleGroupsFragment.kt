package com.example.client_training_app.ui.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.client_training_app.databinding.FragmentSelectMuscleGroupsBinding
import com.example.client_training_app.model.MuscleGroup
import com.example.client_training_app.ui.adapters.MuscleGroupAdapter

class SelectMuscleGroupsFragment : Fragment() {

    private var _binding: FragmentSelectMuscleGroupsBinding? = null
    private val binding get() = _binding!!

    // Načteme argumenty pomocí Safe-Args
    private val args: SelectMuscleGroupsFragmentArgs by navArgs()
    private lateinit var adapter: MuscleGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectMuscleGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Převedeme příchozí pole na MutableSet
        val currentSelection = args.currentSelection.toMutableSet()
        val allMuscleGroups = MuscleGroup.Companion.getAllDisplayNames()

        // Vytvoříme a nastavíme adaptér
        adapter = MuscleGroupAdapter(allMuscleGroups, currentSelection)
        binding.muscleGroupsRecyclerView.adapter = adapter

        // --- Logika tlačítek ---

        binding.btnConfirm.setOnClickListener {
            // Pošleme aktuální výběr z adaptéru
            sendResultAndClose(adapter.getSelectedGroups())
        }

        binding.btnClear.setOnClickListener {
            // Pošleme prázdný seznam
            sendResultAndClose(emptyList())
        }

        binding.btnCancel.setOnClickListener {
            // Jen se vrátíme, nic neposíláme
            findNavController().popBackStack()
        }
    }

    private fun sendResultAndClose(selected: List<String>) {
        // Pošleme výsledek zpět na "posluchače" s klíčem "muscleGroupRequest"
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(BUNDLE_KEY to selected)
        )
        // Vrátíme se zpět
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Klíče pro komunikaci mezi fragmenty
        const val REQUEST_KEY = "muscleGroupRequest"
        const val BUNDLE_KEY = "selectedGroups"
    }
}