package com.cocido.nonna.ui.genealogy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cocido.nonna.R
import com.cocido.nonna.databinding.FragmentGenealogyBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para mostrar el árbol genealógico interactivo
 * Canvas personalizado con zoom, pan y gestos táctiles
 */
@AndroidEntryPoint
class GenealogyFragment : Fragment() {
    
    private var _binding: FragmentGenealogyBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: GenealogyViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenealogyBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        
        // Cargar datos iniciales
        viewModel.loadGenealogy()
    }
    
    private fun setupUI() {
        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Configurar controles de zoom
        binding.buttonZoomIn.setOnClickListener {
            binding.genealogyCanvas.zoomIn()
        }
        
        binding.buttonZoomOut.setOnClickListener {
            binding.genealogyCanvas.zoomOut()
        }
        
        binding.buttonResetZoom.setOnClickListener {
            binding.genealogyCanvas.resetZoom()
        }
        
        // Configurar click en persona
        binding.genealogyCanvas.setOnPersonClickListener { person ->
            // TODO: Mostrar detalles de la persona o navegar a sus recuerdos
            showPersonDetails(person)
        }
        
        // Botón para agregar persona
        binding.buttonAddPerson.setOnClickListener {
            // TODO: Navegar a pantalla de agregar persona
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is GenealogyUiState.Loading -> {
                        showLoading(true)
                        showEmptyState(false)
                    }
                    is GenealogyUiState.Success -> {
                        showLoading(false)
                        updateUI(state)
                    }
                    is GenealogyUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    is GenealogyUiState.Idle -> {
                        showLoading(false)
                    }
                }
            }
        }
    }
    
    private fun updateUI(state: GenealogyUiState.Success) {
        binding.genealogyCanvas.setData(state.persons, state.relations)
        showEmptyState(state.persons.isEmpty())
    }
    
    private fun showPersonDetails(person: com.cocido.nonna.domain.model.Person) {
        // TODO: Implementar diálogo o navegación a detalles de persona
        // Por ahora, mostrar un toast
        android.widget.Toast.makeText(
            requireContext(),
            "Persona: ${person.fullName}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.layoutEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.genealogyCanvas.visibility = if (show) View.GONE else View.VISIBLE
        binding.layoutZoomControls.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




