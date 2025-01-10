package com.example.imagerecognitionapp.ui.diseaseInfo

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
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

    //private val historyViewModel: HistoryViewModel by activityViewModels()
    private lateinit var historyViewModel: HistoryViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var isSaving = false // Variable para controlar si se está guardando
    private lateinit var section:String
    private var isObservingHistoryItem = false
    private var isAddingToHistory = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDiseaseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        historyViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtén el nombre de la enfermedad y la sección de los argumentos
        //val diseaseName = arguments?.getString("diseaseName") ?: "Desconocida"
        //val section = arguments?.getString("section") ?: "Sin sección"

        val diseaseName = arguments?.getString("diseaseName") ?: return
        section = arguments?.getString("section") ?: return

        // Observe shared bitmap
        /*sharedViewModel.bitmap.observe(viewLifecycleOwner) { bitmap ->
            bitmap?.let { processBitmap(it, diseaseName) }
        }*/
        // Observe shared bitmap path
        /*sharedViewModel.bitmapPath.observe(viewLifecycleOwner) { bitmapPath ->
            bitmapPath?.let {
                val bitmap = BitmapFactory.decodeFile(bitmapPath)
                bitmap?.let { processBitmap(it, diseaseName) }
            }
        }*/

        sharedViewModel.compressedImage?.observe(viewLifecycleOwner) { file ->
            if (file != null) {
                /*lifecycleScope.launch {
                    val lastId = historyViewModel.getLastId()
                    if (lastId != null) {
                        val existingHistory = historyViewModel.getHistoryById(lastId)
                        if (existingHistory != null) {
                            // Ya existe un registro con el mismo ID, no guardar
                            return@launch
                        }
                    }*/
                    // No existe un registro con el mismo ID, procesar la imagen
                    processImage(file, diseaseName)
               // }
            }
            /*if (file != null) {
                val bitmap = BitmapFactory.decodeFile(file.fragment)
                bitmap?.let { processBitmap(it, diseaseName) }
            }*/
        }

        // Observe selected history item
        sharedViewModel.selectedHistoryItem.distinctUntilChanged().observe(viewLifecycleOwner) { history ->
            history?.let { updateUIWithHistoryDetails(it, section) }
            //sharedViewModel.compressedImage?.removeObservers(viewLifecycleOwner)
        }

        // Observa el item seleccionado del historial

    }

    private fun processImage(uri: Uri, diseaseName: String) {
        // First check if entry exists
        lifecycleScope.launch {
        val existingHistory = historyViewModel.getHistoryByDiseaseAndSection(diseaseName)

        if (existingHistory != null) {
            // Use existing entry
            updateUIWithDiseaseInfo(
                DiseaseInfo(
                    name = diseaseName,
                    description = existingHistory.description,
                    prevention = existingHistory.prevention,
                    causes = existingHistory.causes,
                    treatment = existingHistory.treatment
                ),
                section
            )
            return@launch
        }

        // If no existing entry, proceed with new entry creation
        val imagePath = saveImageUriToFile(uri)
        val diseaseInfo = getDiseaseInfo(diseaseName)

        val history = History(
            diseaseName = diseaseName,
            section = "main",
            description = diseaseInfo.description,
            prevention = diseaseInfo.prevention,
            causes = diseaseInfo.causes,
            treatment = diseaseInfo.treatment,
            timestamp = System.currentTimeMillis(),
            imagePath = imagePath
        )

        historyViewModel.addToHistory(history)
        updateUIWithDiseaseInfo(diseaseInfo, section)
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

    private fun processBitmap(bitmap: Bitmap, diseaseName: String) {
        lifecycleScope.launch {
            val imagePath = saveBitmapToFile(bitmap)
            val diseaseInfo = getDiseaseInfo(diseaseName)

            val history = History(
                diseaseName = diseaseName,
                section = "main", // Single entry per disease
                description = diseaseInfo.description,
                prevention = diseaseInfo.prevention,
                causes = diseaseInfo.causes,
                treatment = diseaseInfo.treatment,
                timestamp = System.currentTimeMillis(),
                imagePath = imagePath
            )

            historyViewModel.addToHistory(history)
            //updateUIWithDiseaseInfo(diseaseInfo)
            updateUIWithDiseaseInfo(diseaseInfo, section)

        }
    }

    private fun updateUIWithHistoryDetails(history: History, currentSection: String) {
        binding.apply {
            textSectionTitle.text = currentSection
            textSectionContent.text = when (currentSection) {
                "Enfermedad" -> history.description
                "Tratamiento" -> history.treatment
                "Causas" -> history.causes
                "Prevención" -> history.prevention
                else -> "Información no disponible"
            }
        }
    }
    private fun updateUIWithDiseaseInfo(diseaseInfo: DiseaseInfo, section: String) {
        binding.apply {
            textSectionTitle.text = section
            textSectionContent.text = when (section) {
                "Enfermedad" -> diseaseInfo.description
                "Tratamiento" -> diseaseInfo.treatment
                "Causas" -> diseaseInfo.causes
                "Prevención" -> diseaseInfo.prevention
                else -> "Información no disponible"
            }
        }
    }

    private fun updateUIWithDiseaseInfo(diseaseInfo: DiseaseInfo) {
        binding.apply {
            // Since we're working with a single view setup based on sections
            //textSectionContent.text = when (textSectionTitle.text.toString()) {
            textSectionContent.text = when (textSectionTitle.text.toString()) {
                "Enfermedad" -> diseaseInfo.description
                "Tratamiento" -> diseaseInfo.treatment
                "Causas" -> diseaseInfo.causes
                "Prevención" -> diseaseInfo.prevention
                else -> "Información no disponible"
            }
        }
    }

    private suspend fun getDiseaseInfo(diseaseName: String): DiseaseInfo {
        return withContext(Dispatchers.IO) {
            diseaseDatabase[diseaseName] ?: DiseaseInfo(
                name = "Desconocida",
                description = "Descripción no disponible",
                prevention = "Prevención no disponible",
                causes = "Cuasas No disponible",
                treatment = "Tratamiento no disponible"
            )
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap?): String? {
        if (bitmap == null) {
            Log.e("SaveBitmap", "Bitmap nulo, no se puede guardar.")
            return null
        }
        val contextWrapper = ContextWrapper(requireContext())
        val directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE)
        val fileName = "IMG_${UUID.randomUUID()}.jpg"
        val file = File(directory, fileName)

        return try {
            val outputStream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            file.absolutePath
        } catch (e: IOException) {
            Log.e("SaveBitmap", "Error al guardar la imagen", e)
            null
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

       // sharedViewModel.compressedImage?.removeObservers(viewLifecycleOwner)
        //weakBitmap?.get()?.recycle()  // Libera la memoria del bitmap si es necesario
        //weakBitmap = null
    }

    companion object {
        fun newInstance(diseaseName: String, section: String): DiseaseDetailFragment {
            val fragment = DiseaseDetailFragment()
            val args = Bundle().apply {
                putString("diseaseName", diseaseName)
                putString("section", section)
            }
            fragment.arguments = args
            return fragment
        }
    }
}

