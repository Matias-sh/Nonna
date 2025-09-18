package com.cocido.nonna.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cocido.nonna.R
import com.cocido.nonna.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para el registro de nuevos usuarios
 * Permite a los usuarios crear una cuenta con email y contraseña
 */
@AndroidEntryPoint
class RegisterFragment : Fragment() {
    
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: RegisterViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.buttonRegister.setOnClickListener {
            val name = binding.editTextName.text?.toString() ?: ""
            val email = binding.editTextEmail.text?.toString() ?: ""
            val password = binding.editTextPassword.text?.toString() ?: ""
            val confirmPassword = binding.editTextConfirmPassword.text?.toString() ?: ""
            
            if (validateInput(name, email, password, confirmPassword)) {
                viewModel.register(name, email, password)
            }
        }
        
        binding.textViewLoginLink.setOnClickListener {
            // Navegar al fragment de login
            findNavController().navigate(R.id.action_register_to_login)
        }
    }
    
    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true
        
        // Validar nombre
        if (name.isBlank()) {
            binding.textInputLayoutName.error = "El nombre es requerido"
            isValid = false
        } else {
            binding.textInputLayoutName.error = null
        }
        
        // Validar email
        if (email.isBlank()) {
            binding.textInputLayoutEmail.error = "El email es requerido"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayoutEmail.error = "Email inválido"
            isValid = false
        } else {
            binding.textInputLayoutEmail.error = null
        }
        
        // Validar contraseña
        if (password.isBlank()) {
            binding.textInputLayoutPassword.error = "La contraseña es requerida"
            isValid = false
        } else if (password.length < 6) {
            binding.textInputLayoutPassword.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            binding.textInputLayoutPassword.error = null
        }
        
        // Validar confirmación de contraseña
        if (confirmPassword.isBlank()) {
            binding.textInputLayoutConfirmPassword.error = "Confirma tu contraseña"
            isValid = false
        } else if (password != confirmPassword) {
            binding.textInputLayoutConfirmPassword.error = "Las contraseñas no coinciden"
            isValid = false
        } else {
            binding.textInputLayoutConfirmPassword.error = null
        }
        
        return isValid
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is RegisterUiState.Loading -> {
                        showLoading(true)
                    }
                    is RegisterUiState.Success -> {
                        showLoading(false)
                        showSuccess("Cuenta creada exitosamente")
                        // Navegar al login después de un breve delay
                        kotlinx.coroutines.delay(1500)
                        findNavController().navigate(R.id.action_register_to_login)
                    }
                    is RegisterUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                }
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.buttonRegister.isEnabled = !show
        binding.editTextName.isEnabled = !show
        binding.editTextEmail.isEnabled = !show
        binding.editTextPassword.isEnabled = !show
        binding.editTextConfirmPassword.isEnabled = !show
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


