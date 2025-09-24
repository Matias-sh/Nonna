package com.cocido.nonna.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cocido.nonna.R
import com.cocido.nonna.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fragment para configuración de la aplicación
 * Preferencias de usuario, exportación de datos y configuración de cuenta
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels()
    
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        loadCurrentSettings()
    }
    
    private fun setupUI() {
        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Configurar switches
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkMode(isChecked)
        }
        
        binding.switchNostalgicRoom.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNostalgicRoom(isChecked)
        }
        
        // Configurar botones
        binding.buttonLogout.setOnClickListener {
            viewModel.logout()
        }
        
        binding.buttonExportData.setOnClickListener {
            viewModel.exportData()
        }
        
        binding.buttonBackupData.setOnClickListener {
            viewModel.createBackup()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SettingsUiState.Loading -> {
                        // TODO: Mostrar indicador de carga
                    }
                    is SettingsUiState.Success -> {
                        updateUI(state)
                    }
                    is SettingsUiState.Error -> {
                        showError(state.message)
                    }
                    is SettingsUiState.LogoutSuccess -> {
                        // Navegar a pantalla de login y limpiar stack
                        findNavController().navigate(R.id.loginFragment)
                    }
                }
            }
        }
    }
    
    private fun loadCurrentSettings() {
        // Cargar configuración actual
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        val isNostalgicRoom = sharedPreferences.getBoolean("nostalgic_room", false)
        
        binding.switchDarkMode.isChecked = isDarkMode
        binding.switchNostalgicRoom.isChecked = isNostalgicRoom
        
        // Aplicar modo oscuro
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    private fun updateUI(state: SettingsUiState.Success) {
        // Actualizar información del usuario
        binding.textViewUserName.text = state.userName
        binding.textViewUserEmail.text = state.userEmail
        
        // Actualizar switches
        binding.switchDarkMode.isChecked = state.isDarkMode
        binding.switchNostalgicRoom.isChecked = state.isNostalgicRoom
    }
    
    private fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





