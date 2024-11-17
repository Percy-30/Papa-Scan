package com.example.imagerecognitionapp.data.repository

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Locale

class CameraRepository(private val activity: FragmentActivity) {

    companion object {
        private const val TAG = "CameraRepository"
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    private var imageCapture: ImageCapture? = null
    private var lastCapturedImageUri: Uri? = null

    // Estado de la cámara observable
    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Idle)
    val cameraState: StateFlow<CameraState> = _cameraState

    // Estados posibles de la cámara
    sealed class CameraState {
        object Idle : CameraState()
        object Preview : CameraState()
        data class Error(val message: String) : CameraState()
        data class ImageCaptured(val uri: Uri) : CameraState()
        data class ImageSaved(val uri: Uri) : CameraState()
    }

    fun isCameraPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    fun startCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    _cameraState.value = CameraState.Preview
                } catch (e: Exception) {
                    _cameraState.value = CameraState.Error("Error al iniciar la cámara: ${e.message}")
                    Log.e(TAG, "Error al vincular casos de uso de la cámara", e)
                }
            } catch (e: Exception) {
                _cameraState.value = CameraState.Error("Error al obtener el proveedor de cámara: ${e.message}")
                Log.e(TAG, "Error al obtener el proveedor de cámara", e)
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    fun captureImage(
        onImageCaptured: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        val imageCapture = imageCapture ?: run {
            onError("La captura de imagen no está configurada")
            return
        }

        // Crear un archivo temporal para la vista previa
        val fileName = "PREVIEW_" + SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS",
            Locale.US
        ).format(System.currentTimeMillis()) + ".jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraApp/Preview")
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            activity.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(activity),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { uri ->
                        lastCapturedImageUri = uri
                        _cameraState.value = CameraState.ImageCaptured(uri)
                        onImageCaptured(uri)
                    } ?: run {
                        onError("Error: URI de imagen vacía")
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _cameraState.value = CameraState.Error("Error al capturar la imagen: ${exception.message}")
                    onError("Error al capturar la imagen: ${exception.message}")
                }
            }
        )
    }

    fun saveImage(
        onImageSaved: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        val capturedUri = lastCapturedImageUri ?: run {
            onError("No hay imagen capturada para guardar")
            return
        }

        // Crear un nuevo archivo para la imagen guardada
        val fileName = "IMG_" + SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS",
            Locale.US
        ).format(System.currentTimeMillis()) + ".jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
        }

        try {
            // Copiar la imagen desde la ubicación temporal a la ubicación final
            val inputStream = activity.contentResolver.openInputStream(capturedUri)
            val outputUri = activity.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            outputUri?.let { uri ->
                activity.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    inputStream?.use { input ->
                        input.copyTo(outputStream)
                    }
                }
                // Eliminar la imagen temporal
                activity.contentResolver.delete(capturedUri, null, null)
                lastCapturedImageUri = null
                _cameraState.value = CameraState.ImageSaved(uri)
                onImageSaved(uri)
            } ?: run {
                onError("Error al guardar la imagen")
            }
        } catch (e: Exception) {
            onError("Error al guardar la imagen: ${e.message}")
        }
    }
}
