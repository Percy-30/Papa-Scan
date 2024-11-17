package com.example.imagerecognitionapp.data.model

data class RecognitionResult(
    val diseaseName: String,   // Nombre de la enfermedad detectada
    val probability: Float,    // Probabilidad de la enfermedad detectada
    val imageUrl: String? = null  // URL de la imagen si es necesario
)
