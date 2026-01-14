package com.example.client_training_app.ui.training_unit

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
        setupClientDropdown() // <--- NOVÉ: Nastavení roletky
        setupExercisePickerListener()
        setupBackPressHandler()
        setupChangeTracking()
        observeViewModel()

        if (args.trainingUnitIdToEdit != null && savedInstanceState == null) {
            // EDITACE EXISTUJÍCÍHO
            viewModel.loadUnitData(args.trainingUnitIdToEdit!!)
            binding.btnSaveUnit.text = "Uložit"
        } else if (savedInstanceState == null) {
            // NOVÝ TRÉNINK
            isDataLoaded = true

            // Pokud přicházíme z profilu konkrétního klienta, chceme ho rovnou předvybrat
            if (args.clientId != null) {
                viewModel.selectedClientId.value = args.clientId
            }
        }
    }

    // --- NOVÉ: LOGIKA VÝBĚRU KLIENTA ---

    private fun setupClientDropdown() {
        // 1. Sledujeme seznam všech klientů
        viewModel.clients.observe(viewLifecycleOwner) { clientList ->

            // Vytvoříme seznam jmen pro adaptér (první je vždy "Globální")
            val adapterItems = mutableListOf("Globální (pro všechny)")
            adapterItems.addAll(clientList.map { it.lastName })

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, adapterItems)

            // Najdeme AutoCompleteTextView v layoutu (ujisti se, že máš v XML ID 'actClientSelect')
            // binding.actClientSelect je AutoCompleteTextView uvnitř TextInputLayout
            (binding.actClientSelect as? AutoCompleteTextView)?.setAdapter(adapter)

            // 2. Nastavíme listener pro kliknutí na položku v roletce
            (binding.actClientSelect as? AutoCompleteTextView)?.setOnItemClickListener { _, _, position, _ ->
                if (position == 0) {
                    viewModel.selectedClientId.value = null // Globální
                } else {
                    // Klient je na indexu o 1 menším (protože 0 je "Globální")
                    val selectedClient = clientList[position - 1]
                    viewModel.selectedClientId.value = selectedClient.id
                }

                if (isDataLoaded) hasUnsavedChanges = true
            }

            // 3. Synchronizace textu roletky s aktuálně vybraným ID (např. při načtení dat)
            // Musíme to udělat uvnitř observeru klientů, protože potřebujeme znát jejich jména
            viewModel.selectedClientId.observe(viewLifecycleOwner) { selectedId ->
                val textToDisplay = if (selectedId == null) {
                    "Globální (pro všechny)"
                } else {
                    clientList.find { it.id == selectedId }?.lastName ?: "Globální (pro všechny)"
                }

                // setText(..., false) je důležité, aby se nespustil filtr
                (binding.actClientSelect as? AutoCompleteTextView)?.setText(textToDisplay, false)
            }
        }
    }

    // --- SLEDOVÁNÍ ZMĚN ---

    private fun setupChangeTracking() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
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
                if (hasUnsavedChanges) {
                    showUnsavedChangesDialog()
                } else {
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

        // ZMĚNA: Už neposíláme args.clientId, ViewModel si bere hodnotu ze 'selectedClientId'
        viewModel.saveTrainingUnit(name, note) {
            Toast.makeText(requireContext(), "Uloženo!", Toast.LENGTH_SHORT).show()
            hasUnsavedChanges = false
            backCallback.isEnabled = false
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
                val dialog = com.example.client_training_app.ui.training_unit.ExerciseSettingsBottomSheet(
                    currentSettings = itemToEdit,
                    onSettingsChanged = { updatedSettings ->
                        viewModel.updateTemplateExercise(updatedSettings)
                        hasUnsavedChanges = true
                    }
                )
                dialog.show(parentFragmentManager, "ExerciseSettingsBottomSheet")
            }
        )

        binding.rvAddedExercises.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TrainingUnitEditorFragment.adapter
            setHasFixedSize(true)
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
                binding.root.postDelayed({
                    isDataLoaded = true
                    hasUnsavedChanges = false
                }, 100)
            } else {
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