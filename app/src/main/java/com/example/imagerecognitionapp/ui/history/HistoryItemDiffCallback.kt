package com.example.imagerecognitionapp.ui.history

import androidx.recyclerview.widget.DiffUtil
import com.example.imagerecognitionapp.data.historyItem.HistoryItem

class HistoryItemDiffCallback : DiffUtil.ItemCallback<HistoryItem>() {

    /*override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem.diseaseName == newItem.diseaseName && oldItem.section == newItem.section
    }*/

    override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        // Comparar por ID en lugar de nombre y sección
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem == newItem
    }
}
