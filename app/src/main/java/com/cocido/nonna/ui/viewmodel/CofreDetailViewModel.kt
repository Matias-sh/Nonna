package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.AuthRepository
import com.cocido.nonna.data.repository.CofreRepository
import com.cocido.nonna.data.repository.RecuerdosRepository
import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.ui.components.CofreUiModel
import com.cocido.nonna.ui.components.MemoryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CofreDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val cofreRepository: CofreRepository,
    private val recuerdosRepository: RecuerdosRepository
) : ViewModel() {

    val cofreId: String = savedStateHandle.get<String>("cofreId") ?: ""

    private val _cofre = MutableStateFlow<CofreUiModel?>(null)
    val cofre: StateFlow<CofreUiModel?> = _cofre.asStateFlow()

    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    private val _memories = MutableStateFlow<List<MemoryUiModel>>(emptyList())
    val memories: StateFlow<List<MemoryUiModel>> = _memories.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _deleteSuccess = MutableSharedFlow<Unit>()
    val deleteSuccess: SharedFlow<Unit> = _deleteSuccess.asSharedFlow()

    private val _inviteSuccess = MutableSharedFlow<Unit>()
    val inviteSuccess: SharedFlow<Unit> = _inviteSuccess.asSharedFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = authRepository.getMe()) {
                is ApiResult.Success -> _currentUser.value = result.data
                is ApiResult.Error -> { }
                else -> { }
            }
            when (val result = cofreRepository.getCofre(cofreId)) {
                is ApiResult.Success -> _cofre.value = result.data
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
            recuerdosRepository.recuerdosByCofre(cofreId).collect { result ->
                when (result) {
                    is ApiResult.Success -> _memories.value = result.data
                    is ApiResult.Error -> if (_errorMessage.value == null) _errorMessage.value = result.message
                    else -> { }
                }
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun deleteCofre() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = cofreRepository.deleteCofre(cofreId)) {
                is ApiResult.Success -> _deleteSuccess.emit(Unit)
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
            _isLoading.value = false
        }
    }

    fun invitar(emails: List<String>) {
        viewModelScope.launch {
            _errorMessage.value = null
            when (val result = cofreRepository.invitar(cofreId, emails)) {
                is ApiResult.Success -> _inviteSuccess.emit(Unit)
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
        }
    }
}
