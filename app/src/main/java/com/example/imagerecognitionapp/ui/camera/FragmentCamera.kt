package com.example.imagerecognitionapp.ui.camera

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.example.imagerecognitionapp.ui.recognition.RecognitionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class FragmentCamera : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraRepository: CameraRepository
    private var camera: Camera? = null
    private var isImageCaptured = false

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

        setupCamera()
        setupUI()
        observeCameraState()
    }

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
