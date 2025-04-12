package com.example.imagerecognitionapp.ui.diseaseInfo

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import com.example.imagerecognitionapp.data.diseaseName.DiseaseInfo
import com.example.imagerecognitionapp.data.diseaseName.diseaseDatabase
import com.example.imagerecognitionapp.data.model.History
import com.example.imagerecognitionapp.databinding.FragmentDiseaseDetailBinding
import com.example.imagerecognitionapp.ui.history.HistoryViewModel
import com.example.imagerecognitionapp.ui.result.SharedViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.UUID

@AndroidEntryPoint
class DiseaseDetailFragment : Fragment() {

    private var _binding: FragmentDiseaseDetailBinding? = null
    private val binding get() = _binding!!

    private var weakBitmap: WeakReference<Bitmap>? = null

    private val historyViewModel: HistoryViewModel by activityViewModels()
    //private lateinit var historyViewModel: HistoryViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var section: Section
    private lateinit var diseaseName: String
    private var position: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDiseaseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt("position", 0) ?: 0
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInitialData()
        observeViewModel()
    }

    private fun setupInitialData() {
        diseaseName = arguments?.getString("diseaseName") ?: return
        val sectionTitle = arguments?.getString("section") ?: return
        section = Section.fromTitle(sectionTitle) ?: return
        Log.d("DiseaseDetailFragment", "Initial section: $section")

        // Obtén la información de la enfermedad
        val diseaseInfo = getDiseaseInfo(diseaseName)
        if (diseaseInfo != null) {
            updateUIWithDiseaseInfo(diseaseInfo, section)
        } else {
            Log.e("DiseaseDetailFragment", "DiseaseInfo is null")
        }

    }

    private fun observeViewModel() {
        // Observar la imagen comprimida solo si no ha sido procesada
        sharedViewModel.isImageProcessed.observe(viewLifecycleOwner) { isProcessed ->
            if (!isProcessed) {
                sharedViewModel.compressedImage?.observe(viewLifecycleOwner) { file ->
                    file?.let {
                        processImage(it, diseaseName)
                        sharedViewModel.setImageProcessed(true)
                        sharedViewModel.compressedImage?.removeObservers(viewLifecycleOwner)
                    }
                }
            }
        }

        // Observar el ítem de historial seleccionado
        sharedViewModel.selectedHistoryItem.distinctUntilChanged().observe(viewLifecycleOwner) { history ->
            if (history != null) {
                Log.d("DiseaseDetailFragment", "Updating UI with history: $history")
                updateUIWithHistoryDetails(history)
            } else {
                Log.d("DiseaseDetailFragment", "History item is not relevant for the current disease")
                // No actualizar la UI si el ítem del historial no es relevante
            }
        }
    }

    private fun updateUIWithHistoryDetails(history: History) {
        binding.apply {
            textSectionTitle.text = section.title
            //textSectionContent.text = when (section) {
            val text = when (section) {
                Section.Enfermedad -> history.description
                Section.Tratamiento -> history.treatment
                Section.Causas -> history.causes
                Section.Prevencion -> history.prevention
                else -> "Información no disponible"
            }
            textSectionContent.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
            Log.d("DiseaseDetailFragment", "Updated UI: ${textSectionContent.text}")
        }
    }

    private fun processImage(uri: Uri, diseaseName: String) {
        // Primero verifica si la entrada existe
        val imagePath = saveImageUriToFile(uri)
        if (imagePath != null) {
            val diseaseInfo = getDiseaseInfo(diseaseName)
            Log.d("DiseaseDetailFragment", "La informacion $diseaseInfo")
            updateUIWithDiseaseInfo(DiseaseInfo(
                name = diseaseInfo.name,
                description = diseaseInfo.description,
                prevention = diseaseInfo.prevention,
                causes = diseaseInfo.causes,
                treatment = diseaseInfo.treatment
            ),
                section
            )

            val history = History(
                diseaseName = diseaseName,
                section = "PapaScan",
                description = diseaseInfo.description,
                prevention = diseaseInfo.prevention,
                causes = diseaseInfo.causes,
                treatment = diseaseInfo.treatment,
                timestamp = System.currentTimeMillis(),
                imagePath = imagePath
            )

            historyViewModel.addToHistory(history)

        } else {
            Log.e("DiseaseDetailFragment", "No se pudo guardar la imagen.")
        }
    }

    private fun saveImageUriToFile(uri: Uri): String? {
        val contextWrapper = ContextWrapper(requireContext())
        val directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE)
        val fileName = "IMG_${UUID.randomUUID()}.jpg"
        val file = File(directory, fileName)

        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val outputStream: OutputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            outputStream.flush()
            outputStream.close()
            inputStream?.close()
            file.absolutePath
        } catch (e: IOException) {
            Log.e("SaveImage", "Error al guardar la imagen desde el Uri", e)
            null
        }
    }
    private fun updateUIWithDiseaseInfo(diseaseInfo: DiseaseInfo, section: Section) {
        Log.d("DiseaseDetailFragment", "Updating UI with section: ${section.title} and diseaseInfo: $diseaseInfo")

        binding.apply {
            textSectionTitle.text = section.title
            //textSectionContent.text = getSectionContent(diseaseInfo, section)
            textSectionContent.text = Html.fromHtml(getSectionContent(diseaseInfo, section), Html.FROM_HTML_MODE_COMPACT)
            Log.d("DiseaseDetailFragment", "Updated UI: ${textSectionContent.text}")
        }
    }

    private fun getSectionContent(diseaseInfo: DiseaseInfo, section: Section): String {
        return when (section) {
            Section.Enfermedad -> diseaseInfo.description
            Section.Tratamiento -> diseaseInfo.treatment
            Section.Causas -> diseaseInfo.causes
            Section.Prevencion -> diseaseInfo.prevention
            else -> "Información no disponible"
        }
    }

    /*private fun updateUIWithDiseaseInfo(diseaseInfo: DiseaseInfo, section: Section) {
        Log.d("DiseaseDetailFragment", "Updating UI with section: ${section.title} and diseaseInfo: $diseaseInfo")
        binding.apply {
            textSectionTitle.text = section.title
            textSectionContent.text = when (section) {
                is Section.Enfermedad -> diseaseInfo.description
                is Section.Tratamiento -> diseaseInfo.treatment
                is Section.Causas -> diseaseInfo.causes
                is Section.Prevencion -> diseaseInfo.prevention
                else -> "Información no disponible"
            }
            Log.d("DiseaseDetailFragment","Updated UI: ${textSectionContent.text}")
        }
    }*/

    private fun getDiseaseInfo(diseaseName: String): DiseaseInfo {
        // Aquí debes asegurarte de que la enfermedad tenga un tipo asociado.
        return diseaseDatabase[diseaseName] ?: DiseaseInfo(
            name = "Desconocida",
            description = "Descripción no disponible",
            prevention = "Prevención no disponible",
            causes = "Causas no disponible",
            treatment = "Tratamiento no disponible"
            // Asegúrate de asignar un tipo por defecto
        )
    }


    override fun onDestroyView() {
        cleanupResources()
        super.onDestroyView()

        // sharedViewModel.compressedImage?.removeObservers(viewLifecycleOwner)
        //weakBitmap?.get()?.recycle()  // Libera la memoria del bitmap si es necesario
        //weakBitmap = null
    }
    private fun cleanupResources() {
        try {
            // 1. Limpiar recursos de ViewModel compartido
            sharedViewModel.clearSelectedHistory()
            //sharedViewModel.setImageProcessed(false) // Resetear estado de imagen procesada

            // 2. Remover observadores
            clearObservers()

            // 3. Limpiar referencias de bitmap
            clearBitmapResources()

            // 4. Limpiar referencias de UI
            binding.textSectionContent.text = null

            // 5. Liberar binding (siempre al final)
            _binding = null

            Log.d("DiseaseDetailFragment", "All resources cleaned up successfully")
        } catch (e: Exception) {
            Log.e("DiseaseDetailFragment", "Error during cleanup", e)
        }
    }

    private fun clearObservers() {
        try {
            // Remover todos los observadores del ViewModel compartido
            sharedViewModel.isImageProcessed.removeObservers(viewLifecycleOwner)
            sharedViewModel.compressedImage?.removeObservers(viewLifecycleOwner)
            sharedViewModel.selectedHistoryItem.removeObservers(viewLifecycleOwner)
        } catch (e: Exception) {
            Log.e("DiseaseDetailFragment", "Error clearing observers", e)
        }
    }

    private fun clearBitmapResources() {
        try {
            // Liberar bitmap si existe
            weakBitmap?.get()?.recycle()
            weakBitmap = null

            // Limpiar cualquier caché de imágenes
            System.gc() // Sugerir garbage collection para bitmaps grandes
        } catch (e: Exception) {
            Log.e("DiseaseDetailFragment", "Error clearing bitmap resources", e)
        }
    }


    companion object {
        fun newInstance(diseaseName: String, section: String, position: Int): DiseaseDetailFragment {
            val fragment = DiseaseDetailFragment()
            val args = Bundle().apply {
                putString("diseaseName", diseaseName)
                putString("section", section)
                putInt("position", position)
            }
            fragment.arguments = args
            return fragment
        }
    }
}