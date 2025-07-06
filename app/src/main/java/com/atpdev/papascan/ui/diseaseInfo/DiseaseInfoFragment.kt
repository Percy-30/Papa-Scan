package com.atpdev.papascan.ui.diseaseInfo

import android.graphics.Bitmap
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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.atpdev.papascan.R
import com.atpdev.papascan.data.diseaseName.DiseaseInfoPagerAdapter
import com.atpdev.papascan.databinding.FragmentDiseaseInfoBinding
import com.atpdev.papascan.ui.common.MenuToolbar
import com.atpdev.papascan.ui.dialog.FragmentAlertDialogExit
import com.atpdev.papascan.ui.history.HistoryViewModel
import com.atpdev.papascan.ui.recognition.RecognitionViewModel
import com.atpdev.papascan.ui.result.SharedViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.WeakReference

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

        sharedViewModel.compressedImage?.observe(viewLifecycleOwner) { uri ->
            //binding.imageDisease.setImageURI(uri)
            uri?.let {
                lifecycleScope.launch {
                    loadImageEfficientlyPicaso(it)
                }
            } ?: run {
                Log.d("DiseaseInfoFragment", "No hay una imagen comprimida disponible")
                // Aquí puedes manejar el caso en que no haya una imagen comprimida
            }
        }



        sharedViewModel.selectedHistoryItem.observe(viewLifecycleOwner){ historyItem ->
            historyItem?.let {
                Log.d("DiseaseInfoFragment", "Seleccionado un elemento del historial: $historyItem")
                val diseaseInfo = historyItem.diseaseName
                val section = historyItem.section
                val image = historyItem.imagePath

                diseaseInfo?.let { binding.textDiseaseTitle.text = it }

                image?.let {
                    val file = File(it)
                    val uri = Uri.fromFile(file)
                    Log.d("RUTA", uri.toString())
                    loadImageEfficientlyGlide(uri)
                }
            } ?: run {
                Log.d("DiseaseInfoFragment", "No hay un ítem del historial seleccionado")
                // Aquí puedes manejar el caso en que no haya un ítem del historial seleccionado
            }

        }



        // Guardar en historial
        /*historyViewModel.addToHistory(
            diseaseName = diseaseName,
            section = "Diagnóstico",
            imageUri = saveBitmapToFile(bitmap)?.toString()
        )*/

    }

    private fun loadImageEfficientlyGlide(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .override(800, 600)  // Redimensiona la imagen antes de cargarla
            .into(binding.imageDisease)
    }

    private fun loadImageEfficientlyPicaso(uri: Uri) {
        Picasso.get()
            .load(uri)
            .resize(800, 600)  // Redimensiona la imagen antes de cargarla
            .into(binding.imageDisease)
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
            onExitClick = { showExitConfirmationDialog() }
            //onExitClick = { requireActivity().finish() }
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

    private fun showExitConfirmationDialog() {
        FragmentAlertDialogExit.newInstance(
            title = getString(R.string.exit_app_title),
            message = getString(R.string.exit_app_message),
            positiveText = getString(R.string.exit),
            negativeText = getString(R.string.cancel),
            onPositive = {
                cleanupResources()
                requireActivity().finishAffinity()
            },
            onNegative = {
                // No hacer nada o puedes agregar lógica adicional
            }
        ).show(parentFragmentManager, "ExitDialog")
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
            Log.d("DiseaseInfoFragment", "Navegando al fragmento de historial...")
            findNavController().navigate(R.id.action_diseaseInfoFragment_to_historyFragment)
        } catch (e: Exception) {
            Log.e("Navigation", "Error al navegar al diálogo: ${e.message}")
        }
    }

    override fun onDestroyView() {
        clearImageResources()
        super.onDestroyView()
    }

    private fun cleanupResources() {
        try {
            // 1. Limpiar recursos de imágenes
            clearImageResources()

            // 2. Liberar ViewModel compartido
            sharedViewModel.clearSelectedHistory()

            // 3. Limpiar ViewPager y adaptador
            binding.viewPager.adapter = null

            // 4. Remover observadores
            clearObservers()

            // 5. Limpiar referencias de menú
            menuHandler.let {
                // Si MenuToolbar tiene algún recurso que limpiar
            }

            // 6. Liberar binding (siempre al final)
            _binding = null

            Log.d("DiseaseInfoFragment", "All resources cleaned up successfully")
        } catch (e: Exception) {
            Log.e("DiseaseInfoFragment", "Error during cleanup", e)
        }
    }

    private fun clearImageResources() {
        try {
            // Limpiar Glide
            Glide.with(this).clear(binding.imageDisease)

            // Limpiar Picasso
            Picasso.get().cancelRequest(binding.imageDisease)

            // Liberar bitmap si existe
            weakBitmap?.get()?.recycle()
            weakBitmap = null

            // Resetear ImageView
            binding.imageDisease.setImageDrawable(null)
        } catch (e: Exception) {
            Log.e("DiseaseInfoFragment", "Error clearing image resources", e)
        }
    }

    private fun clearObservers() {
        try {
            // Remover observadores del ViewModel compartido
            sharedViewModel.compressedImage?.removeObservers(viewLifecycleOwner)
            sharedViewModel.selectedHistoryItem.removeObservers(viewLifecycleOwner)
        } catch (e: Exception) {
            Log.e("DiseaseInfoFragment", "Error clearing observers", e)
        }
    }

}

