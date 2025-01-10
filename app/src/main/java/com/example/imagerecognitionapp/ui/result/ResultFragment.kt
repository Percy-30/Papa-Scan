package com.example.imagerecognitionapp.ui.result

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.data.model.RecognitionResult
import com.example.imagerecognitionapp.databinding.FragmentResultBinding
import com.example.imagerecognitionapp.ui.common.MenuToolbar
import com.example.imagerecognitionapp.ui.diseaseInfo.DiseaseInfoFragment
import com.example.imagerecognitionapp.ui.recognition.RecognitionViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@AndroidEntryPoint
class ResultFragment : Fragment() {

    private var _binding:FragmentResultBinding? = null
    private val binding get() = _binding!!

    // Remove the by viewModels() delegation
    // En el ResultFragment
    private lateinit var viewModel: RecognitionViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var bitmap: Bitmap


    private lateinit var menuHandler: MenuToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startMenu()
        viewModel = ViewModelProvider(requireActivity())[RecognitionViewModel::class.java]
        //viewModel = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[RecognitionViewModel::class.java]
        Log.d("Resultado", "ViewModelFinal ")

        // Recupera el RecognitionResult del Bundle
        //mostrarResultUI()
        final()
        btnDiseaseInfo()

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun final(){
        // Obtener los datos enviados desde RecognitionFragment
        val recognitionResult = arguments?.getSerializable("recognitionResult") as? RecognitionResult
        recognitionResult?.let { result ->
            // Ejemplo: Mostrar los datos en la UI
            binding.tvDiseaseName.text = result.diseaseName
            binding.tvConfidence.text = result.getProbabilityString()  // Assuming you have a method to format probability as string

            // Mostrar el Bitmap en el ImageView
            //binding.imageViewResult.setImageBitmap(bitmap)


            Log.d("Resultado", "Resultado del reconocimiento: ${result.diseaseName}, Probabilidad: ${result.probability}")
        }

        // Usa los datos
        sharedViewModel.compressedImage?.observe(viewLifecycleOwner) { file ->
            binding.imageViewResult.setImageURI(file)
            /*if (file != null) {
                val bitmap = BitmapFactory.decodeFile(file.fragment)
                binding.imageViewResult.setImageBitmap(bitmap)
            }*/
        }

        /*val bitmap = arguments?.getParcelable<Bitmap>("bitmap")
        bitmap?.let {
            val cacheFile = File(requireContext().cacheDir, "temp_data")
            val fos = FileOutputStream(cacheFile)
            it.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()

            // Pasa la ruta del archivo temporal al SharedViewModel
            sharedViewModel.setBitmapPath(cacheFile.absolutePath)
            //sharedViewModel.setBitmap(it)
        }*/


    }

    // Función para guardar el Bitmap en un archivo
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val contextWrapper = ContextWrapper(requireContext())
        val directory = contextWrapper.getDir("images", Context.MODE_PRIVATE)
        val file = File(directory, "image_${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file
    }


    private fun mostrarResultUI() {

        val diseaseName = arguments?.getString("diseaseName")
        val probability = arguments?.getFloat("probability")
        val imageUrl = arguments?.getString("imageUrl")
        val confidenceLevel = arguments?.getString("confidenceLevel")
        val bitmap = arguments?.getParcelable<Bitmap>("bitmap")
        // Reconstruye RecognitionResult

        val recognitionResult = RecognitionResult(
            diseaseName = diseaseName ?: "Desconocido",
            probability = probability ?: 0.0f,
            imageUrl = imageUrl,
            confidenceLevel = confidenceLevel
        )
        // Usa los datos
        binding.tvDiseaseName.text = recognitionResult.diseaseName
        binding.tvConfidence.text = recognitionResult.getProbabilityString()
        bitmap?.let { binding.imageViewResult.setImageBitmap(it) }

    }

    private fun btnDiseaseInfo(){
        binding.btnDiseaseInfo.setOnClickListener {
            val recognitionResult = arguments?.getSerializable("recognitionResult") as? RecognitionResult
            val bitmap = arguments?.getParcelable<Bitmap>("bitmap")

            recognitionResult?.let { result ->
                val bundle = Bundle().apply {
                    putString("detectedDisease", result.diseaseName)
                    putParcelable("bitmap", bitmap) // Pasar el Bitmap
                }
                findNavController().navigate(R.id.action_resultFragment_to_diseaseInfoFragment, bundle)
            }

            /*val recognitionResult = arguments?.getSerializable("recognitionResult") as? RecognitionResult
            recognitionResult?.let { result ->
                val action = ResultFragmentDirections.actionResultFragmentToDiseaseInfoFragment()
                findNavController().navigate(action)
            }*/
        }
    }

    private fun navigateToFragmentDiseaseInfo() {
        findNavController().navigate(R.id.action_resultFragment_to_diseaseInfoFragment)
    }

    //********MENU
    private fun startMenu() {
        menuHandler = MenuToolbar(
            context = requireContext(),
            onHistoryClick = { navigateToHistoryFragment() },
            onAboutClick = { navigateToAlertDialog() },
            onExitClick = { requireActivity().finish() }
        )

        // Configurar la Toolbar
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.appBarMenu.toolbar)
            supportActionBar?.apply {
                title = "Reconocimiento"
                setDisplayHomeAsUpEnabled(true)
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuHandler.onCreateOptionsMenu(menu, inflater)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Solo manejamos el botón de navegación hacia atrás aquí
        // Todo lo demás se delega al menuHandler
        return when (item.itemId) {
            android.R.id.home -> {
                navigateToRecognitionFragment()
                true
            }
            else -> menuHandler.onOptionsItemSelected(item)
        }
    }

    // Navegar hacia el diálogo "History"
    private fun navigateToHistoryFragment(){
        try {
            findNavController().navigate(R.id.action_diseaseInfoFragment_to_historyFragment)
        } catch (e: Exception) {
            Log.e("Navigation", "Error al navegar al diálogo: ${e.message}")
        }
    }


    //*********ALERT DIALOG
    private fun navigateToAlertDialog() {
        try {
            findNavController().navigate(R.id.action_resultFragment_to_fragmentAlertDialog)
        } catch (e: Exception) {
            Log.e("Navigation", "Error navigating to AlertDialog: ${e.message}")
            Toast.makeText(requireContext(), "Error al mostrar el diálogo", Toast.LENGTH_SHORT).show()
        }
    }

    //Pasar de fragment en fragment
    private fun navigateToRecognitionFragment() {
        findNavController().navigate(R.id.action_resultFragment_to_recognitionFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}