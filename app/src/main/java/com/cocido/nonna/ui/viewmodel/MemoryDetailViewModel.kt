package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.RecuerdosRepository
import com.cocido.nonna.ui.components.MemoryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recuerdosRepository: RecuerdosRepository
) : ViewModel() {

    private val memoryId: String = savedStateHandle.get<String>("memoryId") ?: ""

    private val _memory = MutableStateFlow<MemoryUiModel?>(null)
    val memory: StateFlow<MemoryUiModel?> = _memory.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = recuerdosRepository.getRecuerdo(memoryId)) {
                is ApiResult.Success -> _memory.value = result.data
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
            _isLoading.value = false
        }
    }

    fun delete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val result = recuerdosRepository.deleteRecuerdo(memoryId)) {
                is ApiResult.Success -> onSuccess()
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
        }
    }
}
