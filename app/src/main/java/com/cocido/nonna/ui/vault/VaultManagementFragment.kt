package com.cocido.nonna.ui.vault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocido.nonna.R
import com.cocido.nonna.databinding.FragmentVaultManagementBinding
import com.cocido.nonna.domain.model.Vault
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VaultManagementFragment : Fragment() {

    private var _binding: FragmentVaultManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VaultManagementViewModel by viewModels()
    private lateinit var memoriesAdapter: MemoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVaultManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupUI() {
        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Configurar FAB para agregar recuerdo
        binding.fabAddMemory.setOnClickListener {
            findNavController().navigate(R.id.action_vaultManagement_to_createMemory)
        }

        // Configurar botón de crear primer recuerdo
        binding.buttonAddFirstMemory.setOnClickListener {
            findNavController().navigate(R.id.action_vaultManagement_to_createMemory)
        }

        // Configurar botón de crear baúl
        binding.buttonCreateVault.setOnClickListener {
            findNavController().navigate(R.id.action_vaultManagement_to_createVault)
        }
    }

    private fun setupRecyclerView() {
        memoriesAdapter = MemoriesAdapter(
            onMemoryClick = { memory ->
                // Navegar al detalle del recuerdo
                val action = VaultManagementFragmentDirections
                    .actionVaultManagementToMemoryDetail(memory.id.value)
                findNavController().navigate(action)
            }
        )

        binding.recyclerViewMemories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = memoriesAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is VaultManagementUiState.Idle -> {
                        showLoading(false)
                    }
                    is VaultManagementUiState.Loading -> {
                        showLoading(true)
                    }
                    is VaultManagementUiState.Success -> {
                        showLoading(false)
                        handleSuccessState(state)
                    }
                    is VaultManagementUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                }
            }
        }
    }

    private fun handleSuccessState(state: VaultManagementUiState.Success) {
        if (state.vaults.isEmpty()) {
            // No hay baúles
            binding.layoutNoVaults.visibility = View.VISIBLE
            binding.vaultSelectorContainer.visibility = View.GONE
            binding.recyclerViewMemories.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.GONE
            binding.fabAddMemory.visibility = View.GONE
        } else {
            // Hay baúles
            binding.layoutNoVaults.visibility = View.GONE
            binding.vaultSelectorContainer.visibility = View.VISIBLE
            binding.fabAddMemory.visibility = View.VISIBLE

            // Actualizar chips de baúles
            updateVaultChips(state.vaults, state.selectedVault)

            if (state.memories.isEmpty()) {
                // El baúl está vacío
                binding.recyclerViewMemories.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
            } else {
                // Hay recuerdos
                binding.recyclerViewMemories.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
                memoriesAdapter.submitList(state.memories)
            }
        }
    }

    private fun updateVaultChips(vaults: List<Vault>, selectedVault: Vault?) {
        binding.chipGroupVaults.removeAllViews()

        vaults.forEach { vault ->
            val chip = Chip(requireContext()).apply {
                text = vault.name
                isCheckable = true
                isChecked = vault.id == selectedVault?.id

                setOnClickListener {
                    viewModel.selectVault(vault)
                }
            }
            binding.chipGroupVaults.addView(chip)
        }
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
