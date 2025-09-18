package com.cocido.nonna.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.MemoryRepositoryImpl
import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.model.MemoryId
import com.cocido.nonna.domain.model.MemoryType
import com.cocido.nonna.domain.model.PersonId
import com.cocido.nonna.domain.model.VaultId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * ViewModel para la línea de tiempo de recuerdos
 * Maneja la carga cronológica y filtros por año
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val memoryRepository: MemoryRepositoryImpl
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TimelineUiState>(TimelineUiState.Loading)
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()
    
    private var allMemories = listOf<Memory>()
    private var currentYearFilter: Int? = null
    
    fun loadTimeline() {
        viewModelScope.launch {
            _uiState.value = TimelineUiState.Loading
            
            try {
                // TODO: Obtener vaultId del usuario autenticado
                val currentVaultId = VaultId("vault_1") // Temporal
                
                memoryRepository.getMemoriesByVault(currentVaultId)
                    .catch { e ->
                        _uiState.value = TimelineUiState.Error(
                            message = e.message ?: "Error al cargar la línea de tiempo"
                        )
                    }
                    .collect { memories ->
                        allMemories = memories
                        val filteredMemories = applyYearFilter(allMemories, currentYearFilter)
                        val sortedMemories = sortMemoriesChronologically(filteredMemories)
                        
                        _uiState.value = TimelineUiState.Success(memories = sortedMemories)
                    }
                
            } catch (e: Exception) {
                _uiState.value = TimelineUiState.Error(
                    message = e.message ?: "Error al cargar la línea de tiempo"
                )
            }
        }
    }
    
    fun filterByYear(year: Int?) {
        currentYearFilter = year
        val filteredMemories = applyYearFilter(allMemories, year)
        val sortedMemories = sortMemoriesChronologically(filteredMemories)
        
        _uiState.value = TimelineUiState.Success(memories = sortedMemories)
    }
    
    private fun applyYearFilter(memories: List<Memory>, year: Int?): List<Memory> {
        if (year == null) return memories
        
        return memories.filter { memory ->
            memory.dateTaken?.let { timestamp ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = timestamp
                calendar.get(Calendar.YEAR) == year
            } ?: false
        }
    }
    
    private fun sortMemoriesChronologically(memories: List<Memory>): List<Memory> {
        return memories.sortedByDescending { it.dateTaken ?: it.createdAt }
    }
    
    fun getAvailableYears(): List<Int> {
        return allMemories
            .mapNotNull { memory ->
                memory.dateTaken?.let { timestamp ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = timestamp
                    calendar.get(Calendar.YEAR)
                }
            }
            .distinct()
            .sortedDescending()
    }
}

/**
 * Estados de la UI para la línea de tiempo
 */
sealed class TimelineUiState {
    object Loading : TimelineUiState()
    data class Success(val memories: List<Memory>) : TimelineUiState()
    data class Error(val message: String) : TimelineUiState()
}

