package com.example.imagerecognitionapp.data.historyItem

data class HistoryItem(
    val diseaseName: String,
    val section: String,
    val timestamp: Long = System.currentTimeMillis(),
    //val imageUri: ByteArray?
    val imagePath: String // Cambiado a String
)
