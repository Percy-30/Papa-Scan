package com.example.imagerecognitionapp.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.data.repository.CameraRepository
import com.example.imagerecognitionapp.databinding.ActivityMainBinding
import com.example.imagerecognitionapp.databinding.FragmentRecognitionMainBinding
import com.example.imagerecognitionapp.ui.common.MenuToolbar
import com.example.imagerecognitionapp.ui.recognition.RecognitionViewModel
import com.example.imagerecognitionapp.utils.TensorFlowHelper
import dagger.hilt.android.AndroidEntryPoint
import io.github.muddz.styleabletoast.StyleableToast

@AndroidEntryPoint
class RecognitionMain : Fragment() {

    private var _binding: FragmentRecognitionMainBinding? = null
    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!


    private lateinit var cameraRepository: CameraRepository
    private val viewModel: RecognitionViewModel by viewModels()
    lateinit var tensorFlowHelper: TensorFlowHelper

    // Flag to track if we should check permissions in onResume
    private var checkPermissionsOnResume = false

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
        _binding = FragmentRecognitionMainBinding.inflate(inflater, container, false)
        return binding.root
        //return inflater.inflate(R.layout.fragment_recognition_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Use the View object to perform additional initialization
        setupCameraRepository()
        initViews()

        // Observe camera permission changes
        viewModel.isCameraPermissionGranted.observe(viewLifecycleOwner) { isGranted ->
            if (isGranted) {
                navigateToRecognitionFragment()
            }
        }
    }

    private fun setupCameraRepository() {
        cameraRepository = CameraRepository(requireActivity())
    }

    private fun initViews() {
        with(binding) {
            btnDetectPatofoli.setOnClickListener { startApp() }
            btnAbout.setOnClickListener { infoApp() }
            btnExit.setOnClickListener { exitApp() }
        }
    }

    private fun startApp() {
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        when {
            cameraRepository.isCameraPermissionGranted() -> navigateToRecognitionFragment()
            cameraRepository.isCameraPermissionPermanentlyDenied() -> showGoToSettingsDialog()
            else -> requestCameraPermission()
        }
    }
    private fun navigateToRecognitionFragment() {
        findNavController().navigate(R.id.recognitionFragment)
    }


    private fun requestCameraPermission() {
        cameraRepository.markPermissionAsRequested() // Marca que ya lo pedimos al menos una vez
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            CameraRepository.CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun showToastError(message: String){
        StyleableToast.makeText(requireContext(), message, R.style.exampleToastError).show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            val isGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED

            viewModel.updateCameraPermissionStatus(isGranted)

            if (!isGranted) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showGoToSettingsDialog()
                } else {
                    showToastError("Se requiere permiso de cámara para esta función")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Check if permissions were potentially modified while the app was in background
        if (checkPermissionsOnResume) {
            checkPermissionsOnResume = false
            checkPermissionStatusAfterSettingsReturn()
        }
    }

    private fun checkPermissionStatusAfterSettingsReturn() {
        if (cameraRepository.isCameraPermissionGranted()) {
            // Permission was granted in settings
            viewModel.updateCameraPermissionStatus(true)
            navigateToRecognitionFragment()
        } else if (cameraRepository.isCameraPermissionPermanentlyDenied()) {
            // User still denied permission in settings
            showToastError("El permiso aún no ha sido concedido.")
            //showToastError(getString(R.string.camera_permission_still_denied))
        }
    }


    private fun infoApp(){
        binding.btnAbout.setOnClickListener{
            findNavController().navigate(R.id.action_recognitionMain_to_fragmentAlertDialog)
        }
    }

    private fun exitApp(){
        binding.btnExit.setOnClickListener{
            activity?.finish()
        }
    }

    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permiso de cámara requerido")
            .setMessage("Parece que denegaste el permiso de cámara. Para usar esta función, ve a Ajustes > Aplicaciones > PapaScan > Permisos y habilítalo.")
            .setPositiveButton("Ir a Ajustes") { _, _ ->
                // Set flag to check permissions when we return from settings
                checkPermissionsOnResume = true
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
                .setNegativeButton("Cancelar", null)
                .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (::tensorFlowHelper.isInitialized) {
            tensorFlowHelper.close()
        }
        _binding = null // Prevent memory leaks
    //binding = null <- Esto sería si usaras `var binding` en lugar de `lateinit val`
    }

    companion object {
        //private const val REQUEST_IMAGE_SELECT_CODE = 1002
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

}
