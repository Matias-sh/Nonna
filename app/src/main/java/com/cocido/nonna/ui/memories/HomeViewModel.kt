package com.cocido.nonna.ui.memories

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.MemoryType
import com.cocido.nonna.domain.model.PersonId
import com.cocido.nonna.domain.model.UserId
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.usecase.GetCurrentUserIdUseCase
import com.cocido.nonna.domain.usecase.GetPhrasesUseCase
import com.cocido.nonna.domain.usecase.ListMemoriesByVaultUseCase
import com.cocido.nonna.domain.usecase.GetVaultsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla principal (Home/Cofre)
 * Maneja la lógica de carga de recuerdos, filtros y estadísticas
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val listMemoriesByVaultUseCase: ListMemoriesByVaultUseCase,
    private val getVaultsUseCase: GetVaultsUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getPhrasesUseCase: GetPhrasesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var allMemories = listOf<Memory>()
    private var currentFilter: MemoryType? = null
    
    fun loadMemories() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Iniciando carga de recuerdos")
            _uiState.value = HomeUiState.Loading
            
            try {
                // Obtener userId del usuario autenticado
                Log.d("HomeViewModel", "Obteniendo ID del usuario actual")
                val currentUserId = getCurrentUserIdUseCase() 
                    ?: throw IllegalStateException("Usuario no autenticado")
                Log.d("HomeViewModel", "Usuario autenticado: $currentUserId")
                
                // Obtener el primer baúl del usuario
                Log.d("HomeViewModel", "Obteniendo baúles del usuario")
                val vaultsResult = getVaultsUseCase(currentUserId)
                if (vaultsResult.isFailure) {
                    Log.e("HomeViewModel", "Error al obtener baúles: ${vaultsResult.exceptionOrNull()?.message}")
                    _uiState.value = HomeUiState.Error(
                        message = "Error al cargar los baúles: ${vaultsResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }
                
                val vaults = vaultsResult.getOrNull() ?: emptyList()
                Log.d("HomeViewModel", "Baúles obtenidos: ${vaults.size}")
                
                if (vaults.isEmpty()) {
                    Log.d("HomeViewModel", "No hay baúles, mostrando estado vacío")
                    _uiState.value = HomeUiState.Success(
                        memories = emptyList(),
                        peopleCount = 0,
                        phrasesCount = 0,
                        currentVault = null
                    )
                    return@launch
                }
                
                Log.d("HomeViewModel", "Usando baúl: ${vaults.first().name} (ID: ${vaults.first().id.value})")
                
                // Usar el primer baúl disponible
                val currentVaultId = vaults.first().id
                
                Log.d("HomeViewModel", "Cargando recuerdos del baúl: ${currentVaultId.value}")
                listMemoriesByVaultUseCase(currentVaultId)
                    .catch { e ->
                        Log.e("HomeViewModel", "Error al cargar recuerdos: ${e.message}", e)
                        _uiState.value = HomeUiState.Error(
                            message = e.message ?: "Error al cargar los recuerdos"
                        )
                    }
                    .collect { memories ->
                        Log.d("HomeViewModel", "Recuerdos cargados: ${memories.size}")
                        allMemories = memories
                        
                        // Contar personas únicas en todos los recuerdos
                        val peopleCount = memories.flatMap { it.people }.distinct().size
                        Log.d("HomeViewModel", "Personas únicas encontradas: $peopleCount")
                        
                        // TODO: Implementar conteo real de frases desde el repositorio de conversación
                        // Por ahora, calculamos basándose en los recuerdos que tienen audio
                        val phrasesCount = memories.count { it.audioRemoteUrl != null }
                        Log.d("HomeViewModel", "Frases con audio: $phrasesCount")
                        
                        val filteredMemories = applyFilter(allMemories, currentFilter)
                        Log.d("HomeViewModel", "Recuerdos filtrados: ${filteredMemories.size}")

                        _uiState.value = HomeUiState.Success(
                            memories = filteredMemories,
                            peopleCount = peopleCount,
                            phrasesCount = phrasesCount,
                            currentVault = vaults.first()
                        )
                        Log.d("HomeViewModel", "Estado actualizado exitosamente")
                    }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al cargar recuerdos: ${e.message}", e)
                _uiState.value = HomeUiState.Error(
                    message = e.message ?: "Error al cargar los recuerdos"
                )
            }
        }
    }
    
    fun filterByType(type: MemoryType?) {
        currentFilter = type
        val filteredMemories = applyFilter(allMemories, type)

        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(memories = filteredMemories)
        }
    }

    fun switchToVault(vaultId: VaultId) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            try {
                listMemoriesByVaultUseCase(vaultId)
                    .catch { e ->
                        _uiState.value = HomeUiState.Error(
                            message = e.message ?: "Error al cargar los recuerdos del baúl"
                        )
                    }
                    .collect { memories ->
                        allMemories = memories

                        // Obtener información del vault
                        val currentUserId = getCurrentUserIdUseCase() 
                            ?: throw IllegalStateException("Usuario no autenticado")
                        val vaultsResult = getVaultsUseCase(currentUserId)
                        val vault = vaultsResult.getOrNull()?.find { it.id == vaultId }

                        val peopleCount = memories.flatMap { it.people }.distinct().size
                        
                        // Obtener conteo real de frases
                        val phrasesResult = getPhrasesUseCase()
                        val phrasesCount = phrasesResult.getOrNull()?.size ?: 0

                        val filteredMemories = applyFilter(allMemories, currentFilter)

                        _uiState.value = HomeUiState.Success(
                            memories = filteredMemories,
                            peopleCount = peopleCount,
                            phrasesCount = phrasesCount,
                            currentVault = vault
                        )
                    }

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    message = e.message ?: "Error al cambiar de baúl"
                )
            }
        }
    }
    
    private fun applyFilter(memories: List<Memory>, filter: MemoryType?): List<Memory> {
        return if (filter == null) {
            memories
        } else {
            memories.filter { it.type == filter }
        }
    }
    
}

/**
 * Estados de la UI para la pantalla principal
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val memories: List<Memory>,
        val peopleCount: Int,
        val phrasesCount: Int,
        val currentVault: com.cocido.nonna.domain.model.Vault? = null
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

