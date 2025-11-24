package com.example.client_training_app

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.client_training_app.data.database.ClientRepository
import com.example.client_training_app.model.Client
import com.example.client_training_app.databinding.FragmentAddProfileBinding
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat

class AddProfileFragment : Fragment() {

    private var _binding: FragmentAddProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ClientRepository

    // Uloží vybrané datum narození jako timestamp (Long)
    private var selectedBirthDateMillis: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProfileBinding.inflate(inflater, container, false)
        repository = ClientRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nastavení posluchače pro otevření DatePickeru
        binding.editTextBirthDate.setOnClickListener {
            showDatePicker()
        }

        // Nastavení posluchače pro tlačítko Uložit
        binding.buttonSaveProfile.setOnClickListener {
            saveNewClient()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        // Použijeme aktuálně vybrané datum, pokud existuje
        selectedBirthDateMillis?.let {
            calendar.timeInMillis = it
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                selectedBirthDateMillis = calendar.timeInMillis

                // Zobrazení data ve formátu dd.MM.yyyy
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                binding.editTextBirthDate.setText(dateFormat.format(calendar.time))

                updateAgeDisplay(selectedBirthDateMillis)
            },
            year, month, day
        )
        // Omezení, aby nešlo vybrat datum v budoucnosti
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateAgeDisplay(birthDateMillis: Long?) {
        if (birthDateMillis == null) {
            binding.textViewCalculatedAge.visibility = View.GONE
            return
        }

        val age = calculateAge(birthDateMillis)
        binding.textViewCalculatedAge.text = "Věk: $age"
        binding.textViewCalculatedAge.visibility = View.VISIBLE
    }

    // Pomocná funkce pro výpočet věku
    private fun calculateAge(birthDateMillis: Long): Int {
        val today = Calendar.getInstance()
        val birthDate = Calendar.getInstance().apply { timeInMillis = birthDateMillis }

        var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    private fun saveNewClient() {
        val firstName = binding.editTextFirstName.text.toString().trim()
        val lastName = binding.editTextLastName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val notes = binding.editTextNotes.text.toString().trim().ifEmpty { null }

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(requireContext(), "Jméno a příjmení jsou povinná pole.", Toast.LENGTH_LONG).show()
            return
        }

        val newClient = Client(
            // Generování unikátního ID pro databázi
            id = UUID.randomUUID().toString(),
            firstName = firstName,
            lastName = lastName,
            birthDate = selectedBirthDateMillis,
            // Nastavíme null, pokud jsou pole prázdná
            email = email.ifEmpty { null },
            phone = phone.ifEmpty { null },
            notes = notes
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.addClient(newClient)
                Toast.makeText(requireContext(), "Klient ${firstName} ${lastName} uložen.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Návrat zpět do ProfilesFragmentu
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Chyba při ukládání klienta: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}