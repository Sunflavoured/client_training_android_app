package com.example.client_training_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.client_training_app.data.database.ClientRepository
import com.example.client_training_app.databinding.FragmentClientDetailBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.fragment.findNavController

class ClientDetailFragment : Fragment() {

    private val args: ClientDetailFragmentArgs by navArgs()
    private var _binding: FragmentClientDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = FragmentClientDetailBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clientId = args.clientId
        loadClientDetails(clientId)
        setupButtons(clientId)
        // TODO: Nastavení posluchačů pro přechod na Kalendář, Měření atd.
    }
    private fun setupButtons(clientId: String) {
        // Tlačítko Kalendář - OŽIVENO!
        binding.btnCalendar.setOnClickListener {
            // Použijeme Safe Args pro bezpečné předání ID
            val action = ClientDetailFragmentDirections
                .actionClientDetailFragmentToTrainingCalendarFragment(clientId)

            // Provedeme navigaci
            findNavController().navigate(action)
        }

        // Tlačítko Měření (zatím necháme Toast)
        binding.btnMeasurements.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Měření (brzy)", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadClientDetails(clientId: String) {
        val repository = ClientRepository(requireContext())

        // POUŽITÍ FLOW: Blok se spustí vždy, když se data v DB změní
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getClientByIdFlow(clientId).collectLatest { client ->
                if (client != null) {
                    displayClientData(client)
                } else {
                    activity?.title = "Klient nenalezen"
                    binding.tvClientFullName.text = "Tento profil neexistuje."
                    // Skrytí prvků při nenalezení
                    binding.detailsGroup.visibility = View.GONE
                }
            }
        }
    }

    private fun displayClientData(client: com.example.client_training_app.model.Client) {
        // Zobrazení detailů (pokud existují)
        binding.detailsGroup.visibility = View.VISIBLE
        activity?.title = "${client.firstName} ${client.lastName}"

        binding.tvClientFullName.text = "${client.firstName} ${client.lastName}"
        binding.tvClientEmail.text = "Email: ${client.email ?: "Není uveden"}"
        binding.tvClientPhone.text = "Telefon: ${client.phone ?: "Není uveden"}"

        val notesText = client.notes ?: "Žádné poznámky."
        binding.tvClientNotes.text = "Poznámky:\n$notesText"

        // Datum narození / Věk
        client.birthDate?.let { birthDate ->
            val age = calculateAge(birthDate)
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            binding.tvClientBirthDate.text = "Datum narození: ${dateFormat.format(Date(birthDate))} ($age let)"
            binding.tvClientBirthDate.visibility = View.VISIBLE
        } ?: run {
            binding.tvClientBirthDate.visibility = View.GONE
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}