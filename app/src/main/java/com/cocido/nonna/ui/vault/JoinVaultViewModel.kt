package com.cocido.nonna.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.usecase.JoinVaultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinVaultViewModel @Inject constructor(
    private val joinVaultUseCase: JoinVaultUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<JoinVaultUiState>(JoinVaultUiState.Idle)
    val uiState: StateFlow<JoinVaultUiState> = _uiState.asStateFlow()
    
    fun joinVault(vaultCode: String) {
        viewModelScope.launch {
            _uiState.value = JoinVaultUiState.Loading
            
            val vaultId = com.cocido.nonna.domain.model.VaultId(vaultCode)
            val userId = com.cocido.nonna.domain.model.UserId("user_1") // TODO: Get from auth
            
            joinVaultUseCase(vaultId, userId)
                .onSuccess { 
                    _uiState.value = JoinVaultUiState.Success("Te has unido al baúl exitosamente")
                }
                .onFailure { exception ->
                    _uiState.value = JoinVaultUiState.Error(
                        message = exception.message ?: "Error al unirse al baúl"
                    )
                }
        }
    }
}

sealed class JoinVaultUiState {
    object Idle : JoinVaultUiState()
    object Loading : JoinVaultUiState()
    data class Success(val message: String) : JoinVaultUiState()
    data class Error(val message: String) : JoinVaultUiState()
}
