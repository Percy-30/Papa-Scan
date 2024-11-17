package com.example.imagerecognitionapp.data.repository

import com.example.imagerecognitionapp.data.model.RecognitionResult
import com.example.imagerecognitionapp.network.ApiService
import dagger.Module
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

import java.io.File
import javax.inject.Inject

class ImageRecognitionRepository @Inject constructor(private val apiService: ApiService) {
    suspend fun getRecognitionResult(image: File): RecognitionResult {
        val requestFile = image.asRequestBody()  // Convierte el archivo a RequestBody
        val body = MultipartBody.Part.createFormData("image", image.name, requestFile)
        return apiService.recognizeDisease(body)
    }
    
}