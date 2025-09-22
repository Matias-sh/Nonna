package com.cocido.nonna.ui.vault

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
import com.cocido.nonna.adapters.VaultAdapter
import com.cocido.nonna.databinding.FragmentVaultManagementBinding
import com.cocido.nonna.domain.model.Vault
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VaultManagementFragment : Fragment() {
    
    private var _binding: FragmentVaultManagementBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: VaultManagementViewModel by viewModels()
    private lateinit var vaultAdapter: VaultAdapter
    
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
        
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        viewModel.loadVaults()
    }
    
    private fun setupRecyclerView() {
        vaultAdapter = VaultAdapter(
            onVaultClick = { vault ->
                // Navegar al home con el vault seleccionado
                findNavController().navigate(R.id.homeFragment)
            },
            onVaultSettingsClick = { vault ->
                // Por ahora, mostrar información del vault
                showVaultInfo(vault)
            }
        )
        
        binding.recyclerViewVaults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = vaultAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.fabCreateVault.setOnClickListener {
            findNavController().navigate(R.id.action_vaultManagement_to_createVault)
        }
        
        binding.fabJoinVault.setOnClickListener {
            findNavController().navigate(R.id.action_vaultManagement_to_joinVault)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is VaultManagementUiState.Loading -> {
                        showLoading(true)
                        showEmptyState(false)
                    }
                    is VaultManagementUiState.Success -> {
                        showLoading(false)
                        updateUI(state.vaults)
                    }
                    is VaultManagementUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    is VaultManagementUiState.Idle -> {
                        showLoading(false)
                        showEmptyState(false)
                    }
                }
            }
        }
    }
    
    private fun updateUI(vaults: List<Vault>) {
        vaultAdapter.submitList(vaults)
        showEmptyState(vaults.isEmpty())
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.layoutEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewVaults.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun showError(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
    }

    private fun showVaultInfo(vault: Vault) {
        val message = "Baúl: ${vault.name}\nCódigo: ${vault.id.value}\nMiembros: ${vault.memberUids.size + 1}"
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
