package com.cocido.nonna.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.usecase.CreateVaultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateVaultViewModel @Inject constructor(
    private val createVaultUseCase: CreateVaultUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CreateVaultUiState>(CreateVaultUiState.Idle)
    val uiState: StateFlow<CreateVaultUiState> = _uiState.asStateFlow()
    
    fun createVault(name: String, description: String?) {
        viewModelScope.launch {
            _uiState.value = CreateVaultUiState.Loading
            
            createVaultUseCase(name, description)
                .onSuccess { vault ->
                    _uiState.value = CreateVaultUiState.Success(vault)
                }
                .onFailure { exception ->
                    _uiState.value = CreateVaultUiState.Error(
                        message = exception.message ?: "Error al crear el baúl"
                    )
                }
        }
    }
}

sealed class CreateVaultUiState {
    object Idle : CreateVaultUiState()
    object Loading : CreateVaultUiState()
    data class Success(val vault: Vault) : CreateVaultUiState()
    data class Error(val message: String) : CreateVaultUiState()
}
