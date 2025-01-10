package com.example.imagerecognitionapp.ui.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imagerecognitionapp.data.diseaseName.DiseaseInfo
import com.example.imagerecognitionapp.data.historyItem.HistoryRepository
import com.example.imagerecognitionapp.data.model.History
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

// HistoryViewModel.kt
@HiltViewModel
class HistoryViewModel @Inject constructor(private val historyRepository: HistoryRepository) : ViewModel() {
    //private val _historyList = MutableLiveData<List<HistoryItem>>(emptyList())
    //val historyList: LiveData<List<HistoryItem>> = _historyList
    private val _historyList = MutableLiveData<List<History>>()
    val historyList: LiveData<List<History>> = _historyList

    private val _selectedDisease = MutableLiveData<DiseaseInfo>()
    val selectedDisease: LiveData<DiseaseInfo> = _selectedDisease

    init {
        //loadHistory()}
        historyRepository.getAllHistoryLive().observeForever { histories ->
            _historyList.value = histories
        }
    }

    fun addToHistory(history: History) {
        viewModelScope.launch {

            // Check for existing entry by disease name and section
            val existingHistory = historyRepository.getHistoryByDiseaseAndSection(history.diseaseName)

            //if (existingHistory == null) {
                historyRepository.insertHistory(history)
                loadHistory() // Reload immediately after insert
            /*} else {
                // Update the existing entry instead of creating a new one
                val updatedHistory = existingHistory.copy(
                    description = history.description,
                    prevention = history.prevention,
                    causes = history.causes,
                    treatment = history.treatment,
                    timestamp = System.currentTimeMillis()
                )
                historyRepository.updateHistory(updatedHistory)
                loadHistory()
            }*/

            // Verificar si ya existe una entrada con la misma ruta de imagen
            /*val existingHistoryByImage = history.imagePath?.let { path ->
                historyRepository.getHistoryByPhotoHash(path)
            }

            if (existingHistoryByImage != null) {
                // Si existe una entrada con la misma imagen, actualizar la existente
                historyRepository.updateHistory(history.copy(
                    id = existingHistoryByImage.id,
                    section = "main"
                ))
            } else {
            //val existingHistory = historyRepository.getHistoryByDiseaseAndSection(history.diseaseName)


           // if (existingHistory == null) {
                historyRepository.insertHistory(history.copy(section = "main"))
                loadHistory()
           }*/
        }
    }

    fun calculatePhotoHash(photoBytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(photoBytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /*fun removeFromHistory(diseaseName: String, section: String) {
        viewModelScope.launch {
            historyRepository.getHistoryByDiseaseAndSection(diseaseName, section)?.let { history ->
                historyRepository.deleteHistory(history)
                loadHistory()
            }
        }
    }*/

    fun removeFromHistory(diseaseName: String) {
        viewModelScope.launch {
            historyRepository.getHistoryByDiseaseAndSection(diseaseName)?.let { history ->
                historyRepository.deleteHistory(history)
                loadHistory()
            }

        }
    }

    suspend fun getLastId(): Int? {
        return historyRepository.getLastId()
    }


    suspend fun getHistoryById(id: Int): History? {
        return historyRepository.getHistoryById(id)
    }

    fun clearHistory() {
        viewModelScope.launch {
           // historyRepository.deleteAll()
            loadHistory()
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val histories = historyRepository.getAllHistory()
            _historyList.postValue(histories)
            //_historyList.postValue(histories.distinctBy { it.diseaseName }) // Only get unique diseases
        }
    }

    fun setSelectedDisease(diseaseInfo: DiseaseInfo) {
        _selectedDisease.value = diseaseInfo
    }

    suspend fun getHistoryByDiseaseAndSection(diseaseName: String): History? {
        return historyRepository.getHistoryByDiseaseAndSection(diseaseName)
    }

    suspend fun getHistoryByImagePath(imagePath: String): History? {
        return historyRepository.getHistoryByImagePath(imagePath)
    }

    fun updateCurrentDisease(diseaseInfo: DiseaseInfo) {
        //_currentDisease.value = diseaseInfo
    }

}