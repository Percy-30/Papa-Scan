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
import androidx.activity.OnBackPressedCallback
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
import com.airbnb.lottie.LottieAnimationView
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
    private val viewModel: RecognitionViewModel by viewModels()

    private lateinit var menuHandler: MenuToolbar
    private lateinit var info: FragmentAlertDialog

    lateinit var lottieAnimation: LottieAnimationView

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

        // Configurar Toolbar
        startMenu()
        setupCamera()
        setupUI()
        observeCameraState()

        EnabledRetroceso()

    }

    private fun EnabledRetroceso(){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = true
                CloseMenuFlotant()
            }
        })
    }


    //********MENU
    private fun startMenu() {
        menuHandler = MenuToolbar(
            context = requireContext(),
            onHistoryClick = { handleHistoryClick() },
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


    private fun handleHistoryClick() {
        // Implementa aquí la lógica para el historial
        Toast.makeText(requireContext(), "Historial seleccionado", Toast.LENGTH_SHORT).show()
        // findNavController().navigate(R.id.action_fragmentCamera_to_historyFragment)
    }


    //*********ALERT DIALOG
    private fun navigateToAlertDialog() {
        try {
            findNavController().navigate(R.id.action_fragmentCamera_to_fragmentAlertDialog)
        } catch (e: Exception) {
            Log.e("Navigation", "Error navigating to AlertDialog: ${e.message}")
            Toast.makeText(requireContext(), "Error al mostrar el diálogo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun CloseMenuFlotant(){
        //viewModel.btnAddMenu()
        viewModel.resetFabMenuState() // Reiniciar el estado del menú flotante
        //viewModel.btnOpenCamera()
    }

    //Pasar de fragment en fragment
    private fun navigateToRecognitionFragment() {
        CloseMenuFlotant()
        findNavController().navigate(R.id.action_fragmentCamera_to_recognitionFragment)
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

            // Asegurarse de que la guía sea visible cuando la cámara está activa
            overlayGuide.visibility = View.VISIBLE

            btnSaveCancelGone()
            btnTakePhoto.setOnClickListener { captureImage() }
            btnSavePhoto.setOnClickListener { saveImage() }
            btnCancelPhoto.setOnClickListener { retakePhoto() }
        }
    }

    private fun btnSaveCancelGone(){
        with(binding){
            btnSavePhoto.visibility = View.GONE
            btnCancelPhoto.visibility = View.GONE
        }
    }

    private fun btnSaveCancelVisibible(){
        with(binding){
            btnSavePhoto.visibility = View.VISIBLE
            btnCancelPhoto.visibility = View.VISIBLE
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
        //binding.lottieAnimationView.playAnimation()
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
            overlayGuide.visibility = View.GONE // Ocultar la guía cuando se muestra la preview
            imagePreview.setImageURI(uri)

            // Cambiar los botones visibles
            btnTakePhoto.visibility = View.GONE
            btnSaveCancelVisibible()
        }
        isImageCaptured = true
    }

    private fun retakePhoto() {
        with(binding) {
            // Volver a mostrar la vista previa de la cámara
            cameraPreview.visibility = View.VISIBLE
            imagePreview.visibility = View.GONE
            overlayGuide.visibility = View.VISIBLE // Mostrar la guía nuevamente

            // Restaurar los botones originales
            btnTakePhoto.visibility = View.VISIBLE
            btnSaveCancelGone()
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
        CloseMenuFlotant()
        cameraExecutor.shutdown()
        _binding = null
    }
}