package com.cocido.nonna.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cocido.nonna.R
import com.cocido.nonna.databinding.ItemTimelineMemoryBinding
import com.cocido.nonna.domain.model.Memory
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para mostrar recuerdos en la línea de tiempo
 * Lista cronológica con diseño de línea de tiempo
 */
class TimelineAdapter(
    private val onMemoryClick: (Memory) -> Unit
) : ListAdapter<Memory, TimelineAdapter.TimelineViewHolder>(MemoryDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val binding = ItemTimelineMemoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimelineViewHolder(binding, onMemoryClick)
    }
    
    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class TimelineViewHolder(
        private val binding: ItemTimelineMemoryBinding,
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
                
                // Cargar imagen
                val imageUrl = memory.photoRemoteUrl ?: memory.photoLocalPath
                if (imageUrl != null) {
                    Glide.with(imageViewMemory.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .centerCrop()
                        .into(imageViewMemory)
                } else {
                    // Mostrar placeholder según el tipo
                    imageViewMemory.setImageResource(
                        when (memory.type) {
                            com.cocido.nonna.domain.model.MemoryType.AUDIO_ONLY -> R.drawable.ic_launcher_foreground // TODO: Icono de audio
                            com.cocido.nonna.domain.model.MemoryType.RECIPE -> R.drawable.ic_launcher_foreground // TODO: Icono de receta
                            com.cocido.nonna.domain.model.MemoryType.NOTE -> R.drawable.ic_launcher_foreground // TODO: Icono de nota
                            else -> R.drawable.ic_launcher_foreground
                        }
                    )
                }
                
                // Tags
                chipGroupTags.removeAllViews()
                memory.tags.take(2).forEach { tag -> // Mostrar máximo 2 tags en timeline
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


