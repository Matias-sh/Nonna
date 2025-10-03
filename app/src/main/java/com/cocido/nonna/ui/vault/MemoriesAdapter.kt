package com.cocido.nonna.ui.vault

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cocido.nonna.databinding.ItemMemoryCardBinding
import com.cocido.nonna.domain.model.Memory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

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

        fun bind(memory: Memory) {
            binding.textViewTitle.text = memory.title

            // Formatear fecha
            if (memory.dateTaken != null) {
                val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es"))
                binding.textViewDate.text = dateFormat.format(Date(memory.dateTaken))
            } else {
                binding.textViewDate.text = "Sin fecha"
            }

            // Mostrar tags
            binding.chipGroupTags.removeAllViews()
            if (memory.tags.isNotEmpty()) {
                // Mostrar solo los primeros 3 tags
                memory.tags.take(3).forEach { tag ->
                    val chip = com.google.android.material.chip.Chip(binding.root.context).apply {
                        text = tag
                        isClickable = false
                        isCheckable = false
                    }
                    binding.chipGroupTags.addView(chip)
                }
            }

            // Indicador de audio
            binding.imageViewAudioIndicator.visibility = if (memory.audioLocalPath != null || memory.audioRemoteUrl != null) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Indicador de tipo
            val hasPhoto = memory.photoLocalPath != null || memory.photoRemoteUrl != null
            val hasAudio = memory.audioLocalPath != null || memory.audioRemoteUrl != null
            binding.textViewTypeIndicator.text = when {
                hasPhoto && hasAudio -> "FOTO+AUDIO"
                hasPhoto -> "FOTO"
                hasAudio -> "AUDIO"
                else -> "RECUERDO"
            }

            // Cargar imagen si existe
            when {
                memory.photoLocalPath != null -> {
                    loadImageSafely(memory.photoLocalPath)
                }
                memory.photoRemoteUrl != null -> {
                    // TODO: Cargar imagen remota con Glide o Coil
                    binding.imageViewMemory.visibility = View.VISIBLE
                }
                else -> {
                    binding.imageViewMemory.visibility = View.VISIBLE
                }
            }

            // Click listener
            binding.root.setOnClickListener {
                onMemoryClick(memory)
            }
        }

        private fun loadImageSafely(imagePath: String, maxDimension: Int = 1024) {
            try {
                // Primero obtener las dimensiones sin cargar la imagen completa
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(imagePath, options)

                // Calcular el factor de escala
                val scaleFactor = min(
                    options.outWidth / maxDimension,
                    options.outHeight / maxDimension
                )

                // Cargar la imagen redimensionada
                val loadOptions = BitmapFactory.Options().apply {
                    inSampleSize = if (scaleFactor > 1) scaleFactor else 1
                }

                val bitmap = BitmapFactory.decodeFile(imagePath, loadOptions)
                if (bitmap != null) {
                    binding.imageViewMemory.setImageBitmap(bitmap)
                    binding.imageViewMemory.visibility = View.VISIBLE
                } else {
                    binding.imageViewMemory.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.imageViewMemory.visibility = View.VISIBLE
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
