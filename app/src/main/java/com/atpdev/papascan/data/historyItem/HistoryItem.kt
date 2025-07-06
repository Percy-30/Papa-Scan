package com.atpdev.papascan.data.historyItem

data class HistoryItem(
    val id: Int,  // Ya está presente el identificador
    val diseaseName: String,
    val section: String,
    val timestamp: Long = System.currentTimeMillis(),
    //val imageUri: ByteArray?
    val imagePath: String // Cambiado a String
)
