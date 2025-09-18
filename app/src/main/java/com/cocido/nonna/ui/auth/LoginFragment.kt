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
import com.cocido.nonna.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para el login de usuarios
 * Permite a los usuarios iniciar sesión con email y contraseña
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {
    
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LoginViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text?.toString() ?: ""
            val password = binding.editTextPassword.text?.toString() ?: ""
            
            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }
        
        binding.textViewRegisterLink.setOnClickListener {
            // Navegar al fragment de registro
            findNavController().navigate(R.id.action_login_to_register)
        }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true
        
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
        
        return isValid
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is LoginUiState.Idle -> {
                        showLoading(false)
                    }
                    is LoginUiState.Loading -> {
                        showLoading(true)
                    }
                    is LoginUiState.Success -> {
                        showLoading(false)
                        // Navegar a la pantalla principal
                        findNavController().navigate(R.id.action_login_to_home)
                    }
                    is LoginUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                }
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !show
        binding.editTextEmail.isEnabled = !show
        binding.editTextPassword.isEnabled = !show
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


