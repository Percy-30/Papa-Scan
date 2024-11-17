package com.example.imagerecognitionapp.network

import androidx.room.Query
import com.example.imagerecognitionapp.data.model.RecognitionResult
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("recognize/")
    suspend fun recognizeDisease(@Part image: MultipartBody.Part): RecognitionResult
}