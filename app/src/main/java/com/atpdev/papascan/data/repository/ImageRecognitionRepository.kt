package com.atpdev.papascan.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.atpdev.papascan.data.model.RecognitionResult
import com.atpdev.papascan.utils.TensorFlowHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImageRecognitionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private lateinit var tensorflowHelper: TensorFlowHelper
    private val classNames: List<String> = loadLabels()
    private var detectionThreshold = 0.95f // Umbral para considerar una clase como detectada
    /*# Probabilidades predichas por el modelo
    predicted_probabilities = [0.1, 0.4, 0.35, 0.8, 0.95, 0.2]
    # Etiquetas reales
    true_labels = [0, 0, 1, 1, 1, 0]*/

    init {
        tensorflowHelper = TensorFlowHelper(context)
    }

    fun setDetectionThreshold(threshold: Float) {
        if (threshold in 0.0f..1.0f) {
            detectionThreshold = threshold
        } else {
            throw IllegalArgumentException("El umbral debe estar entre 0.0 y 1.0")
        }
    }


    private fun loadLabels(): List<String> {
        return context.assets.open("labels.txt").bufferedReader().useLines { lines ->
            lines.map { it.trim() }.toList()
        }
    }

    suspend fun getRecognitionResult(bitmap: Bitmap): RecognitionResult {
        return withContext(Dispatchers.IO) {
            try {
                val scaledBitmap = scaleBitmap(bitmap, 256 , 256) // Cambia a 512 - 250
                Log.d("ImageRecognitionRepository", "Imagen escalada: ${scaledBitmap.width}x${scaledBitmap.height}")

                // Ejecutar inferencia
                val output = tensorflowHelper.runInference(scaledBitmap)
                Log.d("ImageRecognitionRepository", "Resultado de la inferencia: ${output.joinToString()}")

                if (output.isEmpty()) {
                    Log.e("ImageRecognitionRepository", "La inferencia no produjo resultados")
                    throw ImageRecognitionException("La inferencia no produjo resultados")
                }

                // Obtener la probabilidad más alta
                val maxProbability = output.maxOrNull()!!

                // Si la probabilidad más alta está por debajo del umbral de detección, se considera "No detectado"
                return@withContext if (maxProbability < detectionThreshold) {
                    Log.d("ImageRecognitionRepository", "No se detectó ninguna clase con suficiente confianza")
                    RecognitionResult("No detectado", 0f)
                } else {
                    // Obtener el índice de la probabilidad más alta
                    val maxIndex = output.indexOfFirst { it == maxProbability }
                    val diseaseName = classNames.getOrNull(maxIndex) ?: "Desconocido"
                    Log.d("ImageRecognitionRepository", "Resultado final: $diseaseName, Probabilidad: $maxProbability")
                    RecognitionResult(diseaseName, maxProbability)
                }
            } catch (e: ImageRecognitionException) {
                Log.e("ImageRecognitionRepository", "Error al ejecutar la inferencia: ${e.message}")
                throw e
            } catch (e: Exception) {
                Log.e("ImageRecognitionRepository", "Error al ejecutar la inferencia: ${e.message}")
                throw ImageRecognitionException(e.message)
            }
        }
    }


    private fun scaleBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}

class ImageRecognitionException(message: String?) : Exception(message)