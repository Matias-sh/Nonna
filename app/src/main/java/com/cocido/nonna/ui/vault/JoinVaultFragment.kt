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
import com.cocido.nonna.databinding.FragmentJoinVaultBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JoinVaultFragment : Fragment() {
    
    private var _binding: FragmentJoinVaultBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: JoinVaultViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinVaultBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupClickListeners() {
        binding.btnJoinVault.setOnClickListener {
            joinVault()
        }
        
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is JoinVaultUiState.Loading -> {
                        showLoading(true)
                        binding.btnJoinVault.isEnabled = false
                    }
                    is JoinVaultUiState.Success -> {
                        showLoading(false)
                        binding.btnJoinVault.isEnabled = true
                        Toast.makeText(requireContext(), "¡Te has unido al baúl exitosamente!", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is JoinVaultUiState.Error -> {
                        showLoading(false)
                        binding.btnJoinVault.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    is JoinVaultUiState.Idle -> {
                        showLoading(false)
                        binding.btnJoinVault.isEnabled = true
                    }
                }
            }
        }
    }
    
    private fun joinVault() {
        val vaultCode = binding.etVaultCode.text.toString().trim()
        
        if (vaultCode.isEmpty()) {
            binding.tilVaultCode.error = "El código del baúl es requerido"
            return
        }
        
        if (vaultCode.length < 6) {
            binding.tilVaultCode.error = "El código debe tener al menos 6 caracteres"
            return
        }
        
        binding.tilVaultCode.error = null
        
        viewModel.joinVault(vaultCode)
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
