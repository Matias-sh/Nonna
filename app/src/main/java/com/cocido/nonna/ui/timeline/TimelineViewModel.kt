package com.cocido.nonna.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.domain.model.Memory
import com.cocido.nonna.domain.usecase.GetTimelineMemoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el fragment de línea de tiempo
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getTimelineMemoriesUseCase: GetTimelineMemoriesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TimelineUiState>(TimelineUiState.Idle)
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()
    
    private var currentYearFilter: Int? = null
    
    fun loadTimeline() {
        viewModelScope.launch {
            _uiState.value = TimelineUiState.Loading
            
            getTimelineMemoriesUseCase(currentYearFilter)
                .onSuccess { memories ->
                    _uiState.value = TimelineUiState.Success(memories)
                }
                .onFailure { exception ->
                    _uiState.value = TimelineUiState.Error(
                        message = exception.message ?: "Error al cargar la línea de tiempo"
                    )
                }
        }
    }
    
    fun filterByYear(year: Int?) {
        currentYearFilter = year
        loadTimeline()
    }
    
    fun refreshTimeline() {
        loadTimeline()
    }
}

/**
 * Estados de la UI para la línea de tiempo
 */
sealed class TimelineUiState {
    object Idle : TimelineUiState()
    object Loading : TimelineUiState()
    data class Success(val memories: List<Memory>) : TimelineUiState()
    data class Error(val message: String) : TimelineUiState()
}