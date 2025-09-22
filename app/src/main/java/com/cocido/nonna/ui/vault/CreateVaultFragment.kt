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
import com.cocido.nonna.databinding.FragmentCreateVaultBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateVaultFragment : Fragment() {
    
    private var _binding: FragmentCreateVaultBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CreateVaultViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateVaultBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupClickListeners() {
        binding.btnCreateVault.setOnClickListener {
            createVault()
        }
        
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is CreateVaultUiState.Loading -> {
                        showLoading(true)
                        binding.btnCreateVault.isEnabled = false
                    }
                    is CreateVaultUiState.Success -> {
                        showLoading(false)
                        binding.btnCreateVault.isEnabled = true
                        Toast.makeText(requireContext(), "¡Baúl creado exitosamente!", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is CreateVaultUiState.Error -> {
                        showLoading(false)
                        binding.btnCreateVault.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    is CreateVaultUiState.Idle -> {
                        showLoading(false)
                        binding.btnCreateVault.isEnabled = true
                    }
                }
            }
        }
    }
    
    private fun createVault() {
        val name = binding.etVaultName.text.toString().trim()
        val description = binding.etVaultDescription.text.toString().trim()
        
        if (name.isEmpty()) {
            binding.tilVaultName.error = "El nombre del baúl es requerido"
            return
        }
        
        if (name.length < 3) {
            binding.tilVaultName.error = "El nombre debe tener al menos 3 caracteres"
            return
        }
        
        binding.tilVaultName.error = null
        
        viewModel.createVault(name, if (description.isEmpty()) null else description)
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
