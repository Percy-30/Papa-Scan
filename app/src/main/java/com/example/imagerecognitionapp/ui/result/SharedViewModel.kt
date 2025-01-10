package com.example.imagerecognitionapp.ui.result

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.imagerecognitionapp.data.historyItem.HistoryItem
import com.example.imagerecognitionapp.data.model.History
import java.io.File
import java.lang.ref.WeakReference

/*class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val _bitmap = MutableLiveData<Bitmap>()
    val bitmap: LiveData<Bitmap> = _bitmap

    fun setBitmap(bitmap: Bitmap) {
        _bitmap.value = bitmap
    }
}*/
class SharedViewModel : ViewModel() {
//class SharedViewModel(application: Application) : AndroidViewModel(application) {
    // MutableLiveData para el Bitmap
    private val _bitmap = MutableLiveData<Bitmap>()
    val bitmap: LiveData<Bitmap> = _bitmap
    private val _bitmapPath = MutableLiveData<String>()
    val bitmapPath: LiveData<String> = _bitmapPath

    private var weakBitmap: WeakReference<Bitmap>? = null



    // MutableLiveData para el historial seleccionado
    private val _selectedHistoryItem = MutableLiveData<History>()
    val selectedHistoryItem: LiveData<History> = _selectedHistoryItem

    private val _compressedImage = MutableLiveData<File>()
    //val compressedImage: LiveData<File> = _compressedImage
    // En el SharedViewModel
    val compressedImage: LiveData<Uri?> = MutableLiveData()

    /*fun setCompressedImage(file: File) {
        _compressedImage.value = file
    }*/
    fun setCompressedImage(file: File) {
        //val uri = Uri.fromFile(file) // Convertir archivo a Uri
        //(compressedImage as MutableLiveData).value = uri
        val uri = Uri.fromFile(file)
        if (uri != null) {
           // loadImageEfficiently(uri)
            (compressedImage as MutableLiveData).value = uri
        } else {
            Log.e("ImageLoading", "Invalid file or URI.")
        }
    }

    // Método para actualizar el Bitmap
    fun setBitmap(bitmap: Bitmap) {
        _bitmap.value = bitmap
    }
    fun setBitmapPath(path: String) {
        _bitmapPath.value = path
    }

    /*private fun loadImageEfficiently(uri: Uri) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            val targetWidth = 1024
            val scale = kotlin.math.max(1, kotlin.math.min(
                options.outWidth / targetWidth,
                options.outHeight / targetWidth
            ))

            val loadOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, loadOptions)?.let { bitmap ->
                    weakBitmap = WeakReference(bitmap)
                    setBitmap(bitmap)
                }
            }
        } catch (e: Exception) {
            Log.e("ImageLoading", "Error loading image: ${e.message}")
        }
    }*/


    // Método para actualizar el HistoryItem seleccionado
    fun setSelectedHistoryItem(history: History) {
        _selectedHistoryItem.value = history
    }
}