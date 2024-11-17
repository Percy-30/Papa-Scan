package com.example.imagerecognitionapp.ui.recognition

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.data.repository.CameraRepository
import com.example.imagerecognitionapp.databinding.FragmentRecognitionBinding
import com.example.imagerecognitionapp.ui.camera.FragmentCamera
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecognitionFragment : Fragment() {

    private var _binding: FragmentRecognitionBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraRepository: CameraRepository
    private val viewModel: RecognitionViewModel by viewModels()

    // Animaciones
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.to_botton_anim) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecognitionBinding.inflate(inflater, container, false)
        //return binding.root
        return binding.root
    }



    //MENU ->
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        requireActivity().menuInflater.inflate(R.menu.menu, menu) // Asegúrate de tener un archivo XML `context_menu`
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu) // Asegúrate de tener un archivo XML `menu_main`
        return super.onCreateOptionsMenu(menu, inflater)
        //super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> {
                Toast.makeText(requireContext(), "Acerca de seleccionado", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_Exti -> {
                requireActivity().finish() // Salir de la app
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    //Princiapal Funciones
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar la Toolbar del Fragment
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.appBarMenu.toolbar) // Se usa la referencia correcta
            supportActionBar?.title = "Reconocimiento"
            supportActionBar?.setDisplayHomeAsUpEnabled(true) // Mostrar el botón de "atrás"
        }
        setHasOptionsMenu(true)

        /*(activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.appBarMenu.toolbar) // Asegúrate de que `toolbar` esté definido en tu diseño
            registerForContextMenu(binding.appBarMenu.toolbar)
            supportActionBar?.title = "Reconocimiento"
            supportActionBar?.setDisplayHomeAsUpEnabled(true) // Opcional: Para mostrar el botón de "atrás"
            setHasOptionsMenu(true)
        }*/

        //MenuAdd
        setupObservers()
        //setupAddButtonMenu()
        setupFabMenu()
        observeNavigationResult()

        // Inicializar CameraRepository
        cameraRepository = CameraRepository(requireActivity())

        // Configurar el botón de "Tomar Foto"
        binding.btnCamera.setOnClickListener {
            if(cameraRepository.isCameraPermissionGranted()){
                navigateToCameraFragment()
            }else{
                requestCameraPermission()
            }

            /*if (cameraRepository.isCameraPermissionGranted()) {
                startCameraPreview()
                binding.btnCamera.visibility = View.GONE
                binding.btnTakePhoto.visibility = View.VISIBLE
                binding.imgPhotoPreview.visibility = View.GONE
            } else {
                requestCameraPermission()
            }*/
        }
        // Configuar el boton para cargar el archivo
        // Configura el botón para cargar una imagen desde la galería
        binding.btnUploadPhoto.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                // Android 11+ (API 30+)
                selectImageLauncher.launch("image/*")
            }else{
                // Android 8 - Android 10 (API 26 - 29)
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_IMAGE_SELECT_CODE)
            }
        }

        // Configurar el botón de "Reconocer"

    }

    private fun observeNavigationResult() {
        // Observar el resultado de la navegación desde FragmentCamera
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("image_uri")?.observe(
            viewLifecycleOwner
        ) { uri ->
            uri?.let {
                // Mostrar la imagen capturada
                binding.imgPhotoPreview.setImageURI(Uri.parse(it))
                binding.imgPhotoPreview.visibility = View.VISIBLE
                // Limpiar el savedStateHandle para evitar mostrar la misma imagen múltiples veces
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("image_uri")
            }
        }
    }


    private fun navigateToCameraFragment() {
        findNavController().navigate(R.id.action_recognitionFragment_to_fragmentCamera)
    }




    /*private fun startCameraPreview() {
        // Inicia la cámara y establece la vista previa en el `PreviewView`
        //cameraRepository.startCamera(bindingcamera.cameraPreview)

        // Configurar botón de captura una vez que la cámara esté lista
        binding.btnTakePhoto.setOnClickListener {
            try {
                cameraRepository.takePhoto(
                    onImageCaptured = { imageUri ->
                        // Mostrar la foto capturada
                        binding.imgPhotoPreview.setImageURI(imageUri)
                        binding.imgPhotoPreview.visibility = View.VISIBLE

                        Toast.makeText(requireContext(), "Foto guardada en galería", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Log.e("RecognitionFragment", "Error al tomar la foto: ${e.message}", e)
                Toast.makeText(requireContext(), "Error al tomar la foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }*/


    //Maneja el resultado de la selección de imagen (Android 8 - Android 10)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                binding.imgPhotoPreview.setImageURI(selectedImageUri)
                binding.imgPhotoPreview.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Imagen cargada con éxito", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFabMenu() {
        binding.iccAdd.setOnClickListener { viewModel.btnAddMenu() }
        binding.icPhotoAlternative.setOnClickListener {
            viewModel.btnUploadImage()
            //selectImageFromGallery()
        }
        binding.icAddAPhoto.setOnClickListener {
            viewModel.btnOpenCamera()
            navigateToCameraFragment()
        }
    }

    // Maneja   Selección de imágenes (Android 11+)
    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imgPhotoPreview.setImageURI(it)
                binding.imgPhotoPreview.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Imagen cargada con éxito", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(requireContext(), "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }


    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), CameraRepository.CAMERA_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CameraRepository.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //startCameraPreview()
                navigateToCameraFragment()
            } else {
                Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupObservers() {
        viewModel.isFabMenuOpen.observe(viewLifecycleOwner) { isOpen ->
            setVisibility(isOpen)
            setAnimation(isOpen)
            setClickeable(isOpen)
        }
    }

   /* private fun setupAddButtonMenu() {
        binding.iccAdd.setOnClickListener {
            viewModel.btnAddMenu()
        }
        binding.icPhotoAlternative.setOnClickListener {
            viewModel.btnUploadImage()

            Toast.makeText(requireContext(), "Cargar Foto Clicked", Toast.LENGTH_SHORT).show()

        }
        binding.icAddAPhoto.setOnClickListener {
           // viewModel.btnOpenCamera()
            findNavController().navigate(R.id.action_recognitionFragment_to_fragmentCamera)
            //startCameraPreview()
            Toast.makeText(requireContext(), "Tomar Foto Clicked", Toast.LENGTH_SHORT).show()
        }
    }*/

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.icPhotoAlternative.visibility = View.VISIBLE
            binding.icAddAPhoto.visibility = View.VISIBLE
        } else {
            binding.icPhotoAlternative.visibility = View.INVISIBLE
            binding.icAddAPhoto.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.icPhotoAlternative.startAnimation(fromBottom)
            binding.icAddAPhoto.startAnimation(fromBottom)
            binding.iccAdd.startAnimation(rotateOpen)
        } else {
            binding.icPhotoAlternative.startAnimation(toBottom)
            binding.icAddAPhoto.startAnimation(toBottom)
            binding.iccAdd.startAnimation(rotateClose)
        }
    }

    private fun setFabClickable(clicked: Boolean) {
        binding.icPhotoAlternative.isClickable = !clicked
        binding.icAddAPhoto.isClickable = !clicked
    }

    private fun setClickeable(clicked: Boolean){
        if(!clicked){
            binding.icPhotoAlternative.isClickable = true
            binding.icAddAPhoto.isClickable = true
        }else{
            binding.icPhotoAlternative.isClickable = false
            binding.icAddAPhoto.isClickable = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setFabVisibility(clicked: Boolean) {
        binding.icPhotoAlternative.visibility = if (clicked) View.INVISIBLE else View.VISIBLE
        binding.icAddAPhoto.visibility = if (clicked) View.INVISIBLE else View.VISIBLE
    }


    // Define el código de solicitud para la selección de imagen
    companion object {
        private const val REQUEST_IMAGE_SELECT_CODE = 1001
    }



}
