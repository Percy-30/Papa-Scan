package com.example.imagerecognitionapp.ui.diseaseInfo

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.data.diseaseName.DiseaseInfo
import com.example.imagerecognitionapp.data.diseaseName.DiseaseInfoPagerAdapter
import com.example.imagerecognitionapp.data.diseaseName.diseaseDatabase
import com.example.imagerecognitionapp.databinding.FragmentDiseaseInfoBinding
import com.example.imagerecognitionapp.databinding.FragmentRecognitionBinding
import com.example.imagerecognitionapp.ui.common.MenuToolbar
import com.example.imagerecognitionapp.ui.history.HistoryViewModel
import com.example.imagerecognitionapp.ui.recognition.RecognitionViewModel
import com.example.imagerecognitionapp.ui.result.SharedViewModel
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.util.UUID

class DiseaseInfoFragment : Fragment() {
    private var _binding: FragmentDiseaseInfoBinding? = null
    private val binding get() = _binding!!

    private var weakBitmap: WeakReference<Bitmap>? = null

    // ViewModel compartido
    private val historyViewModel: HistoryViewModel by activityViewModels()
    private lateinit var menuHandler: MenuToolbar
    // En el ResultFragment
    private lateinit var viewModel: RecognitionViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var diseaseName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDiseaseInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startMenu()
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {

        /*sharedViewModel.bitmap.observe(viewLifecycleOwner) { bitmap ->
            bitmap?.let {
                binding.imageDisease.setImageBitmap(it)
            }
        }*/
        /*sharedViewModel.bitmapPath.observe(viewLifecycleOwner) { bitmapPath ->
           bitmapPath?.let {
               val bitmap = BitmapFactory.decodeFile(bitmapPath)
               binding.imageDisease.setImageBitmap(bitmap)
           }
       }*/
        sharedViewModel.compressedImage?.observe(viewLifecycleOwner) { uri ->
            binding.imageDisease.setImageURI(uri)
            //val bitmap = BitmapFactory.decodeFile(uri?.path)
            //bitmap?.recycle()  // Libera la memoria del Bitmap
            /*if (file != null) {
                Log.d("IMAGEN", file.toString())
                val bitmap = BitmapFactory.decodeFile(file.fragment)
                binding.imageDisease.setImageBitmap(bitmap)
                bitmap.recycle()
            }*/
        }



        sharedViewModel.selectedHistoryItem.observe(viewLifecycleOwner){ historyItem ->
            val diseaseInfo = historyItem.diseaseName
            val section = historyItem.section
            val image = historyItem.imagePath
            diseaseInfo?.let {
                binding.textDiseaseTitle.text = diseaseInfo.toString()
                //binding.imageDisease.setImageBitmap(saveBitmapToFile(diseaseInfo.imageUri))
            }
            image?.let {
                //val uri = Uri.parse(it)
                val file = File(it)
                val uri = Uri.fromFile(file) // Convertir el File en un Uri
                Log.d("RUTA", uri.toString()) // Verifica que la URI sea correcta
                //binding.imageDisease.setImageURI(uri) // Si es una URI válida
                // Redimensionar la imagen antes de cargarla en el ImageView
                loadImageEfficiently(uri)
            }
        }

        val diseaseName = arguments?.getString("detectedDisease") ?: "Desconocido"
        //val bitmap = arguments?.getParcelable<Bitmap>("bitmap")

        binding.apply {
            textDiseaseTitle.text = diseaseName
            //bitmap?.let { imageDisease.setImageBitmap(it) }

            viewPager.adapter = DiseaseInfoPagerAdapter(this@DiseaseInfoFragment, diseaseName)
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Enfermedad"
                    1 -> "Tratamiento"
                    2 -> "Causas"
                    3 -> "Prevención"
                    else -> "Otro"
                }
            }.attach()
        }

        // Guardar en historial
        /*historyViewModel.addToHistory(
            diseaseName = diseaseName,
            section = "Diagnóstico",
            imageUri = saveBitmapToFile(bitmap)?.toString()
        )*/

    }

    fun loadImageEfficiently(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .override(800, 600)  // Redimensiona la imagen antes de cargarla
            .into(binding.imageDisease)
    }


    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val contextWrapper = ContextWrapper(requireContext())
        val directory = contextWrapper.getDir("images", Context.MODE_PRIVATE)
        val file = File(directory, "image_${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file
    }

    private fun observeViewModel() {
        //historyViewModel.currentDisease.observe(viewLifecycleOwner) { diseaseInfo ->
            // Actualizar UI con la información de la enfermedad
        //}
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
                navigateToPreviousFragment()
                true
            }
            else -> menuHandler.onOptionsItemSelected(item)
        }
    }

    // Navegar hacia el fragmento anterior
    private fun navigateToPreviousFragment() {
        findNavController().navigate(R.id.action_diseaseInfoFragment_to_recognitionFragment)
    }

    // Navegar hacia el diálogo "Acerca de"
    private fun navigateToAlertDialog() {
        try {
            findNavController().navigate(R.id.action_diseaseInfoFragment_to_fragmentAlertDialog)
        } catch (e: Exception) {
            Log.e("Navigation", "Error al navegar al diálogo: ${e.message}")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // asegúrate de liberar la memoria aquí también.
        //sharedViewModel.compressedImage?.removeObservers(viewLifecycleOwner)
        //weakBitmap?.get()?.recycle()  // Libera la memoria del bitmap si es necesario
        //weakBitmap = null
    }

/*private fun checkInternetConnectionAndFetchInfo() {
    // Verifica si hay conexión a Internet
    if (isNetworkAvailable()) {
        // Si hay conexión, haz una consulta a la API para obtener información adicional
        fetchDiseaseInfoFromAPI()
    } else {
        // Muestra un mensaje indicando que no hay conexión
        Toast.makeText(context, "No hay conexión a Internet", Toast.LENGTH_SHORT).show()
    }
}

/*private fun isNetworkAvailable(): Booln {
    val connectivityManager =
        requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

private fun fetchDiseaseInfoFromAPI() {
    // Aquí podrías usar una API para obtener más información sobre la enfermedad
    // Puedes usar Retrofit, OkHttp o cualquier otra librería para hacer la consulta HTTP

    val apiService = ApiClient.getApiService()
    apiService.getDiseaseInfo().enqueue(object : Callback<DiseaseInfo> {
        override fun onResponse(call: Call<DiseaseInfo>, response: Response<DiseaseInfo>) {
            if (response.isSuccessful) {
                val diseaseInfo = response.body()
                // Actualiza la UI con la nueva información
                diseaseInfo?.let {
                    diseaseDescription = it.description
                    preventionTips = it.prevention
                    treatmentTips = it.treatment
                    updateUI()
                }
            }
        }

        override fun onFailure(call: Call<DiseaseInfo>, t: Throwable) {
            Toast.makeText(context, "Error al obtener datos", Toast.LENGTH_SHORT).show()
        }
    })
}*/*/
}

