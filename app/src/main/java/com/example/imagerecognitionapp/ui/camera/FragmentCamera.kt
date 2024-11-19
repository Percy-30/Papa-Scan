package com.example.imagerecognitionapp.ui.camera

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
//import com.example.imagerecognitionapp.Manifest
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.data.repository.CameraRepository
import com.example.imagerecognitionapp.databinding.FragmentCameraBinding
import com.example.imagerecognitionapp.databinding.FragmentRecognitionBinding
import com.example.imagerecognitionapp.ui.common.MenuToolbar
import com.example.imagerecognitionapp.ui.dialog.FragmentAlertDialog
import com.example.imagerecognitionapp.ui.recognition.RecognitionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class FragmentCamera : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private val aboutDialog: FragmentAlertDialog? = null

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraRepository: CameraRepository
    private var camera: Camera? = null
    private var isImageCaptured = false

    private lateinit var menuHandler: MenuToolbar
    private lateinit var info: FragmentAlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Inicializar MenuHandler


        // Configurar Toolbar
        startMenu()
        setupCamera()
        setupUI()
        observeCameraState()
       // setupToolbar()

    }

    private fun startMenu() {
        menuHandler = MenuToolbar(
            context = requireContext(),
           onAboutClick = { navigateToAlertDialog() }
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

    //********MENU


    private fun navigateToAlertDialog() {
        try {
            findNavController().navigate(R.id.action_fragmentCamera_to_fragmentAlertDialog)
        } catch (e: Exception) {
            Log.e("Navigation", "Error navigating to AlertDialog: ${e.message}")
            Toast.makeText(requireContext(), "Error al mostrar el diálogo", Toast.LENGTH_SHORT).show()
        }
    }

    /*private fun showAboutDialog() {
        // Evitar mostrar múltiples diálogos
        /*FragmentAlertDialog().apply {
            title = getString(R.string.info_title) // Make sure these string resources exist
            description = getString(R.string.info_description)
            onActionClicked = {
                dismiss()
            }
        }*/
        val dialogFragment = FragmentAlertDialog().apply {
            title = "Acerca de la aplicación"  // Texto directo en lugar de recurso
            description = "Esta es una aplicación de reconocimiento de imágenes"  // Texto directo
            onActionClicked = {
                parentFragmentManager.beginTransaction()
                    .remove(this)
                    .commit()
            }
        }

        parentFragmentManager.beginTransaction()
            .add(R.id.InfoConstraintLayout, dialogFragment)  // Asegúrate que InfoConstraintLayout existe en tu layout
            .addToBackStack(null)
            .commit()

    }*/


    //Pasar de fragment en fragment
    private fun navigateToRecognitionFragment() {
        findNavController().navigate(R.id.action_fragmentCamera_to_recognitionFragment)
    }
   /* private fun navigateToAlertDialog() {
        findNavController().navigate(R.id.action_recognitionFragment_to_fragmentAlertDialog)
    }*/


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuHandler.onCreateOptionsMenu(menu, inflater)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateToRecognitionFragment()
                true
            }
            R.id.menu_Exti -> {
                requireActivity().finish()
                true
            }
            R.id.menu_about ->{
                //showAboutDialog()
                navigateToAlertDialog()
                true
            }
            else -> menuHandler.onOptionsItemSelected(item)
        }
    }




    //*****CAMERA
    private fun setupCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraRepository = CameraRepository(requireActivity())
        startCamera()
    }

    private fun setupUI() {
        with(binding) {
            // Inicialmente, mostrar solo la vista previa y el botón de captura
            cameraPreview.visibility = View.VISIBLE
            btnTakePhoto.visibility = View.VISIBLE
            imagePreview.visibility = View.GONE
            btnSave.visibility = View.GONE
            //btnRetake.visibility = View.GONE

            btnTakePhoto.setOnClickListener { captureImage() }
            btnSave.setOnClickListener { saveImage() }
            //btnRetake.setOnClickListener { retakePhoto() }
        }
    }

    private fun startCamera() {
        cameraRepository.startCamera(binding.cameraPreview, viewLifecycleOwner)
    }

    private fun captureImage() {
        cameraRepository.captureImage(
            onImageCaptured = { uri ->
                showPreview(uri)
            },
            onError = { error ->
                showError(error)
            }
        )
    }

    private fun saveImage() {
        cameraRepository.saveImage(
            onImageSaved = { uri ->
                // Navegar de vuelta con el URI de la imagen
                findNavController().previousBackStackEntry?.savedStateHandle?.set("image_uri", uri.toString())
                findNavController().popBackStack()
            },
            onError = { error ->
                showError(error)
            }
        )
    }

    private fun showPreview(uri: Uri) {
        with(binding) {
            // Ocultar la vista previa de la cámara y mostrar la imagen capturada
            cameraPreview.visibility = View.GONE
            imagePreview.visibility = View.VISIBLE
            imagePreview.setImageURI(uri)

            // Cambiar los botones visibles
            btnTakePhoto.visibility = View.GONE
            btnSave.visibility = View.VISIBLE
            //btnRetake.visibility = View.VISIBLE
        }
        isImageCaptured = true
    }

    private fun retakePhoto() {
        with(binding) {
            // Volver a mostrar la vista previa de la cámara
            cameraPreview.visibility = View.VISIBLE
            imagePreview.visibility = View.GONE

            // Restaurar los botones originales
            btnTakePhoto.visibility = View.VISIBLE
            btnSave.visibility = View.GONE
            //btnRetake.visibility = View.GONE
        }
        isImageCaptured = false
        startCamera()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun observeCameraState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cameraRepository.cameraState.collect { state ->
                    when (state) {
                        is CameraRepository.CameraState.Error -> showError(state.message)
                        is CameraRepository.CameraState.ImageCaptured -> {
                            // La imagen se ha capturado, mostrar la vista previa
                        }
                        is CameraRepository.CameraState.ImageSaved -> {
                            // La imagen se ha guardado, navegar de vuelta
                        }
                        else -> { /* No action needed */ }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}
