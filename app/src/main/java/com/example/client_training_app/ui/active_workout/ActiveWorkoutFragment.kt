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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView // Důležitý import pro ItemTouchHelper
import com.example.client_training_app.R
import com.example.client_training_app.databinding.FragmentActiveWorkoutBinding
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.ui.adapters.ActiveWorkoutAdapter

class ActiveWorkoutFragment : Fragment(R.layout.fragment_active_workout) {

    private val args: ActiveWorkoutFragmentArgs by navArgs()
    private val viewModel: ActiveWorkoutViewModel by viewModels()
    private lateinit var binding: FragmentActiveWorkoutBinding
    private lateinit var adapter: ActiveWorkoutAdapter

    // Ukládáme si index cviku, který chceme nahradit (pokud null, tak přidáváme nový)
    private var indexToSubstitute: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentActiveWorkoutBinding.bind(view)

        binding.tvWorkoutTitle.text = args.trainingTitle

        if (savedInstanceState == null) {
            val scheduleId = if (args.scheduledWorkoutId == -1L) null else args.scheduledWorkoutId
            viewModel.startWorkout(args.trainingUnitId, args.clientId, scheduleId)
        }

        setupRecyclerView()
        setupButtons()
        setupBackPress()
        observeViewModel()

        // Inicializace listeneru pro návrat z výběru cviku
        setupExercisePickerResult()
    }

    private fun setupRecyclerView() {
        // 1. Nastavení Drag & Drop
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition

                // Prohodíme data ve ViewModelu
                viewModel.moveExercise(fromPos, toPos)

                // Řekneme adaptéru o vizuální změně
                adapter.notifyItemMoved(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Swipe nepoužíváme
            }

            override fun isLongPressDragEnabled(): Boolean = false
        })

        itemTouchHelper.attachToRecyclerView(binding.rvActiveExercises)

        // 2. Inicializace adaptéru se všemi callbacky
        adapter = ActiveWorkoutAdapter(
            exercises = emptyList(),

            onAddSetClicked = { exerciseIndex ->
                viewModel.addSet(exerciseIndex)
            },

            onDragStart = { viewHolder ->
                // Manuálně spustíme drag, když uživatel chytne "držadlo"
                itemTouchHelper.startDrag(viewHolder)
            },

            onHistoryClick = { exerciseId, exerciseName ->
                // TADY JE NAVIGACE
                val clientId = viewModel.currentClientId // Musíme zpřístupnit clientId ve ViewModelu

                val action = ActiveWorkoutFragmentDirections.actionActiveWorkoutToHistory(
                    clientId = clientId,
                    exerciseId = exerciseId,
                    exerciseName = exerciseName
                )
                findNavController().navigate(action)
            },

            onSubstituteClicked = { exerciseIndex ->
                // Uložíme si index a jdeme vybírat náhradní cvik
                indexToSubstitute = exerciseIndex
                val action = ActiveWorkoutFragmentDirections.actionActiveWorkoutFragmentToExercisePickerFragment()
                findNavController().navigate(action)
            }
        )

        binding.rvActiveExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActiveExercises.adapter = adapter
        // Optimalizace pro scrollování složitějších položek
        binding.rvActiveExercises.setItemViewCacheSize(20)
    }

    private fun setupButtons() {
        // Tlačítko Dokončit trénink
        binding.btnFinishWorkout.setOnClickListener {
            viewModel.finishWorkout()
        }

        // Tlačítko Přidat extra cvik (musí být v XML)
        binding.btnAddExtraExercise.setOnClickListener {
            indexToSubstitute = null // Důležité: nulujeme, protože přidáváme, nenahrazujeme
            val action = ActiveWorkoutFragmentDirections.actionActiveWorkoutFragmentToExercisePickerFragment()
            findNavController().navigate(action)
        }
    }

    // Zpracování návratu z ExercisePickerFragment
    private fun setupExercisePickerResult() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Exercise>("selected_exercise")
            ?.observe(viewLifecycleOwner) { exercise ->
                if (exercise != null) {
                    if (indexToSubstitute != null) {
                        // REŽIM NAHRAZENÍ (Substitute)
                        viewModel.substituteExercise(indexToSubstitute!!, exercise)
                        Toast.makeText(requireContext(), "Cvik nahrazen", Toast.LENGTH_SHORT).show()
                        indexToSubstitute = null // Reset
                    } else {
                        // REŽIM PŘIDÁNÍ NOVÉHO
                        viewModel.addNewExercise(exercise)
                        Toast.makeText(requireContext(), "Cvik přidán", Toast.LENGTH_SHORT).show()
                    }

                    // Vyčistit výsledek, aby se nepřidal znovu při otočení displeje
                    findNavController().currentBackStackEntry?.savedStateHandle
                        ?.remove<Exercise>("selected_exercise")
                }
            }
    }

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
}