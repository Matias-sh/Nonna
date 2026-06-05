package com.cocido.nonna.ui.viewmodel

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.DataRefreshCoordinator
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
    private val recuerdosRepository: RecuerdosRepository,
    private val refreshCoordinator: DataRefreshCoordinator
) : ViewModel() {

    private val memoryId: String = savedStateHandle.get<String>("memoryId") ?: ""

    private val _memory = MutableStateFlow<MemoryUiModel?>(null)
    val memory: StateFlow<MemoryUiModel?> = _memory.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var lastPassiveRefreshAt: Long = 0L

    init {
        load()
        viewModelScope.launch {
            refreshCoordinator.events.collect { event ->
                if (event is DataRefreshCoordinator.Event.MemoryDetail && event.memoryId == memoryId) {
                    load(showLoading = false)
                }
            }
        }
    }

    fun load(showLoading: Boolean? = null) {
        viewModelScope.launch {
            val shouldShowLoading = showLoading ?: (_memory.value == null)
            if (shouldShowLoading) _isLoading.value = true
            _errorMessage.value = null
            when (val result = recuerdosRepository.getRecuerdo(memoryId)) {
                is ApiResult.Success -> _memory.value = result.data
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
            _isLoading.value = false
        }
    }

    fun refreshOnResume(minIntervalMs: Long = 1500L) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastPassiveRefreshAt < minIntervalMs) return
        lastPassiveRefreshAt = now
        load(showLoading = false)
    }

    fun delete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val result = recuerdosRepository.deleteRecuerdo(memoryId)) {
                is ApiResult.Success -> {
                    refreshCoordinator.invalidateCofresList()
                    onSuccess()
                }
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
        }
    }
}
