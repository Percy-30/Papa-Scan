package com.atpdev.papascan.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atpdev.papascan.data.diseaseName.DiseaseInfo
import com.atpdev.papascan.data.historyItem.HistoryRepository
import com.atpdev.papascan.data.model.History
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject

// HistoryViewModel.kt
@HiltViewModel
class HistoryViewModel @Inject constructor(private val historyRepository: HistoryRepository) : ViewModel() {
    //private val _historyList = MutableLiveData<List<HistoryItem>>(emptyList())
    //val historyList: LiveData<List<HistoryItem>> = _historyList
    //private val _historyList = MutableLiveData<List<History>>()
    val historyList: LiveData<List<History>> get() = historyRepository.getAllHistoryItems()
    //val historyList: LiveData<List<History>> = _historyList

    private val _selectedDisease = MutableLiveData<DiseaseInfo>()
    val selectedDisease: LiveData<DiseaseInfo> = _selectedDisease

    init {
        //loadHistory()
    }

    fun addToHistory(history: History) {
        viewModelScope.launch {
            historyRepository.insertHistory(history)
            //loadHistory()
            /*historyRepository.insertHistory(history)
            loadHistory()
            // 🔥 Forzar actualización del historial después de insertar
            val updatedHistoryList = historyRepository.getAllHistory()
            withContext(Dispatchers.Main) {
                _historyList.value = updatedHistoryList
                Log.d("HistoryViewModel", "Historial actualizado: $updatedHistoryList")
            }*/
            //loadHistory()
            // Verifica si el ID es 0, indicando que es un nuevo historial
            /*if (history.id == 0) {
                // Insertar historial y obtener el ID generado automáticamente
                val newId = historyRepository.insertHistory(history)
                Log.d("HistoryViewModel", "Historial agregado con ID: $newId")
            } else {
                // Actualizar historial existente
                Log.d("HistoryViewModel", "Actualizando historial con ID: ${history.id}")
                historyRepository.updateHistory(history)
            }

            loadHistory()  // Recargar la lista después de actualizar o insertar*/
        }
    }






    /*fun addToHistory(history: History) {
        viewModelScope.launch {
            //historyRepository.insertHistory(history)
            //loadHistory() // Reload immediately after insert
            val existingHistory = historyRepository.getHistoryByDiseaseAndSection(history.diseaseName)

            if (existingHistory == null) {
                // Insertar nuevo historial si no existe
                historyRepository.insertHistory(history)
            } else {
                // Actualizar el existente en lugar de agregar duplicado
                val updatedHistory = existingHistory.copy(
                    description = history.description,
                    prevention = history.prevention,
                    causes = history.causes,
                    treatment = history.treatment,
                    timestamp = System.currentTimeMillis()
                )
                historyRepository.updateHistory(updatedHistory)
            }
            loadHistory() // Actualizar la lista después de la operación
        }
    }*/

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

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
            // Opcional: Mostrar mensaje o actualizar UI
            loadHistory()
            //_toastMessage.postValue("Historial vaciado")
        }
    }

    // En el ViewModel
    fun removeFromHistoryById(id: Int) {
        // Aquí va la lógica para eliminar el historial por id, por ejemplo, llamando al repositorio o a la base de datos
        viewModelScope.launch {
            historyRepository.getHistoryById(id)?.let { history ->
                historyRepository.deleteHistory(history)
               //loadHistory()  // Recargar la lista después de eliminar
                // Actualiza la lista de historial después de la eliminación
                //_historyList.value = historyRepository.getAllHistory() // O recarga los datos
                //_historyList.value = historyRepository.getAllHistory()  // Aquí deberías obtener los datos
            }
        }
    }



    // Obtener todos los historiales (opcional, pero útil si necesitas datos sin LiveData)
    /*fun getAllHistory() {
        viewModelScope.launch {
            val historyList = historyRepository.getAllHistory()
            Log.d("HistoryViewModel", "Historial recuperado desde BD: ${historyList.size} elementos")

        }
    }*/

    suspend fun getLastId(): Int? {
        return historyRepository.getLastId()
    }


    suspend fun getHistoryById(id: Int): History? {
        return historyRepository.getHistoryById(id)
    }

    fun clearHistory() {
        viewModelScope.launch {
           //historyRepository.deleteAll()
            loadHistory()
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            //val histories = historyRepository.getAllHistory()
            // Llamar al repositorio para obtener los datos
                val updatedList = historyRepository.getAllHistory()
                withContext(Dispatchers.Main) {
                    //_historyList.postValue(updatedList)
                }
              // Aquí deberías obtener los datos
            //Log.d("HistoryViewModel", "Historial refrescado: ${histories.size} elementos")
            //_historyList.postValue(histories)  // Asegura que los datos estén actualizados
        }
    }

    //fun getAllHistory() = historyRepository.getAllHistoryLive()  // Si usas LiveData en el repositorio

    fun updateHistory(history: History) {
        viewModelScope.launch {
            historyRepository.updateHistory(history)
            loadHistory()
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

    /**
     * Calcular hash de una imagen (SHA-256)
     */
    fun calculatePhotoHash(photoBytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(photoBytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

}