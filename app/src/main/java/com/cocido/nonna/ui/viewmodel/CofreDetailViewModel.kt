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
import com.cocido.nonna.ui.permissions.canManageCofre
import com.cocido.nonna.ui.permissions.resolveOwnership
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

    private val _abandonSuccess = MutableSharedFlow<Unit>()
    val abandonSuccess: SharedFlow<Unit> = _abandonSuccess.asSharedFlow()

    private val _inviteeRemovedSuccess = MutableSharedFlow<Unit>()
    val inviteeRemovedSuccess: SharedFlow<Unit> = _inviteeRemovedSuccess.asSharedFlow()

    private val _leaveInProgress = MutableStateFlow(false)
    val leaveInProgress: StateFlow<Boolean> = _leaveInProgress.asStateFlow()

    private val _removeInviteeInProgress = MutableStateFlow(false)
    val removeInviteeInProgress: StateFlow<Boolean> = _removeInviteeInProgress.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            var me: UserDto? = null
            when (val result = authRepository.getMe()) {
                is ApiResult.Success -> {
                    me = result.data
                    _currentUser.value = result.data
                }
                is ApiResult.Error -> { }
                else -> { }
            }
            when (val result = cofreRepository.getCofre(cofreId)) {
                is ApiResult.Success -> _cofre.value = resolveOwnership(result.data, me)
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
            if (!canManageCofre(_cofre.value)) {
                _errorMessage.value = "Solo el dueño puede eliminar este cofre."
                return@launch
            }
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
            if (!canManageCofre(_cofre.value)) {
                _errorMessage.value = "Solo el dueño puede invitar miembros."
                return@launch
            }
            _errorMessage.value = null
            when (val result = cofreRepository.invitar(cofreId, emails)) {
                is ApiResult.Success -> {
                    when (val cofreResult = cofreRepository.getCofre(cofreId)) {
                        is ApiResult.Success -> _cofre.value = resolveOwnership(cofreResult.data, _currentUser.value)
                        else -> Unit
                    }
                    _inviteSuccess.emit(Unit)
                }
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
        }
    }

    fun abandonarCofreCompartido() {
        viewModelScope.launch {
            if (_cofre.value?.isOwner == true) {
                _errorMessage.value = "El dueño no puede abandonar su propio cofre."
                return@launch
            }
            _leaveInProgress.value = true
            _errorMessage.value = null
            when (val result = cofreRepository.abandonarCofreCompartido(cofreId)) {
                is ApiResult.Success -> _abandonSuccess.emit(Unit)
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
            _leaveInProgress.value = false
        }
    }

    fun eliminarInvitadoAceptado(invitadoUsuarioId: String) {
        viewModelScope.launch {
            if (!canManageCofre(_cofre.value)) {
                _errorMessage.value = "Solo el dueño puede quitar invitados."
                return@launch
            }
            _removeInviteeInProgress.value = true
            _errorMessage.value = null
            when (val result = cofreRepository.eliminarInvitadoAceptado(cofreId, invitadoUsuarioId)) {
                is ApiResult.Success -> {
                    when (val cofreResult = cofreRepository.getCofre(cofreId)) {
                        is ApiResult.Success -> _cofre.value = resolveOwnership(cofreResult.data, _currentUser.value)
                        else -> Unit
                    }
                    _inviteeRemovedSuccess.emit(Unit)
                }
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> Unit
            }
            _removeInviteeInProgress.value = false
        }
    }
}
