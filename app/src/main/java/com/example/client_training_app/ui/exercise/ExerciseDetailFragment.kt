package com.example.client_training_app.ui.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs // Pro Safe Args
import com.example.client_training_app.data.repository.ExerciseRepository
import com.example.client_training_app.databinding.FragmentExerciseDetailBinding
import com.example.client_training_app.model.Exercise // Tvůj model
import com.example.client_training_app.utils.YouTubeUtils
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener // DŮLEŽITÝ IMPORT
import kotlinx.coroutines.launch

class ExerciseDetailFragment : Fragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!

    // Použití Safe Args delegáta (čistší zápis)
    private val args: ExerciseDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Přidání observeru pro YouTube přehrávač (řeší životní cyklus)
        lifecycle.addObserver(binding.youtubePlayerView)

        val exerciseId = args.exerciseId
        val repository = ExerciseRepository(requireContext())

        // 2. Spuštění asynchronního načítání
        viewLifecycleOwner.lifecycleScope.launch {
            val exercise = repository.getExerciseById(exerciseId)

            if (exercise != null) {
                // Teprve TEĎ, když máme data, můžeme volat nastavovací metody
                setupTextData(exercise)
                setupVideo(exercise.mediaUrl)
            } else {
                binding.exerciseName.text = "Cvik nenalezen"
            }
        }
    }

    // Pomocná metoda pro nastavení textů
    private fun setupTextData(exercise: Exercise) {
        binding.exerciseName.text = exercise.name
        // Předpokládám, že ExerciseCategory je Enum a má property displayName
        binding.exerciseCategory.text = exercise.category.displayName
        binding.exerciseDescription.text = exercise.description

        if (exercise.muscleGroups.isEmpty()) {
            binding.cardMuscleGroups.visibility = View.GONE
        } else {
            binding.cardMuscleGroups.visibility = View.VISIBLE
            binding.exerciseMuscleGroups.text = exercise.muscleGroups.joinToString(", ")
        }

        binding.exerciseIsDefault.text = if (exercise.isDefault) "Ano" else "Ne"
    }

    // Pomocná metoda pro nastavení videa
    private fun setupVideo(url: String?) {
        if (url.isNullOrBlank()) {
            binding.youtubePlayerView.visibility = View.GONE
            return
        }

        val videoId = YouTubeUtils.extractVideoId(url)

        if (videoId != null) {
            binding.youtubePlayerView.visibility = View.VISIBLE

            // Tady vznikala chyba s Listenerem - teď už máme import, takže to bude fungovat
            binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    // cueVideo načte video, ale nespustí ho automaticky (šetří data)
                    youTubePlayer.cueVideo(videoId, 0f)
                }
            })
        } else {
            binding.youtubePlayerView.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        // Přehrávač uvolníme PŘEDTÍM, než zničíme binding
        binding.youtubePlayerView.release()
        super.onDestroyView()
        _binding = null
    }
}