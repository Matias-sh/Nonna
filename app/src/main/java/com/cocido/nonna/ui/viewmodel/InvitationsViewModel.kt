package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.CofreRepository
import com.cocido.nonna.ui.components.CofreInvitationUiModel
import com.cocido.nonna.util.UserMessages
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
class InvitationsViewModel @Inject constructor(
    private val cofreRepository: CofreRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _pendingInvitations = MutableStateFlow<List<CofreInvitationUiModel>>(emptyList())
    val pendingInvitations: StateFlow<List<CofreInvitationUiModel>> = _pendingInvitations.asStateFlow()

    private val _sentInvitations = MutableStateFlow<List<CofreInvitationUiModel>>(emptyList())
    val sentInvitations: StateFlow<List<CofreInvitationUiModel>> = _sentInvitations.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _successMessage = MutableSharedFlow<String>()
    val successMessage: SharedFlow<String> = _successMessage.asSharedFlow()

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = cofreRepository.getPendingInvitations()) {
                is ApiResult.Success -> _pendingInvitations.value = result.data
                is ApiResult.Error -> _errorMessage.emit(result.message)
                ApiResult.Loading -> Unit
            }
            when (val result = cofreRepository.getSentInvitations()) {
                is ApiResult.Success -> _sentInvitations.value = result.data
                is ApiResult.Error -> _errorMessage.emit(result.message)
                ApiResult.Loading -> Unit
            }

            _isLoading.value = false
        }
    }

    fun acceptInvitation(invitationId: String) {
        viewModelScope.launch {
            when (val result = cofreRepository.acceptInvitation(invitationId)) {
                is ApiResult.Success -> {
                    _successMessage.emit(UserMessages.INVITATION_ACCEPTED)
                    load()
                }
                is ApiResult.Error -> _errorMessage.emit(result.message)
                ApiResult.Loading -> Unit
            }
        }
    }

    fun rejectInvitation(invitationId: String) {
        viewModelScope.launch {
            when (val result = cofreRepository.rejectInvitation(invitationId)) {
                is ApiResult.Success -> {
                    _successMessage.emit(UserMessages.INVITATION_REJECTED)
                    load()
                }
                is ApiResult.Error -> _errorMessage.emit(result.message)
                ApiResult.Loading -> Unit
            }
        }
    }

    fun cancelInvitation(invitationId: String) {
        viewModelScope.launch {
            when (val result = cofreRepository.cancelInvitation(invitationId)) {
                is ApiResult.Success -> {
                    _successMessage.emit(UserMessages.INVITATION_CANCELLED)
                    load()
                }
                is ApiResult.Error -> _errorMessage.emit(result.message)
                ApiResult.Loading -> Unit
            }
        }
    }
}
