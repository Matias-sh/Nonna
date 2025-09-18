package com.cocido.nonna.ui.memories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.MemoryType
import com.cocido.nonna.domain.model.PersonId
import com.cocido.nonna.domain.model.VaultId
import com.cocido.nonna.domain.usecase.ListMemoriesByVaultUseCase
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
    private val listMemoriesByVaultUseCase: ListMemoriesByVaultUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var allMemories = listOf<Memory>()
    private var currentFilter: MemoryType? = null
    
    fun loadMemories() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            
            try {
                // TODO: Obtener vaultId del usuario autenticado
                val currentVaultId = VaultId("vault_1") // Temporal
                
                listMemoriesByVaultUseCase(currentVaultId)
                    .catch { e ->
                        _uiState.value = HomeUiState.Error(
                            message = e.message ?: "Error al cargar los recuerdos"
                        )
                    }
                    .collect { memories ->
                        allMemories = memories
                        
                        // TODO: Implementar conteo real de personas y frases
                        val peopleCount = memories.flatMap { it.people }.distinct().size
                        val phrasesCount = 12 // Temporal
                        
                        val filteredMemories = applyFilter(allMemories, currentFilter)
                        
                        _uiState.value = HomeUiState.Success(
                            memories = filteredMemories,
                            peopleCount = peopleCount,
                            phrasesCount = phrasesCount
                        )
                    }
                
            } catch (e: Exception) {
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
    
    private fun applyFilter(memories: List<Memory>, filter: MemoryType?): List<Memory> {
        return if (filter == null) {
            memories
        } else {
            memories.filter { it.type == filter }
        }
    }
    
    private fun createDummyMemories(): List<Memory> {
        val vaultId = VaultId("vault_1")
        val personId1 = PersonId("person_1")
        val personId2 = PersonId("person_2")
        
        return listOf(
            Memory(
                id = MemoryId("memory_1"),
                vaultId = vaultId,
                title = "Abuela cocinando",
                type = MemoryType.PHOTO_WITH_AUDIO,
                photoLocalPath = null,
                photoRemoteUrl = "https://picsum.photos/400/300?random=1",
                audioLocalPath = null,
                audioRemoteUrl = null,
                hasTranscript = false,
                transcript = null,
                people = listOf(personId1),
                tags = listOf("cocina", "familia", "abuela"),
                dateTaken = System.currentTimeMillis() - 86400000L * 30, // 30 días atrás
                location = "Casa de la abuela",
                createdAt = System.currentTimeMillis() - 86400000L * 30,
                updatedAt = System.currentTimeMillis() - 86400000L * 30
            ),
            Memory(
                id = MemoryId("memory_2"),
                vaultId = vaultId,
                title = "Receta del pastel de manzana",
                type = MemoryType.RECIPE,
                photoLocalPath = null,
                photoRemoteUrl = "https://picsum.photos/400/300?random=2",
                audioLocalPath = null,
                audioRemoteUrl = null,
                hasTranscript = true,
                transcript = "Ingredientes: 3 manzanas, 2 tazas de harina, 1 taza de azúcar...",
                people = listOf(personId1, personId2),
                tags = listOf("receta", "postre", "manzana"),
                dateTaken = System.currentTimeMillis() - 86400000L * 15, // 15 días atrás
                location = "Cocina familiar",
                createdAt = System.currentTimeMillis() - 86400000L * 15,
                updatedAt = System.currentTimeMillis() - 86400000L * 15
            ),
            Memory(
                id = MemoryId("memory_3"),
                vaultId = vaultId,
                title = "Historia de la boda",
                type = MemoryType.AUDIO_ONLY,
                photoLocalPath = null,
                photoRemoteUrl = null,
                audioLocalPath = null,
                audioRemoteUrl = null,
                hasTranscript = false,
                transcript = null,
                people = listOf(personId1),
                tags = listOf("historia", "boda", "familia"),
                dateTaken = System.currentTimeMillis() - 86400000L * 60, // 60 días atrás
                location = "Salón familiar",
                createdAt = System.currentTimeMillis() - 86400000L * 60,
                updatedAt = System.currentTimeMillis() - 86400000L * 60
            ),
            Memory(
                id = MemoryId("memory_4"),
                vaultId = vaultId,
                title = "Foto del jardín",
                type = MemoryType.PHOTO_ONLY,
                photoLocalPath = null,
                photoRemoteUrl = "https://picsum.photos/400/300?random=4",
                audioLocalPath = null,
                audioRemoteUrl = null,
                hasTranscript = false,
                transcript = null,
                people = listOf(personId2),
                tags = listOf("jardín", "naturaleza", "verano"),
                dateTaken = System.currentTimeMillis() - 86400000L * 7, // 7 días atrás
                location = "Jardín de casa",
                createdAt = System.currentTimeMillis() - 86400000L * 7,
                updatedAt = System.currentTimeMillis() - 86400000L * 7
            ),
            Memory(
                id = MemoryId("memory_5"),
                vaultId = vaultId,
                title = "Nota sobre el árbol genealógico",
                type = MemoryType.NOTE,
                photoLocalPath = null,
                photoRemoteUrl = null,
                audioLocalPath = null,
                audioRemoteUrl = null,
                hasTranscript = false,
                transcript = null,
                people = listOf(personId1, personId2),
                tags = listOf("genealogía", "historia", "familia"),
                dateTaken = System.currentTimeMillis() - 86400000L * 3, // 3 días atrás
                location = "Archivo familiar",
                createdAt = System.currentTimeMillis() - 86400000L * 3,
                updatedAt = System.currentTimeMillis() - 86400000L * 3
            ),
            Memory(
                id = MemoryId("memory_6"),
                vaultId = vaultId,
                title = "Cena familiar con audio",
                type = MemoryType.PHOTO_WITH_AUDIO,
                photoLocalPath = null,
                photoRemoteUrl = "https://picsum.photos/400/300?random=6",
                audioLocalPath = null,
                audioRemoteUrl = null,
                hasTranscript = false,
                transcript = null,
                people = listOf(personId1, personId2),
                tags = listOf("cena", "familia", "tradición"),
                dateTaken = System.currentTimeMillis() - 86400000L * 1, // 1 día atrás
                location = "Mesa del comedor",
                createdAt = System.currentTimeMillis() - 86400000L * 1,
                updatedAt = System.currentTimeMillis() - 86400000L * 1
            )
        )
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
        val phrasesCount: Int
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

