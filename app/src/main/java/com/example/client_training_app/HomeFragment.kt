package com.example.client_training_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.client_training_app.data.database.ClientRepository
import com.example.client_training_app.data.database.ExerciseRepository
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
        return binding.root
    }

    private fun setupClickListeners() {

        // Navigate to Profiles
        binding.cardProfiles.setOnClickListener {
            findNavController().navigate(R.id.profilesFragment)
        }

        // Navigate to Exercises
        binding.cardExercises.setOnClickListener {
            findNavController().navigate(R.id.exercisesFragment)
        }
    }

    // FUNKCE: Načte počet cviků
    private fun loadExerciseCount() {
        val ExerciseRepository = ExerciseRepository(requireContext())

        // 1. Spustíme coroutine, která je vázaná na životní cyklus fragmentu.
        //    Poběží jen, když je fragment "naživu".
        viewLifecycleOwner.lifecycleScope.launch {
            // 2. Napojíme se na Flow a "sbíráme" data.
            //    Tento blok se spustí, jakmile přijdou data z databáze.
            ExerciseRepository.getAllExercisesFlow().collect { exerciseList ->
                // 3. 'exerciseList' je nyní SKUTEČNÝ List<Exercise>!
                //    Teď už můžeme bezpečně získat jeho velikost.
                val exerciseCount = exerciseList.size
                // 4. Zobrazíme počet v UI.
                //    Toto musí být také uvnitř 'collect', protože 'exerciseCount'
                //    existuje pouze tady, až po doručení dat.
                binding.tvExerciseCount.text = exerciseCount.toString()
            }
        }
    }

    private fun loadProfileCount() {
        val clientRepository = ClientRepository(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            clientRepository.getAllClientsFlow().collect { clientList ->
                val profileCount = clientList.size
                binding.tvClientCount.text = profileCount.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}