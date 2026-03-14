package com.example.client_training_app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.client_training_app.data.repository.ClientRepository
import com.example.client_training_app.databinding.FragmentAddMeasurementBinding
import com.example.client_training_app.model.Measurement
import kotlinx.coroutines.launch

// POZNÁMKA: Musíš definovat argument v nav_graph.xml pro tento fragment!
class AddMeasurementFragment : Fragment() {

    // Předpoklad: nav_graph má argument 'clientId'
    private val args: AddMeasurementFragmentArgs by navArgs()

    private var _binding: FragmentAddMeasurementBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ClientRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMeasurementBinding.inflate(inflater, container, false)
        repository = ClientRepository(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSaveMeasurement.setOnClickListener {
            saveNewMeasurement()
        }

        // Volitelné: Zobrazení jména klienta, pokud ho získáš z DB nebo jako argument
        // binding.textViewClientName.text = "Přidat měření pro klienta ID: ${args.clientId}"
    }

    private fun saveNewMeasurement() {
        val clientId = args.clientId

        // Funkce pro bezpečné získání Double hodnoty z EditTextu
        fun getDoubleOrNull(text: String): Double? {
            return text.trim().toDoubleOrNull()
        }

        val weight = getDoubleOrNull(binding.editTextWeight.text.toString())
        val bustCm = getDoubleOrNull(binding.editTextBustCm.text.toString())
        val chestCm = getDoubleOrNull(binding.editTextChestCm.text.toString())
        val waistCm = getDoubleOrNull(binding.editTextWaistCm.text.toString())
        val abdomenCm = getDoubleOrNull(binding.editTextAbdomenCm.text.toString())
        val hipsCm = getDoubleOrNull(binding.editTextHipsCm.text.toString())
        val thighCm = getDoubleOrNull(binding.editTextThighCm.text.toString())
        val armCm = getDoubleOrNull(binding.editTextArmCm.text.toString())

        // Kontrola, zda bylo zadáno alespoň jedno měření
        if (weight == null && bustCm == null && chestCm == null && waistCm == null &&
            abdomenCm == null && hipsCm == null && thighCm == null && armCm == null) {
            Toast.makeText(requireContext(), "Zadejte alespoň jednu hodnotu pro uložení měření.", Toast.LENGTH_LONG).show()
            return
        }

        val newMeasurement = Measurement(
            clientId = clientId,
            date = System.currentTimeMillis(),
            weight = weight,
            bustCm = bustCm,
            chestCm = chestCm,
            waistCm = waistCm,
            abdomenCm = abdomenCm,
            hipsCm = hipsCm,
            thighCm = thighCm,
            armCm = armCm
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.addMeasurement(newMeasurement)
                Toast.makeText(requireContext(), "Nové měření uloženo.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Chyba při ukládání měření: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}