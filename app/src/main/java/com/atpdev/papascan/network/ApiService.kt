package com.atpdev.papascan.network

import com.atpdev.papascan.data.model.RecognitionResult
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("recognize/")
    suspend fun recognizeDisease(@Part image: MultipartBody.Part): RecognitionResult
}