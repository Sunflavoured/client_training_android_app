package com.example.client_training_app.ui.profile

import android.app.AlertDialog // <--- !!! NUTNÝ IMPORT PRO DIALOG
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem // <--- !!! NUTNÝ IMPORT
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider // <--- !!! NUTNÝ IMPORT
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle // <--- !!! NUTNÝ IMPORT
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.client_training_app.R // <--- !!! UJISTI SE, ŽE IMPORTUJEŠ SVÉ R
import com.example.client_training_app.data.repository.ClientRepository
import com.example.client_training_app.databinding.FragmentProfileDetailBinding
import com.example.client_training_app.model.Client
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProfileDetailFragment : Fragment() {

    private val args: ProfileDetailFragmentArgs by navArgs()
    private var _binding: FragmentProfileDetailBinding? = null
    private val binding get() = _binding!!

    // Proměnná pro uložení načteného klienta, abychom ho mohli smazat/upravit
    private var currentClient: Client? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clientId = args.clientId

        // 1. Načteme data
        loadClientDetails(clientId)

        // 2. Nastavíme tlačítka (Kalendář, Měření)
        setupButtons(clientId)

        // 3. Nastavíme horní menu (Edit, Delete)
        setupMenu() // <--- !!! OPRAVENO (odstraněn parametr clientId, který funkce nečekala)
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_profile_detail, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit -> {
                        // Pokud currentClient není null, jdeme editovat
                        currentClient?.let { navigateToEdit(it.id) }
                        true
                    }
                    R.id.action_delete -> {
                        // Pokud currentClient není null, vyvoláme dialog smazání
                        currentClient?.let { confirmDelete(it) }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun navigateToEdit(clientId: String) {
        Toast.makeText(context, "Editace klienta (Dodělat)", Toast.LENGTH_SHORT).show()

        val action = ProfileDetailFragmentDirections.actionClientDetailFragmentToAddProfileFragment(clientId)
        findNavController().navigate(action)
    }

    private fun confirmDelete(client: Client) {
        AlertDialog.Builder(requireContext())
            .setTitle("Smazat klienta")
            .setMessage("Opravdu chcete smazat profil ${client.firstName} ${client.lastName}? Tato akce je nevratná.")
            .setPositiveButton("Smazat") { _, _ ->
                deleteClient(client)
            }
            .setNegativeButton("Zrušit", null)
            .show()
    }

    private fun deleteClient(client: Client) {
        val repository = ClientRepository(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            repository.deleteClient(client)
            Toast.makeText(context, "Klient smazán", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun setupButtons(clientId: String) {
        binding.btnCalendar.setOnClickListener {
            // Zkontroluj, zda se akce v nav_graph.xml jmenuje přesně takto
            val action = ProfileDetailFragmentDirections
                .actionClientDetailFragmentToTrainingCalendarFragment(clientId)
            findNavController().navigate(action)
        }

        binding.btnMeasurements.setOnClickListener {
            Toast.makeText(requireContext(), "Měření (brzy)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadClientDetails(clientId: String) {
        val repository = ClientRepository(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            repository.getClientByIdFlow(clientId).collectLatest { client ->
                if (client != null) {
                    // <--- !!! TOTO JE KLÍČOVÉ !!!
                    // Musíme si klienta uložit do proměnné, jinak ho setupMenu neuvidí
                    currentClient = client

                    displayClientData(client)
                } else {
                    activity?.title = "Klient nenalezen"
                    binding.tvClientFullName.text = "Tento profil neexistuje."
                    binding.detailsGroup.visibility = View.GONE
                    // Můžeme také deaktivovat menu, pokud klient neexistuje, ale to není nutné
                }
            }
        }
    }

    private fun displayClientData(client: Client) {
        binding.detailsGroup.visibility = View.VISIBLE
        // Použití safe call ?.let pro zobrazení jména v toolbaru
        activity?.title = "${client.firstName} ${client.lastName}"

        binding.tvClientFullName.text = "${client.firstName} ${client.lastName}"
        binding.tvClientEmail.text = "Email: ${client.email ?: "Není uveden"}"
        binding.tvClientPhone.text = "Telefon: ${client.phone ?: "Není uveden"}"

        val notesText = client.notes ?: "Žádné poznámky."
        binding.tvClientNotes.text = "Poznámky:\n$notesText"

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