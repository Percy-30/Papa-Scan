package com.example.imagerecognitionapp.data.historyItem

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.imagerecognitionapp.R
import com.example.imagerecognitionapp.databinding.ItemHistoryCardBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onDeleteClick: (HistoryItem) -> Unit,
    private val onItemClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    //private var items = mutableListOf<HistoryItem>()
    private var historyItems = emptyList<HistoryItem>()

    private val diffCallback = object : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem.diseaseName == newItem.diseaseName &&
                    oldItem.section == newItem.section
        }

        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem == newItem
        }
    }

    fun submitList(newItems: List<HistoryItem>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = historyItems.size
            override fun getNewListSize(): Int = newItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return diffCallback.areItemsTheSame(
                    historyItems[oldItemPosition],
                    newItems[newItemPosition]
                )
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return diffCallback.areContentsTheSame(
                    historyItems[oldItemPosition],
                    newItems[newItemPosition]
                )
            }
        })

        historyItems = newItems
        diffResult.dispatchUpdatesTo(this)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            ItemHistoryCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyItems[position])
    }

    //override fun getItemCount() = items.size
    override fun getItemCount(): Int = historyItems.size

    inner class HistoryViewHolder(
        private val binding: ItemHistoryCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryItem) {
            binding.apply {
                // Carga de la imagen con Glide
                /*Glide.with(imageViewDisease.context)
                    .load(item.imageUri) // Carga el Uri de la imagen
                    //.placeholder(R.drawable.placeholder_image) // Imagen de carga por defecto
                    //.error(R.drawable.ic_bar_exit) // Imagen en caso de error
                    .into(imageViewDisease) // ImageView donde se cargará la imagen*/

                // Cargar la imagen desde imagePath
                val bitmap = BitmapFactory.decodeFile(item.imagePath)
                binding.imageViewDisease.setImageBitmap(bitmap) // Asegúrate de que este ImageView exista


                textViewName.text = item.diseaseName
                textViewSection.text = item.section

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(item.timestamp))
                textViewTimestamp.text = formattedDate

                // Configurar el clic en el ítem para mostrar detalles
                /*root.setOnClickListener {
                    onItemClick(item)  // Llamar al método onItemClick pasando el ítem
                }*/
                binding.root.setOnClickListener {
                    onItemClick(item)
                }

                // Configurar el botón de eliminar

                buttonDelete.setOnClickListener {
                    onDeleteClick(item)
                }
            }
        }
    }
}

/*class HistoryAdapter(private val historyList: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewDisease: ImageView = itemView.findViewById(R.id.imageViewDisease)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        val textViewType: TextView = itemView.findViewById(R.id.textViewType)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_card, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        holder.imageViewDisease.setImageResource(item.imageRes)
        holder.textViewStatus.text = item.status
        holder.textViewType.text = item.type
        holder.textViewName.text = item.name
    }

    override fun getItemCount(): Int = historyList.size
}*/
