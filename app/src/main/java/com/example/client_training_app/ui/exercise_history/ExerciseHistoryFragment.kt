package com.example.client_training_app.ui.exercise_history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment // Důležitý import
import androidx.fragment.app.viewModels // Důležitý import
import androidx.navigation.fragment.findNavController // Pro tlačítko zpět
import androidx.navigation.fragment.navArgs // Pro argumenty
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.R
import com.example.client_training_app.databinding.FragmentExerciseHistoryBinding
import com.example.client_training_app.ui.adapters.ExerciseHistoryAdapter

// Dědíme z Fragmentu, ne z AndroidViewModel!
class ExerciseHistoryFragment : Fragment(R.layout.fragment_exercise_history) {

    // Tady už navArgs bude fungovat
    private val args: ExerciseHistoryFragmentArgs by navArgs()

    // ViewModel si vytáhneme pomocí delegáta 'by viewModels()'
    private val viewModel: ExerciseHistoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentExerciseHistoryBinding.bind(view)

        // Nastavení textu z argumentů
        binding.tvTitle.text = "Cvik: ${args.exerciseName}"


        // Nastavení seznamu
        val adapter = ExerciseHistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        // Sledování dat z ViewModelu
        viewModel.getHistory(args.clientId, args.exerciseId).observe(viewLifecycleOwner) { historyList ->
            adapter.submitList(historyList)
        }
    }
}