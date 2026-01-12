package com.example.client_training_app.ui.training

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.R
import com.example.client_training_app.databinding.FragmentTrainingUnitEditorBinding
import com.example.client_training_app.model.Exercise
import com.example.client_training_app.ui.adapters.UnitExerciseEditorAdapter

class TrainingUnitEditorFragment : Fragment(R.layout.fragment_training_unit_editor) {

    private val args: TrainingUnitEditorFragmentArgs by navArgs()
    private val viewModel: TrainingUnitEditorViewModel by viewModels()
    private lateinit var binding: FragmentTrainingUnitEditorBinding
    private lateinit var adapter: UnitExerciseEditorAdapter
    private lateinit var backCallback: OnBackPressedCallback

    // Sledování neuložených změn
    private var hasUnsavedChanges = false

    // Flag pro sledování, jestli už jsou data načtená
    private var isDataLoaded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrainingUnitEditorBinding.bind(view)

        setupRecyclerView()
        setupButtons()
        setupExercisePickerListener()
        setupBackPressHandler()
        setupChangeTracking()
        observeViewModel()

        if (args.trainingUnitIdToEdit != null && savedInstanceState == null) {
            viewModel.loadUnitData(args.trainingUnitIdToEdit!!)
            binding.btnSaveUnit.text = "Uložit"
        } else if (savedInstanceState == null) {
            // Pokud nevytváříme nový trénink (bez načítání dat), data jsou "načtená"
            isDataLoaded = true
        }
    }



    // --- SLEDOVÁNÍ ZMĚN ---

    private fun setupChangeTracking() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Změny se počítají pouze pokud už jsou data načtená
                if (isDataLoaded) {
                    hasUnsavedChanges = true
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etUnitName.addTextChangedListener(textWatcher)
        binding.etUnitNote.addTextChangedListener(textWatcher)
    }

    // --- LOGIKA HLÍDÁNÍ ODCHODU ---

    private fun setupBackPressHandler() {
        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Zobrazíme dialog pouze pokud jsou neuložené změny
                if (hasUnsavedChanges) {
                    showUnsavedChangesDialog()
                } else {
                    // Pokud nejsou změny, vypneme callback a necháme systém navigovat
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
    }

    private fun showUnsavedChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Neuložené změny")
            .setMessage("Máte rozpracované změny. Chcete je před odchodem uložit?")
            .setPositiveButton("Uložit") { _, _ ->
                performSave()
            }
            .setNegativeButton("Zahodit") { _, _ ->
                // Vypneme hlídače a odejdeme
                backCallback.isEnabled = false
                findNavController().popBackStack()
            }
            .setNeutralButton("Zrušit", null)
            .show()
    }

    // --- LOGIKA UKLÁDÁNÍ A ODCHODU ---

    private fun performSave() {
        val name = binding.etUnitName.text.toString()
        val note = binding.etUnitNote.text.toString()

        if (name.isBlank()) {
            Toast.makeText(requireContext(), "Vyplňte název tréninku", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveTrainingUnit(name, note, args.clientId) {
            Toast.makeText(requireContext(), "Uloženo!", Toast.LENGTH_SHORT).show()

            // Vypneme hlídače a označíme, že už nejsou neuložené změny
            hasUnsavedChanges = false
            backCallback.isEnabled = false

            // Vrátíme se zpět na předchozí obrazovku
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = UnitExerciseEditorAdapter(
            onDataChanged = { updatedItem ->
                viewModel.updateTemplateExercise(updatedItem)
                hasUnsavedChanges = true
            },
            onDeleteClicked = { itemToDelete ->
                viewModel.deleteTemplateExercise(itemToDelete)
                hasUnsavedChanges = true
            },
            onSettingsClicked = { itemToEdit ->
                // Otevřeme BottomSheet dialog
                val dialog = com.example.client_training_app.ui.training.ExerciseSettingsBottomSheet(
                    currentSettings = itemToEdit,
                    onSettingsChanged = { updatedSettings ->
                        // Když uživatel v dialogu klikne na "Použít", aktualizujeme ViewModel
                        viewModel.updateTemplateExercise(updatedSettings)
                        hasUnsavedChanges = true
                    }
                )
                dialog.show(parentFragmentManager, "ExerciseSettingsBottomSheet")
            }
        )
        // Nastavení, které opravilo bug kurzoru přeskakujícího na začátek řádku
        binding.rvAddedExercises.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TrainingUnitEditorFragment.adapter
            // Optimalizace pro RecyclerView, pokud se nemění jeho velikost
            setHasFixedSize(true)
            // Vypneme animace při změně (aby neblikaly inputy při psaní)
            (itemAnimator as? androidx.recyclerview.widget.SimpleItemAnimator)?.supportsChangeAnimations = false
        }

    }

    private fun setupButtons() {
        binding.btnAddExercise.setOnClickListener {
            val action = TrainingUnitEditorFragmentDirections
                .actionTrainingUnitEditorFragmentToExercisePickerFragment()
            findNavController().navigate(action)
        }

        binding.btnSaveUnit.setOnClickListener {
            performSave()
        }
    }

    private fun observeViewModel() {
        viewModel.templateExercises.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list.toList())
        }

        viewModel.initialUnitName.observe(viewLifecycleOwner) { name ->
            if (binding.etUnitName.text.isNullOrEmpty()) {
                binding.etUnitName.setText(name)
                // Po nastavení dat z ViewModelu počkáme chvíli a pak povolíme sledování změn
                binding.root.postDelayed({
                    isDataLoaded = true
                    hasUnsavedChanges = false
                }, 100)
            } else {
                // Pokud už text máme (např. návrat z výběru cviku nebo rotace), data považujeme za načtená
                isDataLoaded = true
            }
        }

        viewModel.initialUnitNote.observe(viewLifecycleOwner) { note ->
            if (binding.etUnitNote.text.isNullOrEmpty()) {
                binding.etUnitNote.setText(note)
            }
        }
    }

    private fun setupExercisePickerListener() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Exercise>("selected_exercise")
            ?.observe(viewLifecycleOwner) { exercise ->
                if (exercise != null) {
                    viewModel.addExercise(exercise)
                        hasUnsavedChanges = true

                    findNavController().currentBackStackEntry?.savedStateHandle
                        ?.remove<Exercise>("selected_exercise")
                }
            }
    }
}