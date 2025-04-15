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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.data.repository.CameraRepository
import com.example.imagerecognitionapp.databinding.ActivityMainBinding
import com.example.imagerecognitionapp.databinding.FragmentRecognitionMainBinding
import com.example.imagerecognitionapp.ui.common.MenuToolbar
import com.example.imagerecognitionapp.ui.dialog.FragmentAlertDialogExit
import com.example.imagerecognitionapp.utils.TensorFlowHelper
import io.github.muddz.styleabletoast.StyleableToast
import kotlinx.coroutines.launch

class RecognitionMain : Fragment() {

    private lateinit var binding: FragmentRecognitionMainBinding
    private lateinit var menuHandler: MenuToolbar
    //private val model: MainViewModel by activityViewModels()

    lateinit var buttonWithAnimation: ConstraintLayout
    lateinit var lottieAnimationView: LottieAnimationView
    lateinit var buttonText: TextView

    // private lateinit var viewModel: RecognitionViewModel
    lateinit var tensorFlowHelper: TensorFlowHelper

    // Camera repository
    private lateinit var cameraRepository: CameraRepository

    // Registrar el lanzador para solicitar permisos usando la API de ActivityResult
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso concedido, navegar al fragmento de reconocimiento
            Log.d("RecognitionMain", "Permiso de cámara concedido")
            navigateToRecognitionFragment()
        } else {
            // Permiso denegado
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Mostrar explicación de por qué necesitamos el permiso
                showPermissionRationaleDialog()
            } else {
                // El usuario marcó "No volver a preguntar", mostrar diálogo para ir a configuración
                showGoToSettingsDialog()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        // Initialize camera repository
        cameraRepository = CameraRepository(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRecognitionMainBinding.inflate(inflater, container, false)
        return binding.root
        //return inflater.inflate(R.layout.fragment_recognition_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMainButtons()
    }

    override fun onResume() {
        super.onResume()
        // Verificar permisos cada vez que el fragmento se reanuda
        // para el caso cuando el usuario regresa de la configuración
        if (cameraRepository.isCameraPermissionGranted()) {
            // Actualizar la UI si el permiso ya fue concedido
            Log.d("RecognitionMain", "Permiso de cámara ya concedido")
            binding.btnDetectPatofoli.isEnabled = true
        }
    }
    private fun setupMainButtons() {
        // Botón para iniciar la detección - verifica permisos primero
        binding.btnDetectPatofoli.setOnClickListener {
            checkCameraPermission()
        }
        // Configurar otros botones
        infoApp()
        exitApp()
    }

    private fun showToastError(message: String){
        StyleableToast.makeText(requireContext(), message, R.style.exampleToastError).show()
    }

    private fun   showToastWarning(message: String){
        StyleableToast.makeText(requireContext(), message, R.style.exampleToastProcessImage).show()
    }

    private fun checkCameraPermission() {
        when {
            cameraRepository.isCameraPermissionGranted() -> {
                // Permiso ya concedido, navegar al fragmento de reconocimiento
                navigateToRecognitionFragment()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Mostrar explicación de por qué necesitamos el permiso
                showPermissionRationaleDialog()
            }
            else -> {
                // Solicitar permiso de cámara
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun navigateToRecognitionFragment() {
        findNavController().navigate(R.id.recognitionFragment)
    }

    private fun showPermissionRationaleDialog() {
        FragmentAlertDialogExit.newInstance(
            title = "Permiso de cámara necesario",
            message = "Esta aplicación necesita acceso a la cámara para detectar y analizar imágenes. Por favor, concede el permiso para continuar.",
            positiveText = "Conceder",
            negativeText = "Cancelar",
            onPositive = {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onNegative = {
                showToastWarning("El acceso a la cámara es necesario para usar esta función")
            },
            iconResId = R.drawable.ic_advertencia // Ícono desde recursos

        ).show(parentFragmentManager, "CameraPermissionDialog")
    }

    private fun showGoToSettingsDialog() {
        FragmentAlertDialogExit.newInstance(
            title = "Permiso de cámara requerido",
            message = "Parece que denegaste el permiso de cámara. Para usar esta función, ve a Ajustes > Aplicaciones > PapaScan > Permisos y habilítalo.",
            positiveText = "Ir a Ajustes",
            negativeText = "Cancelar",
            onPositive = {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            },
            onNegative = {
                showToastError("El acceso a la cámara es necesario para usar esta función")
            },
            iconResId = R.drawable.ic_bar_info, // Ícono desde recursos
            positiveButtonBackgroundColor = R.color.colorTeal,
            positiveButtonTextColor = R.color.black,
            negativeButtonBackgroundColor = R.color.colorOrangeSoft,
            negativeButtonTextColor = R.color.black
        ).show(parentFragmentManager, "GoToSettingsDialog")
    }



    /*private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permiso de cámara necesario")
            .setMessage("Esta aplicación necesita acceso a la cámara para detectar y analizar imágenes. Por favor, concede el permiso para continuar.")
            .setPositiveButton("Conceder") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                showToastWarning("El acceso a la cámara es necesario para usar esta función",)
            }
            .show()
    }*/

    /*private fun showGoToSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permiso de cámara requerido")
            .setMessage("Parece que denegaste el permiso de cámara. Para usar esta función, ve a Ajustes > Aplicaciones > PapaScan > Permisos y habilítalo.")
            .setPositiveButton("Ir a Ajustes") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancelar"){ dialog, _ ->
                dialog.dismiss()
                showToastError("El acceso a la cámara es necesario para usar esta función",)
            }
            .show()
    }*/


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


    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
        if (::tensorFlowHelper.isInitialized) {
            tensorFlowHelper.close()  // Llamar a close() solo si tensorflowHelper está inicializado
        } else {
            Log.w("RecognitionFragment", "TensorFlowHelper no inicializado.")
        }
    }


}