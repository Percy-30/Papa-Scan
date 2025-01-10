package com.example.imagerecognitionapp.ui.recognition

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.data.model.RecognitionResult
import com.example.imagerecognitionapp.data.repository.CameraRepository
import com.example.imagerecognitionapp.data.repository.ImageRecognitionException
import com.example.imagerecognitionapp.data.repository.ImageRecognitionRepository
import com.example.imagerecognitionapp.databinding.FragmentRecognitionBinding
import com.example.imagerecognitionapp.ui.common.MenuToolbar
import com.example.imagerecognitionapp.ui.result.SharedViewModel
import com.example.imagerecognitionapp.utils.TensorFlowHelper
import dagger.hilt.android.AndroidEntryPoint
import io.github.muddz.styleabletoast.StyleableToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

@AndroidEntryPoint
class RecognitionFragment : Fragment() {

    private var _binding: FragmentRecognitionBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageRecognitionRepository: ImageRecognitionRepository
    private lateinit var cameraRepository: CameraRepository
    private lateinit var menuHandler: MenuToolbar
    private val viewModel: RecognitionViewModel by viewModels()
    private var cargeImage = false

   // private lateinit var viewModel: RecognitionViewModel
   lateinit var tensorFlowHelper: TensorFlowHelper

   // Crear una instancia de SharedViewModel
   val sharedViewModel: SharedViewModel by activityViewModels()


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
        tensorFlowHelper = TensorFlowHelper(requireContext())
        startMenu()
        setupCameraRepository()
        setupObservers()
        setupClickListenersCameraGalery()
        observeNavigationResult()
        //ProcesarImagen()
        BotonProcesar()
        // Agrega el código aquí
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            CameraRepository.CAMERA_PERMISSION_REQUEST_CODE
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, actualizar LiveData
                 viewModel.updateCameraPermissionStatus(true)
            } else {
                // Permiso denegado, actualizar LiveData
                viewModel.updateCameraPermissionStatus(false)
            }
        }
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
                navigateToRecognitionFragmentMain()
                true
            }
            else -> menuHandler.onOptionsItemSelected(item)
        }
    }

    // Navegar hacia el diálogo "History"
    private fun navigateToHistoryFragment(){
        try {
            findNavController().navigate(R.id.action_recognitionFragment_to_historyFragment)
        } catch (e: Exception) {
            Log.e("Navigation", "Error al navegar al diálogo: ${e.message}")
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

    private fun navigateToProcessImagenFragment() {
        findNavController().navigate(R.id.action_recognitionFragment_to_resultFragment)
    }

    private fun navigateToResultFragment(result: RecognitionResult, bitmap: Bitmap) {
        Log.d("RecognitionFragment", "Navegando a ResultFragment")
        Handler(Looper.getMainLooper()).postDelayed({
        val bundle = Bundle().apply {
            putSerializable("recognitionResult", result) // Usar Serializable en lugar de Parcelable
            //putString("diseaseName", result.diseaseName)
            //putFloat("probability", result.probability)
            //putString("imageUrl", result.imageUrl)
            //putString("confidenceLevel", result.confidenceLevel)
            //putParcelable("bitmap", bitmap)  // Bitmap sigue siendo Parcelable
        }
        findNavController().navigate(R.id.action_recognitionFragment_to_resultFragment, bundle)
        }, 500)
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
        viewModel.resetFabMenuState()
        binding.icPhotoAlternative.visibility = View.GONE
        binding.icAddAPhoto.visibility = View.GONE
    }

    private fun setupMenuFlotant() {
       with(binding){
            iccAdd.setOnClickListener { viewModel.btnAddMenu() }
            icPhotoAlternative.setOnClickListener {
                if (!cameraRepository.isCameraPermissionGranted()) {
                    requestCameraPermission()
                } else {
                    viewModel.btnUploadImage()
                    showToast("Cargar Foto")
                    OpenUploadClick()
                }
            }

            icAddAPhoto.setOnClickListener {
                if (!cameraRepository.isCameraPermissionGranted()) {
                    requestCameraPermission()
                } else {
                    viewModel.btnOpenCamera()
                    showToast("Tomar foto")
                    navigateToCameraFragment()
                }
            }

           imgPhotoPreview.setOnClickListener{
               if (cargeImage){
                   showToast("Ya se cargo la imagen ahora debe procesar......")
                   //CloseMenuFlotant()
                   //binding.imgPhotoPreview.setImageBitmap(null)
                   //cargeImage = false
               }else{
                   if(!cameraRepository.isCameraPermissionGranted()){
                       requestCameraPermission()
                   }else{
                       viewModel.btnOpenCamera()
                       showToast("Tomar foto")
                       navigateToCameraFragment()
                   }
               }

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
    }

    private fun BotonProcesar(){
        binding.btnProcessImage.setOnClickListener {
            if (cargeImage) {
                ProcesarImagen()
            } else {
                Toast.makeText(requireContext(), "Debe abrir el menú para realizar la acción", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun ProcesarImagen() {
        Log.d("RecognitionFragment", "Botón procesar imagen presionado")
        if (cargeImage) {
            Log.d("RecognitionFragment", "Imagen cargada")

            // Obtener el bitmap de la vista previa
            val imageUri = (binding.imgPhotoPreview.drawable as? BitmapDrawable)?.bitmap
            imageUri?.let { bitmap ->
                Log.d("RecognitionFragment", "Imagen convertida a bitmap")

                // Guardar el bitmap en un archivo temporal
                val file = saveBitmapToFile(bitmap)
                file?.let { file ->
                    Log.d("RecognitionFragment", "Imagen guardada en archivo")

                    // Lanzar una corrutina para procesar la imagen
                    lifecycleScope.launch {
                        sharedViewModel.setCompressedImage(file)
                        Log.d("RecognitionFragment", "Corrutina lanzada")
                        try {
                            // Llamar al ViewModel para procesar la imagen
                            val result = viewModel.processImage(file)
                            // Ajustar dinámicamente el umbral (por ejemplo, desde un Slider/SeekBar)

                            // Mostrar el resultado en la interfaz de usuario
                            if (result.diseaseName == "No reconocido") {
                                Log.d("RecognitionFragment", "Imagen no reconocida")
                                Toast.makeText(requireContext(), "No reconocido", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.d("RecognitionFragment", "Resultado del procesamiento: $result")
                                navigateToResultFragment(result, bitmap)
                            }
                        } catch (e: ImageRecognitionException) {
                            Log.e("RecognitionFragment", "Error al procesar la imagen: ${e.message}")
                            Toast.makeText(requireContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("RecognitionFragment", "Error al procesar la imagen: ${e.message}")
                            Toast.makeText(requireContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                        }
                    }
                } ?: run {
                    Log.e("RecognitionFragment", "Error al guardar la imagen en un archivo")
                    Toast.makeText(requireContext(), "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Log.e("RecognitionFragment", "Error al convertir la imagen a bitmap")
                Toast.makeText(requireContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Debe abrir el menú para realizar la acción", Toast.LENGTH_SHORT).show()
        }
    }


    // Helper method to save bitmap to a temporary file
    private fun saveBitmapToFile(bitmap: Bitmap): File? {
        return try {
            val tempFile = File.createTempFile("image", ".jpg", requireContext().cacheDir)
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            Log.e("ImageSave", "Error saving bitmap to file", e)
            null
        }
    }

    fun saveBitmapToFileUri(bitmap: Bitmap): Uri? {
        val file = File.createTempFile("image", ".jpg", requireContext().cacheDir)
        val outputStream: OutputStream
        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            return Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
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
        startActivityForResult(intent, CAMERA_PERMISSION_REQUEST_CODE)
    }

    //Pasar de fragment en fragment
    private fun navigateToCameraFragment() {
        CloseMenuFlotant()
        findNavController().navigate(R.id.action_recognitionFragment_to_fragmentCamera)
    }
    // Mostrar resultrando tayedo de otro activity
    private fun observeNavigationResult() {
        // Observar el resultado de la navega
        // ción desde FragmentCamera
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("image_uri")?.observe(
            viewLifecycleOwner
        ) { uri ->
            uri?.let {
                // Mostrar la imagen capturada
                binding.imgPhotoPreview.setImageURI(Uri.parse(it))
                binding.imgPhotoPreview.visibility = View.VISIBLE
                cargeImage = true
                // Limpiar el savedStateHandle para evitar mostrar la misma imagen múltiples veces
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("image_uri")
            }
        }
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
            icPhotoAlternative.isClickable = clicked
            icAddAPhoto.isClickable = clicked
        }
    }

    private fun showToast(message: String) {
        //Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        //StyleableToast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        StyleableToast.makeText(requireContext(), message, R.style.exampleToast).show()
    }

    //Maneja el resultado de la selección de imagen (Android 8 - Android 10)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
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
                cargeImage = true
            }
            showToast("Imagen cargada con éxito")
        } ?: showToast("No se pudo cargar la imagen")
    }


    // Maneja   Selección de imágenes (Android 11+)
    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Cargar la imagen con escalado antes de asignarla al ImageView
                loadImageEfficiently(it)
                CloseMenuFlotant()
                cargeImage = true
                Toast.makeText(requireContext(), "Imagen cargada con éxito", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(requireContext(), "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }

    private fun loadImageEfficiently(uri: Uri) {
        try {
            // Cargar la imagen de manera eficiente, evitando OOM (OutOfMemoryError)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            // Obtener las dimensiones de la imagen sin cargarla completamente en memoria
            requireContext().contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            // Definir un tamaño máximo para las imágenes (por ejemplo, 1024px de ancho)
            val targetWidth = 1024
            val scale = kotlin.math.max(1, kotlin.math.min(
                options.outWidth / targetWidth,
                options.outHeight / targetWidth
            ))

            // Configurar las opciones para cargar la imagen de forma eficiente
            val loadOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            // Cargar la imagen escalada
            requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream, null, loadOptions)
                bitmap?.let {
                    binding.imgPhotoPreview.setImageBitmap(it) // Asignar la imagen escalada
                    binding.imgPhotoPreview.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            Log.e("ImageLoading", "Error loading image: ${e.message}")
            Toast.makeText(requireContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        CloseMenuFlotant()
        _binding = null
        if (::tensorFlowHelper.isInitialized) {
            tensorFlowHelper.close()  // Llamar a close() solo si tensorflowHelper está inicializado
        } else {
            Log.w("RecognitionFragment", "TensorFlowHelper no inicializado.")
        }
    }

    // Define el código de solicitud para la selección de imagen
    companion object {
        //private const val REQUEST_IMAGE_SELECT_CODE = 1002
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }
}
