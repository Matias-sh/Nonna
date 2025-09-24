package com.cocido.nonna.ui.memories

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.cocido.nonna.R
import com.cocido.nonna.adapters.MemoriesAdapter
import com.cocido.nonna.databinding.FragmentHomeBinding
import com.cocido.nonna.domain.model.MemoryType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment principal que muestra el cofre de recuerdos
 * Grid de recuerdos sensoriales con filtros y estadísticas
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var memoriesAdapter: MemoriesAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupFilters()
        setupClickListeners()
        observeViewModel()

        // Cargar datos iniciales
        viewModel.loadMemories()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_manage_vaults -> {
                findNavController().navigate(R.id.vaultManagementFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupToolbar() {
        setHasOptionsMenu(true)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            onOptionsItemSelected(menuItem)
        }
    }
    
    private fun setupRecyclerView() {
        memoriesAdapter = MemoriesAdapter { memory ->
            // Navegar al detalle del recuerdo
            val action = HomeFragmentDirections.actionHomeToMemoryDetail(memory.id.value)
            findNavController().navigate(action)
        }
        
        binding.recyclerViewMemories.apply {
            adapter = memoriesAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)
        }
    }
    
    private fun setupFilters() {
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.filterByType(null)
                clearOtherChips()
            }
        }
        
        binding.chipPhotos.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.filterByType(MemoryType.PHOTO_WITH_AUDIO)
                clearOtherChips()
            }
        }
        
        binding.chipAudio.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.filterByType(MemoryType.AUDIO_ONLY)
                clearOtherChips()
            }
        }
        
        binding.chipRecipes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.filterByType(MemoryType.RECIPE)
                clearOtherChips()
            }
        }
    }
    
    private fun clearOtherChips() {
        binding.chipAll.isChecked = false
        binding.chipPhotos.isChecked = false
        binding.chipAudio.isChecked = false
        binding.chipRecipes.isChecked = false
    }
    
    private fun setupClickListeners() {
        binding.buttonAddFirstMemory.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_create_memory)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                Log.d("HomeFragment", "UI State changed: ${state.javaClass.simpleName}")
                when (state) {
                    is HomeUiState.Loading -> {
                        Log.d("HomeFragment", "Showing loading state")
                        showLoading(true)
                        showEmptyState(false)
                    }
                    is HomeUiState.Success -> {
                        Log.d("HomeFragment", "Showing success state: ${state.memories.size} memories, ${state.peopleCount} people, ${state.phrasesCount} phrases")
                        showLoading(false)
                        updateUI(state)
                    }
                    is HomeUiState.Error -> {
                        Log.e("HomeFragment", "Showing error state: ${state.message}")
                        showLoading(false)
                        showError(state.message)
                    }
                }
            }
        }
    }
    
    private fun updateUI(state: HomeUiState.Success) {
        memoriesAdapter.submitList(state.memories)
        
        // Actualizar estadísticas
        binding.textViewMemoriesCount.text = state.memories.size.toString()
        binding.textViewPeopleCount.text = state.peopleCount.toString()
        binding.textViewPhrasesCount.text = state.phrasesCount.toString()
        
        // Mostrar estado vacío si no hay recuerdos
        showEmptyState(state.memories.isEmpty())
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.layoutEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewMemories.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun showError(message: String) {
        // TODO: Implementar snackbar o toast para errores
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


