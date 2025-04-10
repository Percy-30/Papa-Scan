package com.example.imagerecognitionapp.ui.recognition

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.data.model.RecognitionResult
import com.example.imagerecognitionapp.data.repository.ImageRecognitionException
import com.example.imagerecognitionapp.data.repository.ImageRecognitionRepository
import com.example.imagerecognitionapp.utils.TensorFlowHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    application: Application,
    private val imageRecognitionRepository: ImageRecognitionRepository
) : AndroidViewModel(application) {

    // Guarda un flag para saber que queremos abrir la cámara después de obtener permiso
// En RecognitionViewModel.kt
    private val _openCameraAfterPermission = MutableLiveData<Boolean>(false)
    val openCameraAfterPermission: LiveData<Boolean> = _openCameraAfterPermission

    fun setOpenCameraAfterPermission(open: Boolean) {
        _openCameraAfterPermission.value = open
    }

    fun shouldOpenCameraAfterPermission(): Boolean {
        return _openCameraAfterPermission.value == true
    }

    private val tensorFlowHelper: TensorFlowHelper = TensorFlowHelper(application)

    // LiveData para observar el resultado del reconocimiento
    private val _recognitionResult = MutableLiveData<RecognitionResult>()
    val recognitionResult: LiveData<RecognitionResult> = _recognitionResult

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

    private val _bitmap = MutableLiveData<Bitmap>()
    val bitmap: LiveData<Bitmap> = _bitmap

    //Cambia el estado del FAB Menu
    fun btnAddMenu() {
        _isFabMenuOpen.value = _isFabMenuOpen.value?.not()
    }
    fun btnUploadImage(){
        //_isUploadImage.value = _isUploadImage.value != true
        _isUploadImage.value = true
        resetFabMenuState()
    }
    fun btnOpenCamera(){
        //_isOpenCamera.value = _isOpenCamera.value!= true
        _isOpenCamera.value = true
        resetFabMenuState()
    }

    fun resetFabMenuState() {
        _isFabMenuOpen.value = false
        _isUploadImage.value = false
        _isOpenCamera.value = false
    }

    /*suspend fun processImage(imageFile: File): RecognitionResult {
        _loading.value = true
        return try {
            // Leer el archivo de imagen y convertirlo a un Bitmap
            val bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(imageFile.path)
            }
            val scaledBitmap = scaleBitmap(bitmap, 250, 250)
            Log.d("ViewModel", "Nuevo medida de la foto grande: ${scaledBitmap.width}x${scaledBitmap.height}")

            // Normalizar la imagen
            val normalizedBitmap = normalizeBitmap(scaledBitmap)
            Log.d("ViewModel", "Nuevo medida de la foto normalizada: ${normalizedBitmap.width}x${normalizedBitmap.height}")

            // Usar TensorFlow Lite para la inferencia
            val (isUnknown, result) = tensorFlowHelper.runInferenceWithUnknownDetection(normalizedBitmap)

            if (isUnknown) {
                RecognitionResult("No reconocido", 0f)
            } else {
                result
            }
        } catch (e: Exception) {
            Log.d("ViewModel", "Error al procesar la imagen: ${e.message}")
            RecognitionResult("", 0f)
        } finally {
            _loading.value = false
        }
    }*/
    private companion object {
        const val ERROR_MESSAGE_IMAGE_PROCESSING = "Error al procesar la imagen"
    }


    suspend fun processImage(imageFile: File): RecognitionResult {
        _loading.postValue(true) // Activa el indicador de carga
        return withContext(Dispatchers.IO) {
            try {
                // Leer y escalar el archivo de imagen
                val bitmap = BitmapFactory.decodeFile(imageFile.path)
                val scaledBitmap = scaleBitmap(bitmap, 250, 250)
                Log.d("ViewModel", "Tamaño de la imagen escalada: ${scaledBitmap.width}x${scaledBitmap.height}")

                // Realizar el reconocimiento
                val recognitionResult = recognizeImage(scaledBitmap)
                Log.d("ViewModel", "Resultado del reconocimiento: $recognitionResult")

                // Actualiza el LiveData en el hilo principal
                _recognitionResult.postValue(recognitionResult)
                recognitionResult // Retorna el resultado
            } catch (e: ImageRecognitionException) {
                Log.e("ViewModel", "Error al procesar la imagen: ${e.message}")
                _errorMessage.postValue(ERROR_MESSAGE_IMAGE_PROCESSING)
                RecognitionResult("Error", 0.0f)
            } catch (e: Exception) {
                Log.e("ViewModel", "Error al procesar la imagen: ${e.message}")
                _errorMessage.postValue(ERROR_MESSAGE_IMAGE_PROCESSING)
                RecognitionResult("Error", 0.0f)
            } finally {
                // Desactiva el indicador de carga
                _loading.postValue(false)
            }
        }
    }

    fun setBitmap(bitmap: Bitmap) {
        _bitmap.value = bitmap
    }

    private fun scaleBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private suspend fun recognizeImage(bitmap: Bitmap): RecognitionResult {
        return imageRecognitionRepository.getRecognitionResult(bitmap)
    }

    fun setRecognitionResult(result: RecognitionResult) {
        Log.d("ViewModel", "Resultado establecido: $result")
        _recognitionResult.value = result
    }

    // En tu ViewModel
    private fun normalizeBitmap(bitmap: Bitmap): Bitmap? {
        val bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bmp32)
        val paint = Paint()
        val rect = Rect(0, 0, bmp32.width, bmp32.height)
        canvas.drawBitmap(bmp32, rect, rect, paint)

        val pixels = IntArray(bmp32.width * bmp32.height)
        bmp32.getPixels(pixels, 0, bmp32.width, 0, 0, bmp32.width, bmp32.height)

        for (i in pixels.indices) {
            pixels[i] = (pixels[i] / 255.0f).toInt()
        }

        bmp32.setPixels(pixels, 0, bmp32.width, 0, 0, bmp32.width, bmp32.height)

        return bmp32
        // Obtener la forma de entrada esperada del modelo
       /* val inputShape = tensorFlowHelper.getInputShape()

        // Escalar el bitmap a las dimensiones esperadas por el modelo
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputShape[1], inputShape[2], true)

        // Crear un objeto TensorImage de tipo FLOAT32
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(resizedBitmap)

        // Crear un ByteBuffer para almacenar los valores de los píxeles
        val byteBuffer = ByteBuffer.allocateDirect(resizedBitmap.width * resizedBitmap.height * 4) // 4 bytes por pixel
        byteBuffer.order(ByteOrder.nativeOrder())

        // Obtener los valores de los píxeles
        val pixelValues = IntArray(resizedBitmap.width * resizedBitmap.height)
        resizedBitmap.getPixels(pixelValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

        // Normalizar los valores de los píxeles de 0-255 a 0.0-1.0
        for (i in pixelValues.indices) {
            val pixelValue = pixelValues[i]
            val r = (pixelValue shr 16 and 0xFF) / 255.0f
            val g = (pixelValue shr 8 and 0xFF) / 255.0f
            val b = (pixelValue and 0xFF) / 255.0f
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
        byteBuffer.rewind()

        // Obtener la forma de entrada esperada del modelo
        val shape = intArrayOf(1, inputShape[1], inputShape[2], inputShape[3]) // Assuming input shape is like [batch, height, width, channels]

        // Crear un TensorBuffer desde el ByteBuffer
        return TensorBuffer.createFixedSize(shape, DataType.FLOAT32).apply {
            loadBuffer(byteBuffer)
        }*/
    }

        /*val bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bmp32)
        val paint = Paint()
        val rect = Rect(0, 0, bmp32.width, bmp32.height)
        canvas.drawBitmap(bmp32, rect, rect, paint)

        val pixels = IntArray(bmp32.width * bmp32.height)
        bmp32.getPixels(pixels, 0, bmp32.width, 0, 0, bmp32.width, bmp32.height)

        for (i in pixels.indices) {
            pixels[i] = (pixels[i] / 255.0f).toInt()
        }

        bmp32.setPixels(pixels, 0, bmp32.width, 0, 0, bmp32.width, bmp32.height)

        return bmp32*/



    // Convierte el URI de la imagen a una cadena base64
    private suspend fun convertUriToBase64(imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            val inputStream = getApplication<Application>().contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tensorFlowHelper.close()
        //tensorFlowHelper.close()
    }
}


