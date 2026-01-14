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

    // Binding by měl být nullable pro správné uvolnění paměti v onDestroyView, ale pro jednoduchost necháme takto
    private lateinit var binding: FragmentTrainingUnitEditorBinding
    private lateinit var adapter: UnitExerciseEditorAdapter
    private lateinit var backCallback: OnBackPressedCallback

    private var hasUnsavedChanges = false
    private var isDataLoaded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrainingUnitEditorBinding.bind(view)

        setupRecyclerView()
        setupButtons()
        setupClientDropdown()
        setupExercisePickerListener()
        setupBackPressHandler()
        setupChangeTracking()
        observeViewModel()

        if (args.trainingUnitIdToEdit != null && savedInstanceState == null) {
            // EDITACE
            viewModel.loadUnitData(args.trainingUnitIdToEdit!!)
            binding.btnSaveUnit.text = "Uložit"
        } else if (savedInstanceState == null) {
            // NOVÝ
            isDataLoaded = true
            if (args.clientId != null) {
                viewModel.selectedClientId.value = args.clientId
            }
        }
    }

    // --- KLIENT DROP DOWN ---
    private fun setupClientDropdown() {
        viewModel.clients.observe(viewLifecycleOwner) { clientList ->
            val adapterItems = mutableListOf("Globální (pro všechny)")
            adapterItems.addAll(clientList.map { "${it.firstName} ${it.lastName}" }) // Upraveno na celé jméno

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, adapterItems)

            // Bezpečné přetypování a nastavení adaptéru
            (binding.actClientSelect as? AutoCompleteTextView)?.setAdapter(adapter)

            (binding.actClientSelect as? AutoCompleteTextView)?.setOnItemClickListener { _, _, position, _ ->
                if (position == 0) {
                    viewModel.selectedClientId.value = null
                } else {
                    val selectedClient = clientList[position - 1]
                    viewModel.selectedClientId.value = selectedClient.id
                }
                if (isDataLoaded) hasUnsavedChanges = true
            }

            viewModel.selectedClientId.observe(viewLifecycleOwner) { selectedId ->
                val textToDisplay = if (selectedId == null) {
                    "Globální (pro všechny)"
                } else {
                    val client = clientList.find { it.id == selectedId }
                    if (client != null) "${client.firstName} ${client.lastName}" else "Globální (pro všechny)"
                }
                (binding.actClientSelect as? AutoCompleteTextView)?.setText(textToDisplay, false)
            }
        }
    }

    // --- RECYCLER VIEW (OPRAVENO PRO NESTED SCROLL) ---
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
                // Předpokládám, že tento dialog existuje
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
            // <--- OPRAVA 1: Vlastní LayoutManager, který zakáže interní skrolování
            layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun canScrollVertically() = false
            }

            adapter = this@TrainingUnitEditorFragment.adapter

            // <--- OPRAVA 2: Vypnutí nested skrolování a fixní velikosti
            isNestedScrollingEnabled = false
            setHasFixedSize(false) // Protože je wrap_content, velikost se mění!

            // Vypnutí animací (volitelné, pomáhá proti problikávání)
            itemAnimator = null
        }
    }

    // --- OBSERVERS ---
    private fun observeViewModel() {
        viewModel.templateExercises.observe(viewLifecycleOwner) { list ->
            // <--- OPRAVA 3: .toList() pro vytvoření kopie a requestLayout() pro překreslení
            adapter.submitList(list.toList()) {
                // Toto se zavolá, až adaptér zpracuje data
                if (list.isNotEmpty()) {
                    binding.rvAddedExercises.requestLayout()
                }
            }
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

    // --- ZBYTEK FUNKCÍ (Stejné jako dřív) ---

    private fun setupChangeTracking() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isDataLoaded) hasUnsavedChanges = true
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etUnitName.addTextChangedListener(textWatcher)
        binding.etUnitNote.addTextChangedListener(textWatcher)
    }

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
            .setPositiveButton("Uložit") { _, _ -> performSave() }
            .setNegativeButton("Zahodit") { _, _ ->
                backCallback.isEnabled = false
                findNavController().popBackStack()
            }
            .setNeutralButton("Zrušit", null)
            .show()
    }

    private fun performSave() {
        val name = binding.etUnitName.text.toString()
        val note = binding.etUnitNote.text.toString()

        if (name.isBlank()) {
            binding.etUnitName.error = "Vyplňte název" // Lepší než Toast
            return
        }

        viewModel.saveTrainingUnit(name, note) {
            Toast.makeText(requireContext(), "Uloženo!", Toast.LENGTH_SHORT).show()
            hasUnsavedChanges = false
            backCallback.isEnabled = false
            findNavController().popBackStack()
        }
    }

    private fun setupButtons() {
        binding.btnAddExercise.setOnClickListener {
            val action = TrainingUnitEditorFragmentDirections
                .actionTrainingUnitEditorFragmentToExercisePickerFragment()
            findNavController().navigate(action)
        }

        binding.btnSaveUnit.setOnClickListener { performSave() }
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