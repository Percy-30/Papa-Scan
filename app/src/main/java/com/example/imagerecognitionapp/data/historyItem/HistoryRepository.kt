package com.example.imagerecognitionapp.data.historyItem

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.example.imagerecognitionapp.dao.HistoryDao
import com.example.imagerecognitionapp.db.HistoryDb
import com.example.imagerecognitionapp.data.model.History
import javax.inject.Inject

class HistoryRepository @Inject constructor(private val historyDb: HistoryDb) {

    suspend fun getHistoryByPhotoHash(photoHash: String): History? {
        return historyDb.HistoryDao().getHistoryByPhotoHash(photoHash)
    }

    // Actualizar un historial existente en la base de datos
    suspend fun updateHistory(history: History) {
        //historyDb.HistoryDao().updateHistory(history)
    }

    // Insertar un nuevo historial en la base de datos
    suspend fun insertHistory(history: History) {
        historyDb.HistoryDao().insertHistory(history)
    }

    suspend fun getHistoryId(): Int {
        return historyDb.HistoryDao().getHistoryId()
    }

    suspend fun getLastId(): Int? {
        return historyDb.HistoryDao().getLastId()
    }

    suspend fun getHistoryById(id: Int): History? {
        return historyDb.HistoryDao().getHistoryById(id)
    }

    // Get all history entries
    suspend fun getAllHistory(): List<History> {
        return historyDb.HistoryDao().getAllHistory()
    }

    // Get history by disease name
    suspend fun getHistoryByDiseaseAndSection(diseaseName: String): History? {
        return historyDb.HistoryDao().getHistoryByDiseaseAndSection(diseaseName)
    }

    suspend fun getHistoryByImagePath(imagePath: String): History? {
        return historyDb.HistoryDao().getHistoryByPhotoHash(imagePath)
    }

    /*suspend fun getHistoryByDiseaseAndSection(diseaseName: String, section: String): History? {
        return historyDb.HistoryDao().getHistoryByDiseaseAndSection(diseaseName, section)
    }*/

    // Delete specific history entry
    suspend fun deleteHistory(history: History) {
        historyDb.HistoryDao().deleteHistory(history)
    }
    // Get all history as LiveData
    fun getAllHistoryLive(): LiveData<List<History>> {
        return historyDb.HistoryDao().getAllHistoryItems()
    }

    /*suspend fun insertHistory(historyItem: HistoryItem) {
        val historyEntity = History(
            id = 0, // Room generará el ID automáticamente
            diseaseName = historyItem.diseaseName,
            section = historyItem.section,
            timestamp = historyItem.timestamp,
            imageUri = historyItem.imageUri
        )
        historyDb.HistoryDao().insertHistory(historyEntity)
    }

    // Obtener todo el historial desde la base de datos
    suspend fun getAllHistory(): List<HistoryItem> {
        return historyDb.HistoryDao().getAll().map { entity ->
            HistoryItem(
                diseaseName = entity.diseaseName,
                section = entity.section,
                timestamp = entity.timestamp,
                imageUri = entity.imageUri
            )
        }
    }

    // Borrar todo el historial de la base de datos
    suspend fun clearHistory() {
        historyDb.HistoryDao().deleteAll()
    }

    // Obtener un historial específico desde la base de datos
    suspend fun getHistoryByDiseaseAndSection(diseaseName: String, section: String): HistoryItem? {
        val historyEntity = historyDb.HistoryDao().getItemByDiseaseAndSection(diseaseName, section)
        return historyEntity?.let { entity ->
            HistoryItem(
                diseaseName = entity.diseaseName,
                section = entity.section,
                timestamp = entity.timestamp,
                imageUri = entity.imageUri
            )
        }
    }

    // Eliminar un historial específico desde la base de datos
    suspend fun deleteHistoryByDiseaseAndSection(diseaseName: String, section: String) {
        val historyEntity = historyDb.HistoryDao().getItemByDiseaseAndSection(diseaseName, section)
        historyEntity?.let { entity ->
            historyDb.HistoryDao().delete(entity)
        }
    }

    // Obtener todos los historiales como LiveData
    // Obtener todo el historial desde la base de datos
    // Obtener todos los historiales como LiveData
    fun getAllHistoryAsLiveData(): LiveData<List<HistoryItem>> {
        return historyDb.HistoryDao().getAllHistoryItems().switchMap { entities ->
            liveData {
                emit(entities.map { entity ->
                    HistoryItem(
                        diseaseName = entity.diseaseName,
                        section = entity.section,
                        timestamp = entity.timestamp,
                        imageUri = entity.imageUri
                    )
                })
            }
        }
    }*/

}
