package com.example.client_training_app.ui.exercise

import android.os.Bundle
import android.util.Log
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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
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
        viewLifecycleOwner.lifecycle.addObserver(binding.youtubePlayerView)

        val exerciseId = args.exerciseId
        val repository = ExerciseRepository(requireContext())

        // 2. Spuštění asynchronního načítání
        viewLifecycleOwner.lifecycleScope.launch {
            val exercise = repository.getExerciseById(exerciseId)

            if (exercise != null) {
                // Teprve TEĎ, když máme data, můžeme volat nastavovací metody
                setupTextData(exercise)
               // setupVideo(exercise.mediaUrl) TODO: Vyřešit chybu 152
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
    /*private fun setupVideo(url: String?) {
        if (url.isNullOrBlank()) {
            binding.youtubePlayerView.visibility = View.GONE
            return
        }

        val videoId = YouTubeUtils.extractVideoId(url)

        if (videoId != null) {
            binding.youtubePlayerView.visibility = View.VISIBLE

            // 1. Vytvoříme možnosti přehrávače s parametrem ORIGIN
            // "https://www.youtube.com" funguje jako univerzální klíč pro většinu videí
            val iFramePlayerOptions = IFramePlayerOptions.Builder()
                .controls(1)
                .fullscreen(0) // 0 = tlačítko full screen skryté (řešíš ho sama nebo vůbec)
                .origin("https://www.youtube.com") // <--- TOTO OPRAVUJE CHYBU 152
                .build()

            // 2. Inicializujeme přehrávač MANUÁLNĚ s těmito možnostmi
            binding.youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    // videoId z databáze (např. "SCVCLChPQFY")
                    youTubePlayer.cueVideo(videoId, 0f)
                }

                override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                    super.onError(youTubePlayer, error)
                    // Logování chyby pro jistotu
                    android.util.Log.e("YOUTUBE", "Chyba: ${error.name}")
                }
            }, iFramePlayerOptions) // <--- Předáváme options sem

        } else {
            binding.youtubePlayerView.visibility = View.GONE
        }
    }*/

    override fun onDestroyView() {
        // Přehrávač uvolníme PŘEDTÍM, než zničíme binding
        binding.youtubePlayerView.release()
        super.onDestroyView()
        _binding = null
    }
}