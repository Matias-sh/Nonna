package com.cocido.nonna.ui.viewmodel

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.AuthRepository
import com.cocido.nonna.data.repository.DataRefreshCoordinator
import com.cocido.nonna.data.repository.RecuerdosRepository
import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.ui.components.MemoryUiModel
import com.cocido.nonna.ui.permissions.canModifyMemory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val recuerdosRepository: RecuerdosRepository,
    private val refreshCoordinator: DataRefreshCoordinator
) : ViewModel() {

    private val memoryId: String = savedStateHandle.get<String>("memoryId") ?: ""
    private val cofreId: String = savedStateHandle.get<String>("cofreId").orEmpty()

    private val _memory = MutableStateFlow<MemoryUiModel?>(null)
    val memory: StateFlow<MemoryUiModel?> = _memory.asStateFlow()

    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    private val _canModify = MutableStateFlow(false)
    val canModify: StateFlow<Boolean> = _canModify.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var lastPassiveRefreshAt: Long = 0L

    init {
        loadCurrentUser()
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
                is ApiResult.Success -> {
                    _memory.value = result.data
                    updateCanModify(result.data)
                }
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
            _isLoading.value = false
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = authRepository.getMe()) {
                is ApiResult.Success -> {
                    _currentUser.value = result.data
                    _memory.value?.let { updateCanModify(it) }
                }
                else -> Unit
            }
        }
    }

    private fun updateCanModify(memory: MemoryUiModel) {
        _canModify.value = canModifyMemory(memory, _currentUser.value)
    }

    fun refreshOnResume(minIntervalMs: Long = 1500L) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastPassiveRefreshAt < minIntervalMs) return
        lastPassiveRefreshAt = now
        load(showLoading = false)
    }

    fun delete(onSuccess: () -> Unit) {
        if (_isDeleting.value) return
        viewModelScope.launch {
            _isDeleting.value = true
            when (val result = recuerdosRepository.deleteRecuerdo(memoryId)) {
                is ApiResult.Success -> {
                    _memory.value = null
                    if (cofreId.isNotBlank()) {
                        refreshCoordinator.invalidateCofre(cofreId)
                    } else {
                        refreshCoordinator.invalidateCofresList()
                    }
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _isDeleting.value = false
                }
                else -> _isDeleting.value = false
            }
        }
    }
}
