package com.cocido.nonna.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.core.logging.Logger
import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.Vault
import com.cocido.nonna.domain.repository.AuthRepository
import com.cocido.nonna.domain.usecase.GetMemoriesUseCase
import com.cocido.nonna.domain.usecase.GetVaultsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultManagementViewModel @Inject constructor(
    private val getVaultsUseCase: GetVaultsUseCase,
    private val getMemoriesUseCase: GetMemoriesUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VaultManagementUiState>(VaultManagementUiState.Idle)
    val uiState: StateFlow<VaultManagementUiState> = _uiState.asStateFlow()

    private var currentVaults: List<Vault> = emptyList()
    private var selectedVault: Vault? = null

    init {
        loadVaults()
    }

    fun loadVaults() {
        viewModelScope.launch {
            try {
                _uiState.value = VaultManagementUiState.Loading

                val userId = authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.value = VaultManagementUiState.Error("Usuario no autenticado")
                    return@launch
                }

                Logger.d("Loading vaults for user: ${userId.value}")

                getVaultsUseCase(userId)
                    .onSuccess { vaults ->
                        currentVaults = vaults
                        Logger.d("Vaults loaded: ${vaults.size}")

                        // Seleccionar el primer baúl por defecto
                        if (vaults.isNotEmpty()) {
                            selectVault(vaults[0])
                        } else {
                            _uiState.value = VaultManagementUiState.Success(
                                vaults = emptyList(),
                                selectedVault = null,
                                memories = emptyList()
                            )
                        }
                    }
                    .onFailure { exception ->
                        Logger.e("Error loading vaults", throwable = exception)
                        _uiState.value = VaultManagementUiState.Error(
                            message = exception.message ?: "Error al cargar los baúles"
                        )
                    }
            } catch (e: Exception) {
                Logger.e("Error in loadVaults", throwable = e)
                _uiState.value = VaultManagementUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    fun selectVault(vault: Vault) {
        viewModelScope.launch {
            try {
                selectedVault = vault
                Logger.d("Vault selected: ${vault.name} (${vault.id.value})")

                _uiState.value = VaultManagementUiState.Loading

                // Cargar recuerdos del baúl seleccionado
                val memories = getMemoriesUseCase(vault.id).first()
                Logger.d("Memories loaded for vault: ${memories.size}")

                _uiState.value = VaultManagementUiState.Success(
                    vaults = currentVaults,
                    selectedVault = vault,
                    memories = memories
                )
            } catch (e: Exception) {
                Logger.e("Error loading memories for vault", throwable = e)
                _uiState.value = VaultManagementUiState.Error("Error al cargar recuerdos: ${e.message}")
            }
        }
    }

    fun refreshVaults() {
        loadVaults()
    }

    fun refreshCurrentVault() {
        selectedVault?.let { selectVault(it) }
    }
}

sealed class VaultManagementUiState {
    object Idle : VaultManagementUiState()
    object Loading : VaultManagementUiState()
    data class Success(
        val vaults: List<Vault>,
        val selectedVault: Vault?,
        val memories: List<Memory>
    ) : VaultManagementUiState()
    data class Error(val message: String) : VaultManagementUiState()
}
