package com.example.client_training_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.client_training_app.R
import com.example.client_training_app.databinding.FragmentHomeBinding

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}