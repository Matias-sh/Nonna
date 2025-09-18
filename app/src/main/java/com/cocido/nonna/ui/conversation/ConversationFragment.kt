package com.cocido.nonna.ui.conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocido.nonna.R
import com.cocido.nonna.adapters.PhrasesAdapter
import com.cocido.nonna.databinding.FragmentConversationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para el modo conversación
 * Lista de frases típicas familiares con reproducción de audio o TTS
 */
@AndroidEntryPoint
class ConversationFragment : Fragment() {
    
    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ConversationViewModel by viewModels()
    private lateinit var phrasesAdapter: PhrasesAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
        
        // Cargar datos iniciales
        viewModel.loadPhrases()
    }
    
    private fun setupRecyclerView() {
        phrasesAdapter = PhrasesAdapter(
            onPhraseClick = { phrase ->
                viewModel.playPhrase(phrase)
            },
            onPlayClick = { phrase ->
                viewModel.playPhrase(phrase)
            }
        )
        
        binding.recyclerViewPhrases.apply {
            adapter = phrasesAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ConversationUiState.Loading -> {
                        showLoading(true)
                        showEmptyState(false)
                    }
                    is ConversationUiState.Success -> {
                        showLoading(false)
                        updateUI(state)
                    }
                    is ConversationUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    is ConversationUiState.Playing -> {
                        updatePlaybackUI(state)
                    }
                }
            }
        }
    }
    
    private fun updateUI(state: ConversationUiState.Success) {
        phrasesAdapter.submitList(state.phrases)
        showEmptyState(state.phrases.isEmpty())
    }
    
    private fun updatePlaybackUI(state: ConversationUiState.Playing) {
        // TODO: Actualizar UI de reproducción
        // Cambiar botón a "Pausar" o mostrar indicador de reproducción
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.layoutEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewPhrases.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


