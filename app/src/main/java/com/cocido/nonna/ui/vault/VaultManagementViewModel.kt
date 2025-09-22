package com.cocido.nonna.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.usecase.GetVaultsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultManagementViewModel @Inject constructor(
    private val getVaultsUseCase: GetVaultsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<VaultManagementUiState>(VaultManagementUiState.Idle)
    val uiState: StateFlow<VaultManagementUiState> = _uiState.asStateFlow()
    
    fun loadVaults() {
        viewModelScope.launch {
            _uiState.value = VaultManagementUiState.Loading
            
            getVaultsUseCase()
                .onSuccess { vaults ->
                    _uiState.value = VaultManagementUiState.Success(vaults)
                }
                .onFailure { exception ->
                    _uiState.value = VaultManagementUiState.Error(
                        message = exception.message ?: "Error al cargar los baúles"
                    )
                }
        }
    }
    
    fun refreshVaults() {
        loadVaults()
    }
}

sealed class VaultManagementUiState {
    object Idle : VaultManagementUiState()
    object Loading : VaultManagementUiState()
    data class Success(val vaults: List<Vault>) : VaultManagementUiState()
    data class Error(val message: String) : VaultManagementUiState()
}
