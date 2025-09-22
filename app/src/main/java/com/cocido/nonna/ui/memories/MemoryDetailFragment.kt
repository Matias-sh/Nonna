package com.cocido.nonna.ui.memories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.cocido.nonna.R
import com.cocido.nonna.databinding.FragmentMemoryDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para mostrar el detalle completo de un recuerdo
 * Incluye foto, audio, metadatos y transcripción
 */
@AndroidEntryPoint
class MemoryDetailFragment : Fragment() {
    
    private var _binding: FragmentMemoryDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MemoryDetailViewModel by viewModels()
    private val args: MemoryDetailFragmentArgs by navArgs()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        
        // Inicializar reproductor de audio
        viewModel.initializeAudioPlayer(requireContext())
        
        // Cargar recuerdo
        viewModel.loadMemory(args.memoryId)
    }
    
    private fun setupUI() {
        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Configurar controles de audio
        binding.buttonPlay.setOnClickListener {
            viewModel.togglePlayback()
        }
        
        binding.seekBarAudio.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.seekTo(progress)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MemoryDetailUiState.Loading -> {
                        showLoading(true)
                    }
                    is MemoryDetailUiState.Success -> {
                        showLoading(false)
                        updateUI(state)
                    }
                    is MemoryDetailUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    is MemoryDetailUiState.Playing -> {
                        updatePlaybackUI(state)
                    }
                }
            }
        }
    }
    
    private fun updateUI(state: MemoryDetailUiState.Success) {
        val memory = state.memory
        
        // Configurar toolbar
        binding.toolbar.title = memory.title
        
        // Título
        binding.textViewTitle.text = memory.title
        
        // Fecha
        memory.dateTaken?.let { timestamp ->
            val dateFormatter = java.text.SimpleDateFormat("dd 'de' MMMM, yyyy", java.util.Locale("es", "ES"))
            binding.textViewDate.text = dateFormatter.format(java.util.Date(timestamp))
        }
        
        // Ubicación
        memory.location?.let { location ->
            binding.textViewLocation.text = location
            binding.textViewLocation.visibility = View.VISIBLE
        }
        
        // Imagen
        val imageUrl = memory.photoRemoteUrl ?: memory.photoLocalPath
        if (imageUrl != null) {
            Glide.with(binding.imageViewMemory.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_photo)
                .error(R.drawable.ic_photo)
                .centerCrop()
                .into(binding.imageViewMemory)
        } else {
            // Mostrar placeholder según el tipo
            binding.imageViewMemory.setImageResource(
                when (memory.type) {
                    com.cocido.nonna.domain.model.MemoryType.AUDIO_ONLY -> R.drawable.ic_audio
                    com.cocido.nonna.domain.model.MemoryType.RECIPE -> R.drawable.ic_recipe
                    com.cocido.nonna.domain.model.MemoryType.NOTE -> R.drawable.ic_note
                    else -> R.drawable.ic_photo
                }
            )
        }
        
        // Audio
        if (memory.audioLocalPath != null || memory.audioRemoteUrl != null) {
            binding.layoutAudioControls.visibility = View.VISIBLE
            // TODO: Configurar ExoPlayer
        }
        
        // Tags
        binding.chipGroupTags.removeAllViews()
        memory.tags.forEach { tag ->
            val chip = com.google.android.material.chip.Chip(binding.chipGroupTags.context)
            chip.text = tag
            chip.isClickable = false
            binding.chipGroupTags.addView(chip)
        }
        
        // Personas
        binding.chipGroupPeople.removeAllViews()
        memory.people.forEach { personId ->
            // TODO: Cargar nombre de la persona desde el repositorio
            val chip = com.google.android.material.chip.Chip(binding.chipGroupPeople.context)
            chip.text = "Persona ${personId.value}"
            chip.isClickable = true
            chip.setOnClickListener {
                // TODO: Navegar al perfil de la persona
            }
            binding.chipGroupPeople.addView(chip)
        }
        
        // Transcripción
        memory.transcript?.let { transcript ->
            binding.textViewTranscript.text = transcript
            binding.textViewTranscript.visibility = View.VISIBLE
        }
    }
    
    private fun updatePlaybackUI(state: MemoryDetailUiState.Playing) {
        binding.buttonPlay.text = if (state.isPlaying) "Pausar" else "Reproducir"
        binding.buttonPlay.setIconResource(
            if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
        binding.textViewDuration.text = "${state.currentTime} / ${state.totalTime}"
        binding.seekBarAudio.progress = state.progress
        binding.seekBarAudio.max = state.maxProgress
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

