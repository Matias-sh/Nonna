package com.cocido.nonna.ui.timeline

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
import com.cocido.nonna.adapters.TimelineAdapter
import com.cocido.nonna.databinding.FragmentTimelineBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para mostrar la línea de tiempo de recuerdos
 * Lista cronológica con filtros por año
 */
@AndroidEntryPoint
class TimelineFragment : Fragment() {
    
    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TimelineViewModel by viewModels()
    private lateinit var timelineAdapter: TimelineAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFilters()
        observeViewModel()
        
        // Cargar datos iniciales
        viewModel.loadTimeline()
    }
    
    private fun setupRecyclerView() {
        timelineAdapter = TimelineAdapter { memory ->
            // Navegar al detalle del recuerdo
            findNavController().navigate("memory_detail/${memory.id.value}")
        }
        
        binding.recyclerViewTimeline.apply {
            adapter = timelineAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }
    
    private fun setupFilters() {
        binding.chipAllYears.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.filterByYear(null)
                clearOtherYearChips()
            }
        }
        
        binding.chip2024.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.filterByYear(2024)
                clearOtherYearChips()
            }
        }
        
        binding.chip2023.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.filterByYear(2023)
                clearOtherYearChips()
            }
        }
        
        binding.chip2022.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.filterByYear(2022)
                clearOtherYearChips()
            }
        }
    }
    
    private fun clearOtherYearChips() {
        binding.chipAllYears.isChecked = false
        binding.chip2024.isChecked = false
        binding.chip2023.isChecked = false
        binding.chip2022.isChecked = false
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is TimelineUiState.Loading -> {
                        showLoading(true)
                        showEmptyState(false)
                    }
                    is TimelineUiState.Success -> {
                        showLoading(false)
                        updateUI(state)
                    }
                    is TimelineUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    is TimelineUiState.Idle -> {
                        showLoading(false)
                    }
                }
            }
        }
    }
    
    private fun updateUI(state: TimelineUiState.Success) {
        timelineAdapter.submitList(state.memories)
        showEmptyState(state.memories.isEmpty())
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.layoutEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewTimeline.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun showError(message: String) {
        // TODO: Implementar snackbar o toast para errores
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


