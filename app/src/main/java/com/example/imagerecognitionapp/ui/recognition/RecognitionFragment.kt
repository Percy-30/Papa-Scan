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
import com.example.imagerecognitionapp.ui.common.MenuToolbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecognitionFragment : Fragment() {

    private var _binding: FragmentRecognitionBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraRepository: CameraRepository
    private lateinit var menuHandler: MenuToolbar
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
        return binding.root
    }

    //Princiapal Funciones
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ocultar botones flotantes
        //viewModel.btnAddMenu()

        startMenu()
        setupCameraRepository()
        setupObservers()
        setupClickListenersCameraGalery()
        observeNavigationResult()
    }

    // Inicializar Menu Toolbar
   /* private fun setupToolbar(){
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.appBarMenu.toolbar) // Se usa la referencia correcta
            supportActionBar?.title = "Reconocimiento"
            supportActionBar?.setDisplayHomeAsUpEnabled(true) // Mostrar el botón de "atrás"
        }
        setHasOptionsMenu(true)
    }*/



    //********MENU
    private fun startMenu() {
        menuHandler = MenuToolbar(context = requireContext())
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
        return when (item.itemId) {
            android.R.id.home -> {
                navigateToRecognitionFragmentMain()
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
    //*********ALERT DIALOG
    private fun navigateToAlertDialog() {
        try {
            findNavController().navigate(R.id.action_recognitionFragment_to_fragmentAlertDialog)
        } catch (e: Exception) {
            Log.e("Navigation", "Error navigating to AlertDialog: ${e.message}")
            Toast.makeText(requireContext(), "Error al mostrar el diálogo", Toast.LENGTH_SHORT).show()
        }
    }

    //Pasar de fragment en fragment
    private fun navigateToRecognitionFragmentMain() {
        findNavController().navigate(R.id.action_recognitionFragment_to_recognitionMain)
    }
    private fun startAddImageAddPhoto(){
        /*with(binding){
            icAddAPhoto.visibility = View.GONE
            icPhotoAlternative.visibility = View.GONE
        }*/
    }


    // Inicializar CameraRepository
    private fun setupCameraRepository(){
        cameraRepository = CameraRepository(requireActivity())
    }

    private fun setupClickListenersCameraGalery(){
               // btnCamera.setOnClickListener { OpenCameraClick() }
            //btnUploadPhoto.setOnClickListener { OpenUploadClick() }
            setupMenuFlotant()
    }

    private fun CloseMenuFlotant(){
        viewModel.btnAddMenu()
        binding.icPhotoAlternative.visibility = View.GONE
        binding.icAddAPhoto.visibility = View.GONE
        //viewModel.btnOpenCamera()
    }

    private fun setupMenuFlotant() {
       with(binding){
            iccAdd.setOnClickListener { viewModel.btnAddMenu() }
            icPhotoAlternative.setOnClickListener {
                viewModel.btnUploadImage()
                showToast("Cargar Foto")
                OpenUploadClick()
            }

            icAddAPhoto.setOnClickListener {
                viewModel.btnOpenCamera()
                showToast("Tomar foto")
                navigateToCameraFragment()
            }
        }
    }



    private fun OpenUploadClick(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            // Android 11+ (API 30+)
            selectImageLauncher.launch("image/*")
        }else{
            // Android 8 - Android 10 (API 26 - 29)
            OpenGaleryUploadImage()
        }
    }

    private fun OpenGaleryUploadImage(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_SELECT_CODE)
    }

    //Pasar de fragment en fragment
    private fun navigateToCameraFragment() {
        CloseMenuFlotant()
        findNavController().navigate(R.id.action_recognitionFragment_to_fragmentCamera)
    }
    // Mostrar resultrando tayedo de otro activity
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

    private fun setupObservers() {
        var isFirstObservation = true // Variable para evitar la animación inicial
        viewModel.isFabMenuOpen.observe(viewLifecycleOwner) { isOpen ->
            if (isFirstObservation) {
                isFirstObservation = false
            } else {
                setVisibility(isOpen)
                setAnimation(isOpen)
                setClickeable(isOpen)
            }
        }
        //binding.icPhotoAlternative.visibility = View.GONE
       // binding.icAddAPhoto.visibility = View.GONE
        /*viewModel.isUploadImage.observe(viewLifecycleOwner) { isUpload ->
            binding.icPhotoAlternative.visibility = if (isUpload) View.VISIBLE else View.GONE
        }

        viewModel.isOpenCamera.observe(viewLifecycleOwner) { isOpen ->
            binding.icAddAPhoto.visibility = if (isOpen) View.VISIBLE else View.GONE
        }*/

    }

    private fun setVisibility(clicked: Boolean) {
        with(binding){
            //icPhotoAlternative.visibility = if (clicked) View.VISIBLE else View.GONE
            //icAddAPhoto.visibility = if (clicked) View.VISIBLE else View.GONE

            if (clicked){
                icPhotoAlternative.visibility = View.GONE
                icAddAPhoto.visibility = View.GONE
            }else{
                // Esperar al final de la animación para ocultarlos
                icPhotoAlternative.postDelayed({
                    icPhotoAlternative.visibility = View.GONE }, toBottom.duration)

                icAddAPhoto.postDelayed({ icAddAPhoto.visibility }, toBottom.duration)
            }


        }

    }

    private fun setAnimation(clicked: Boolean) {
        with(binding){
            if (clicked) {
                icPhotoAlternative.startAnimation(fromBottom)
                icAddAPhoto.startAnimation(fromBottom)
                iccAdd.startAnimation(rotateOpen)
            } else {
                icPhotoAlternative.startAnimation(toBottom)
                icAddAPhoto.startAnimation(toBottom)
                iccAdd.startAnimation(rotateClose)
            }
        }
    }

    private fun setClickeable(clicked: Boolean){
        with(binding){
            /*if (!clicked){
                icPhotoAlternative.isClickable = false
                icAddAPhoto.isClickable = false
            }else{
                icPhotoAlternative.isClickable = true
                icAddAPhoto.isClickable = true
            }*/
            icPhotoAlternative.isClickable = clicked
            icAddAPhoto.isClickable = clicked
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    //Maneja el resultado de la selección de imagen (Android 8 - Android 10)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            handleSelectedImage(data?.data)
            /*val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                binding.imgPhotoPreview.setImageURI(selectedImageUri)
                binding.imgPhotoPreview.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Imagen cargada con éxito", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }*/
        }
    }

    private fun handleSelectedImage(uri: Uri?) {
        uri?.let {
            with(binding.imgPhotoPreview) {
                setImageURI(it)
                visibility = View.VISIBLE
                CloseMenuFlotant()
            }
            showToast("Imagen cargada con éxito")
        } ?: showToast("No se pudo cargar la imagen")
    }


    // Maneja   Selección de imágenes (Android 11+)
    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imgPhotoPreview.setImageURI(it)
                binding.imgPhotoPreview.visibility = View.VISIBLE
                CloseMenuFlotant()
                Toast.makeText(requireContext(), "Imagen cargada con éxito", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(requireContext(), "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onDestroyView() {
        super.onDestroyView()
        CloseMenuFlotant()
        _binding = null
    }

    // Define el código de solicitud para la selección de imagen
    companion object {
        private const val REQUEST_IMAGE_SELECT_CODE = 1001
    }



}
