package com.example.client_training_app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.client_training_app.R
import com.example.client_training_app.data.repository.ClientRepository
import com.example.client_training_app.data.repository.ExerciseRepository
import com.example.client_training_app.data.repository.TrainingUnitRepository // 1. Import repository
import com.example.client_training_app.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupClickListeners()
        loadExerciseCount()
        loadProfileCount()
        loadTrainingUnitCount() // 2. ZAVOLAT metodu při startu
        return binding.root
    }

    private fun setupClickListeners() {
        binding.cardProfiles.setOnClickListener {
            findNavController().navigate(R.id.profilesFragment)
        }

        binding.cardExercises.setOnClickListener {
            findNavController().navigate(R.id.exercisesFragment)
        }

        binding.cardTrainingUnits.setOnClickListener {
            findNavController().navigate(R.id.trainingUnitLibraryFragment)
        }
    }

    private fun loadExerciseCount() {
        val exerciseRepository = ExerciseRepository(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            exerciseRepository.getAllExercisesFlow().collect { exerciseList ->
                binding.tvExerciseCount.text = exerciseList.size.toString()
            }
        }
    }

    private fun loadProfileCount() {
        val clientRepository = ClientRepository(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            clientRepository.getAllClientsFlow().collect { clientList ->
                binding.tvClientCount.text = clientList.size.toString()
            }
        }
    }

    // 3. OPRAVENÁ METODA PRO TRÉNINKY
    private fun loadTrainingUnitCount() {
        // Inicializujeme správný repository
        val trainingRepository = TrainingUnitRepository(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            // Voláme metodu pro získání globálních (šablonových) tréninků
            // Ujisti se, že tuto metodu máš v Repository (viz bod níže)
            trainingRepository.getAllUnitsFlow().collect { unitsList ->
                val count = unitsList.size
                // Předpokládám, že máš v XML TextView s ID tvTrainingUnitCount
                binding.tvUnitCount.text = count.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}