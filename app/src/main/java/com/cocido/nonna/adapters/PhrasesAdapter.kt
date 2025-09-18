package com.cocido.nonna.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cocido.nonna.databinding.ItemPhraseCardBinding
import com.cocido.nonna.domain.model.Phrase

/**
 * Adapter para mostrar frases típicas familiares
 * Cards grandes con texto y controles de reproducción
 */
class PhrasesAdapter(
    private val onPhraseClick: (Phrase) -> Unit,
    private val onPlayClick: (Phrase) -> Unit
) : ListAdapter<Phrase, PhrasesAdapter.PhraseViewHolder>(PhraseDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhraseViewHolder {
        val binding = ItemPhraseCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhraseViewHolder(binding, onPhraseClick, onPlayClick)
    }
    
    override fun onBindViewHolder(holder: PhraseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class PhraseViewHolder(
        private val binding: ItemPhraseCardBinding,
        private val onPhraseClick: (Phrase) -> Unit,
        private val onPlayClick: (Phrase) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(phrase: Phrase) {
            binding.apply {
                // Configurar click listeners
                root.setOnClickListener { onPhraseClick(phrase) }
                buttonPlay.setOnClickListener { onPlayClick(phrase) }
                
                // Texto de la frase
                textViewPhrase.text = "\"${phrase.text}\""
                
                // Persona que dijo la frase
                textViewPerson.text = "Persona ${phrase.personId?.value ?: "Desconocida"}"
                
                // Indicador de audio
                imageViewAudioIndicator.visibility = if (phrase.audioLocalPath != null || phrase.audioRemoteUrl != null) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                
                // Configurar botón de reproducción
                if (phrase.audioLocalPath != null || phrase.audioRemoteUrl != null) {
                    buttonPlay.text = "Reproducir Audio"
                } else {
                    buttonPlay.text = "Reproducir con Voz"
                }
            }
        }
    }
    
    private class PhraseDiffCallback : DiffUtil.ItemCallback<Phrase>() {
        override fun areItemsTheSame(oldItem: Phrase, newItem: Phrase): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Phrase, newItem: Phrase): Boolean {
            return oldItem == newItem
        }
    }
}


