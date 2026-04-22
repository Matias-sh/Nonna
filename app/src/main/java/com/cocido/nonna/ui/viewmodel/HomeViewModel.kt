package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.AuthRepository
import com.cocido.nonna.data.repository.CofreRepository
import com.cocido.nonna.ui.components.CofreInvitationUiModel
import com.cocido.nonna.ui.components.CofreUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cofreRepository: CofreRepository
) : ViewModel() {

    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user.asStateFlow()

    private val _cofres = MutableStateFlow<List<CofreUiModel>>(emptyList())
    val cofres: StateFlow<List<CofreUiModel>> = _cofres.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _featuredInvitation = MutableStateFlow<CofreInvitationUiModel?>(null)
    val featuredInvitation: StateFlow<CofreInvitationUiModel?> = _featuredInvitation.asStateFlow()

    private val _invitationAcceptLoading = MutableStateFlow(false)
    val invitationAcceptLoading: StateFlow<Boolean> = _invitationAcceptLoading.asStateFlow()

    private val _invitationAcceptError = MutableStateFlow<String?>(null)
    val invitationAcceptError: StateFlow<String?> = _invitationAcceptError.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val userResult = authRepository.getMe()) {
                is ApiResult.Success -> _user.value = userResult.data
                is ApiResult.Error -> _errorMessage.value = userResult.message
                else -> { }
            }
            refreshFeaturedInvitation()
            collectCofresList()
            _isLoading.value = false
        }
    }

    private suspend fun collectCofresList() {
        cofreRepository.misCofres().collect { result ->
            when (result) {
                is ApiResult.Success -> _cofres.value = result.data
                is ApiResult.Error -> if (_errorMessage.value == null) _errorMessage.value = result.message
                else -> { }
            }
        }
    }

    private suspend fun refreshFeaturedInvitation() {
        when (val invitationsResult = cofreRepository.getPendingInvitations()) {
            is ApiResult.Success -> {
                _featuredInvitation.value = invitationsResult.data.firstOrNull { !it.accepted && !it.expired }
            }
            is ApiResult.Error -> if (_errorMessage.value == null) _errorMessage.value = invitationsResult.message
            else -> Unit
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun dismissFeaturedInvitation() {
        _featuredInvitation.value = null
    }

    fun clearInvitationAcceptError() {
        _invitationAcceptError.value = null
    }

    fun acceptInvitationFromHome(invitationId: String) {
        viewModelScope.launch {
            _invitationAcceptLoading.value = true
            _invitationAcceptError.value = null
            when (val result = cofreRepository.acceptInvitation(invitationId)) {
                is ApiResult.Success -> {
                    _featuredInvitation.value = null
                    collectCofresList()
                    refreshFeaturedInvitation()
                }
                is ApiResult.Error -> _invitationAcceptError.value = result.message
                else -> Unit
            }
            _invitationAcceptLoading.value = false
        }
    }
}
