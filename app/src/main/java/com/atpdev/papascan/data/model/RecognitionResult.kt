package com.atpdev.papascan.data.model

import java.io.Serializable

data class RecognitionResult(
    val diseaseName: String,   // Nombre de la enfermedad detectada
    val probability: Float,    // Probabilidad de la enfermedad detectada (entre 0 y 1)
    val imageUrl: String? = null,  // URL de la imagen si es necesario
    val confidenceLevel: String? = null  // Nivel de confianza del modelo
):Serializable {
    fun getProbabilityString(): String {
        return String.format("%.2f%%", probability * 100)
    }

    override fun toString(): String {
        return "Enfermedad: $diseaseName, Probabilidad: ${getProbabilityString()}"
    }
}
