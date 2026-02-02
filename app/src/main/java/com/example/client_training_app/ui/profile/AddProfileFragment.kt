package com.example.client_training_app.ui.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs // Důležitý import pro argumenty
import com.example.client_training_app.data.repository.ClientRepository
import com.example.client_training_app.databinding.FragmentAddProfileBinding
import com.example.client_training_app.model.Client
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddProfileFragment : Fragment() {

    private var _binding: FragmentAddProfileBinding? = null
    private val binding get() = _binding!!

    // 1. Získáme argumenty (clientId), které posíláme z Detailu
    private val args: AddProfileFragmentArgs by navArgs()

    private lateinit var repository: ClientRepository

    // Proměnná pro uložení klienta, kterého editujeme (pokud nějaký je)
    private var clientToUpdate: Client? = null

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

        // 2. Zjistíme, jestli máme ID (Editace) nebo je null (Nový profil)
        val editingClientId = args.clientId

        if (editingClientId != null) {
            // --- REŽIM EDITACE ---
            loadClientData(editingClientId)
            binding.buttonSaveProfile.text = "Uložit změny"
        } else {
            // --- REŽIM NOVÝ PROFIL ---
            activity?.title = "Nový profil klienta"
            binding.buttonSaveProfile.text = "Vytvořit profil"
        }

        binding.editTextBirthDate.setOnClickListener {
            showDatePicker()
        }

        binding.buttonSaveProfile.setOnClickListener {
            saveClient()
        }
    }

    // Funkce pro načtení a předvyplnění dat
    private fun loadClientData(clientId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Předpokládám, že repository má metodu vracející Flow nebo suspend funkci
            // Zde použijeme collect, abychom získali data
            repository.getClientByIdFlow(clientId).collect { client ->
                if (client != null) {
                    clientToUpdate = client // Uložíme si originál (hlavně kvůli ID)

                    // 3. Změna titulku v Toolbaru podle jména
                    activity?.title = "Upravit: ${client.firstName} ${client.lastName}"

                    // 4. Předvyplnění políček
                    binding.editTextFirstName.setText(client.firstName)
                    binding.editTextLastName.setText(client.lastName)
                    binding.editTextEmail.setText(client.email)
                    binding.editTextPhone.setText(client.phone)
                    binding.editTextNotes.setText(client.notes)

                    // Předvyplnění data narození
                    selectedBirthDateMillis = client.birthDate
                    selectedBirthDateMillis?.let { dateMillis ->
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        binding.editTextBirthDate.setText(dateFormat.format(java.util.Date(dateMillis)))
                        updateAgeDisplay(dateMillis)
                    }
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
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

                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                binding.editTextBirthDate.setText(dateFormat.format(calendar.time))

                updateAgeDisplay(selectedBirthDateMillis)
            },
            year, month, day
        )
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

    private fun calculateAge(birthDateMillis: Long): Int {
        val today = Calendar.getInstance()
        val birthDate = Calendar.getInstance().apply { timeInMillis = birthDateMillis }
        var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    // Přejmenováno ze saveNewClient na saveClient (protože umí obojí)
    private fun saveClient() {
        val firstName = binding.editTextFirstName.text.toString().trim()
        val lastName = binding.editTextLastName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val notes = binding.editTextNotes.text.toString().trim().ifEmpty { null }

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(requireContext(), "Jméno a příjmení jsou povinná pole.", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (clientToUpdate != null) {
                    // --- 5. LOGIKA PRO UPDATE ---
                    // Vytvoříme kopii původního klienta s novými daty, ale STEJNÝM ID!
                    val updatedClient = clientToUpdate!!.copy(
                        firstName = firstName,
                        lastName = lastName,
                        birthDate = selectedBirthDateMillis,
                        email = email.ifEmpty { null },
                        phone = phone.ifEmpty { null },
                        notes = notes
                    )

                    // Voláme update v repozitáři
                    repository.updateClient(updatedClient)
                    Toast.makeText(requireContext(), "Změny uloženy.", Toast.LENGTH_SHORT).show()

                } else {
                    // --- 6. LOGIKA PRO INSERT (NOVÝ) ---
                    val newClient = Client(
                        id = UUID.randomUUID().toString(), // Generujeme nové ID
                        firstName = firstName,
                        lastName = lastName,
                        birthDate = selectedBirthDateMillis,
                        email = email.ifEmpty { null },
                        phone = phone.ifEmpty { null },
                        notes = notes
                    )
                    repository.addClient(newClient)
                    Toast.makeText(requireContext(), "Klient vytvořen.", Toast.LENGTH_SHORT).show()
                }

                findNavController().popBackStack()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Chyba: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}