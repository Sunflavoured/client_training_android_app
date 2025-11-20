package com.example.client_training_app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.client_training_app.data.database.ClientRepository
import com.example.client_training_app.model.Client
import com.example.client_training_app.databinding.FragmentProfilesBinding // Předpokládá se existující layout
import com.example.client_training_app.ui.ClientAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfilesFragment : Fragment() {

    private var _binding: FragmentProfilesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ClientAdapter
    private lateinit var repository: ClientRepository

    private var allClients: List<Client> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilesBinding.inflate(inflater, container, false)
        repository = ClientRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFAB() // Tlačítko pro přidání klienta
        observeClients() // Sledování Flow dat
        setupSearchBar()
    }

    private fun setupRecyclerView() {
        adapter = ClientAdapter(emptyList()) { client ->
            // TODO: Akce pro zobrazení detailu klienta
            // Budeš potřebovat definovat akci v nav_graph.xml:
            // ProfilesFragmentDirections.actionProfilesFragmentToClientDetailFragment(client.id)
            android.widget.Toast.makeText(requireContext(), "Kliknuto na ${client.firstName}", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.profileRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.profileRecyclerView.adapter = adapter
    }

    private fun setupFAB() {
        // PŘEDPOKLAD: Máš v nav_graph.xml akci pro přechod na přidání klienta (např. AddClientFragment)
        binding.fabAddProfile.setOnClickListener {
            // findNavController().navigate(R.id.action_profilesFragment_to_addClientFragment)
            android.widget.Toast.makeText(requireContext(), "Navigace na přidání klienta", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeClients() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllClientsFlow().collectLatest { clients ->
                allClients = clients
                // Aplikujeme aktuální vyhledávací dotaz, pokud nějaký existuje
                filterClients(binding.searchEditText.text.toString())
            }
        }
    }

    private fun setupSearchBar() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterClients(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterClients(query: String) {
        val filteredList = if (query.isEmpty()) {
            allClients
        } else {
            // Hledání klientů podle jména nebo příjmení
            allClients.filter { client ->
                client.firstName.contains(query, ignoreCase = true) ||
                        client.lastName.contains(query, ignoreCase = true)
            }
        }
        updateUI(filteredList)
    }

    private fun updateUI(clients: List<Client>) {
        if (clients.isEmpty()) {
            binding.profileRecyclerView.visibility = View.GONE
            binding.emptyStateTextView.visibility = View.VISIBLE
        } else {
            binding.profileRecyclerView.visibility = View.VISIBLE
            binding.emptyStateTextView.visibility = View.GONE
            adapter.updateClients(clients)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}