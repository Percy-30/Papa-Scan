package com.example.imagerecognitionapp.ui.recognition

import android.app.Application
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.imagerecognitionapp.data.model.RecognitionResult
import com.example.imagerecognitionapp.data.repository.ImageRecognitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    application: Application,
    private val imageRecognitionRepository: ImageRecognitionRepository
) : AndroidViewModel(application) {

    // LiveData para observar el resultado del reconocimiento
    private val _recognitionResult = MutableLiveData<RecognitionResult>()
    val recognitionResult: LiveData<RecognitionResult> get() = _recognitionResult

    // LiveData para observar el estado de la carga (si hay un error, proceso en curso, etc.)
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // LiveData para el estado del permiso de cámara
    private val _isCameraPermissionGranted = MutableLiveData<Boolean>()
    val isCameraPermissionGranted: LiveData<Boolean> get() = _isCameraPermissionGranted

    // LiveData para controlar el estado del FAB Menu
    private val _isFabMenuOpen = MutableLiveData(false)
    val isFabMenuOpen: LiveData<Boolean> get() = _isFabMenuOpen

    private val _isUploadImage = MutableLiveData(false)
    val isUploadImage: LiveData<Boolean> get() = _isUploadImage

    private val _isOpenCamera = MutableLiveData(false)
    val isOpenCamera: LiveData<Boolean> get() = _isOpenCamera

    // Actualiza el estado del permiso de cámara
    fun updateCameraPermissionStatus(isGranted: Boolean) {
        _isCameraPermissionGranted.value = isGranted
    }

    //Cambia el estado del FAB Menu

    fun btnAddMenu() {
        _isFabMenuOpen.value = _isFabMenuOpen.value?.not()
    }
    fun btnUploadImage(){
        _isUploadImage.value = _isUploadImage.value != true
        //_isUploadImage.value = true
    }
    fun btnOpenCamera(){
        _isOpenCamera.value = _isOpenCamera.value!= true
        //_isOpenCamera.value = true
    }


    // Función para procesar la imagen seleccionada y obtener el resultado del reconocimiento
    fun processImage(imageFile: File) {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Llamar al repositorio para obtener el resultado del modelo
                val result = imageRecognitionRepository.getRecognitionResult(imageFile)

                // Pasar el resultado al LiveData en el hilo principal
                withContext(Dispatchers.Main) {
                    _recognitionResult.value = result
                    _loading.value = false
                }
            } catch (e: Exception) {
                // Manejar errores, mostrar mensaje de error
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al procesar la imagen: ${e.message}"
                    _loading.value = false
                }
            }
        }
    }

    // Función para procesar la imagen capturada desde un URI y actualizar el resultado de reconocimiento
    fun handleCapturedPhoto(imageUri: Uri) {
        _loading.value = true
        viewModelScope.launch {
            try {
                // Convierte el URI de la imagen a una cadena base64
                val imageBase64 = convertUriToBase64(imageUri)

                // Llama al repositorio para obtener el resultado del reconocimiento de la imagen
                //val result = imageRecognitionRepository.recognizeImage(imageBase64)

                // Actualiza el LiveData con el resultado del reconocimiento
                withContext(Dispatchers.Main) {
                    //  _recognitionResult.value = result
                    _loading.value = false
                }
            } catch (e: Exception) {
                // Manejar errores, mostrar mensaje de error
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al procesar la imagen capturada: ${e.message}"
                    _loading.value = false
                }
            }
        }
    }

    // Convierte el URI de la imagen a una cadena base64
    private suspend fun convertUriToBase64(imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            val inputStream = getApplication<Application>().contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        }
    }
}
