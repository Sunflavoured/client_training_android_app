package com.example.client_training_app.ui.active_workout

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.R
import com.example.client_training_app.databinding.FragmentActiveWorkoutBinding
import com.example.client_training_app.ui.adapters.ActiveWorkoutAdapter

class ActiveWorkoutFragment : Fragment(R.layout.fragment_active_workout) {

    private val args: ActiveWorkoutFragmentArgs by navArgs()
    private val viewModel: ActiveWorkoutViewModel by viewModels()
    private lateinit var binding: FragmentActiveWorkoutBinding
    private lateinit var adapter: ActiveWorkoutAdapter

    // ČASOVAČ JSME ODSTRANILI (proměnné seconds, handler, runnable jsou pryč)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentActiveWorkoutBinding.bind(view)

        // Nastavení názvu hned na začátku (pokud jsme ho poslali v argumentech)
        binding.tvWorkoutTitle.text = args.trainingTitle

        if (savedInstanceState == null) {
            val scheduleId = if (args.scheduledWorkoutId == -1L) null else args.scheduledWorkoutId
            viewModel.startWorkout(args.trainingUnitId, args.clientId, scheduleId)
        }

        setupRecyclerView()
        setupButtons()
        setupBackPress() // Vyčlenil jsem to do funkce pro přehlednost
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ActiveWorkoutAdapter(
            exercises = emptyList(),
            onAddSetClicked = { exerciseIndex ->
                viewModel.addSet(exerciseIndex)
            }
        )
        binding.rvActiveExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActiveExercises.adapter = adapter
        binding.rvActiveExercises.setItemViewCacheSize(20)
    }

    private fun setupButtons() {
        binding.btnFinishWorkout.setOnClickListener {
            viewModel.finishWorkout()
        }
    }

    // Ochrana před nechtěným opuštěním
    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Ukončit trénink?")
                .setMessage("Máte rozdělaný trénink. Neuložená data se ztratí.")
                .setPositiveButton("Odejít") { _, _ ->
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                .setNegativeButton("Zrušit", null)
                .show()
        }
    }

    private fun observeViewModel() {
        viewModel.activeExercises.observe(viewLifecycleOwner) { exercises ->
            adapter.updateData(exercises)
        }

        // NOVÉ: Sledování poznámky
        viewModel.trainingNote.observe(viewLifecycleOwner) { note ->
            if (!note.isNullOrEmpty()) {
                binding.tvTrainingNote.text = note
                binding.tvTrainingNote.isVisible = true
            } else {
                binding.tvTrainingNote.isVisible = false
            }
        }

        viewModel.isFinished.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                Toast.makeText(requireContext(), "Trénink uložen! 💪", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
        }
    }

    // onDestroyView už není potřeba řešit (žádný timer k zastavení)
}