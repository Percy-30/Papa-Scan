package com.atpdev.papascan.domain.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.atpdev.papascan.core.utils.TensorFlowHelper
import com.atpdev.papascan.domain.model.RecognitionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


interface ImageRecognitionRepository {
    suspend fun getRecognitionResult(bitmap: Bitmap): RecognitionResult
    fun setDetectionThreshold(threshold: Float)
}