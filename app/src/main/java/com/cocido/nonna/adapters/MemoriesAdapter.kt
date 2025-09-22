package com.cocido.nonna.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cocido.nonna.R
import com.cocido.nonna.databinding.ItemMemoryCardBinding
import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para mostrar recuerdos en un grid
 * Maneja la visualización de fotos, indicadores de audio y metadatos
 */
class MemoriesAdapter(
    private val onMemoryClick: (Memory) -> Unit
) : ListAdapter<Memory, MemoriesAdapter.MemoryViewHolder>(MemoryDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val binding = ItemMemoryCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemoryViewHolder(binding, onMemoryClick)
    }
    
    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class MemoryViewHolder(
        private val binding: ItemMemoryCardBinding,
        private val onMemoryClick: (Memory) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormatter = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
        
        fun bind(memory: Memory) {
            binding.apply {
                // Configurar click listener
                root.setOnClickListener { onMemoryClick(memory) }
                
                // Título
                textViewTitle.text = memory.title
                
                // Fecha
                textViewDate.text = memory.dateTaken?.let { timestamp ->
                    dateFormatter.format(Date(timestamp))
                } ?: "Sin fecha"
                
                // Indicador de audio
                imageViewAudioIndicator.visibility = if (memory.audioLocalPath != null || memory.audioRemoteUrl != null) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                
                // Indicador de tipo
                textViewTypeIndicator.text = when (memory.type) {
                    MemoryType.PHOTO_WITH_AUDIO -> "FOTO+AUDIO"
                    MemoryType.PHOTO_ONLY -> "FOTO"
                    MemoryType.AUDIO_ONLY -> "AUDIO"
                    MemoryType.RECIPE -> "RECETA"
                    MemoryType.NOTE -> "NOTA"
                }
                
                // Cargar imagen
                val imageUrl = memory.photoRemoteUrl ?: memory.photoLocalPath
                if (imageUrl != null) {
                    Glide.with(imageViewMemory.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_photo)
                        .error(R.drawable.ic_photo)
                        .centerCrop()
                        .into(imageViewMemory)
                } else {
                    // Mostrar placeholder según el tipo
                    imageViewMemory.setImageResource(
                        when (memory.type) {
                            MemoryType.AUDIO_ONLY -> R.drawable.ic_audio
                            MemoryType.RECIPE -> R.drawable.ic_recipe
                            MemoryType.NOTE -> R.drawable.ic_note
                            else -> R.drawable.ic_photo
                        }
                    )
                }
                
                // Tags
                chipGroupTags.removeAllViews()
                memory.tags.take(3).forEach { tag -> // Mostrar máximo 3 tags
                    val chip = com.google.android.material.chip.Chip(chipGroupTags.context)
                    chip.text = tag
                    chip.isClickable = false
                    chipGroupTags.addView(chip)
                }
            }
        }
    }
    
    private class MemoryDiffCallback : DiffUtil.ItemCallback<Memory>() {
        override fun areItemsTheSame(oldItem: Memory, newItem: Memory): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Memory, newItem: Memory): Boolean {
            return oldItem == newItem
        }
    }
}


