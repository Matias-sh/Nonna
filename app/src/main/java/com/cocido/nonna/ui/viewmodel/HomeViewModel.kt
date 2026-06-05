package com.cocido.nonna.ui.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.AuthRepository
import com.cocido.nonna.data.repository.CofreRepository
import com.cocido.nonna.data.repository.DataRefreshCoordinator
import com.cocido.nonna.ui.components.CofreInvitationUiModel
import com.cocido.nonna.ui.components.CofreUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cofreRepository: CofreRepository,
    private val refreshCoordinator: DataRefreshCoordinator
) : ViewModel() {
    private val cacheTtlMs = 30_000L
    private var lastLoadAtMs: Long = 0L
    private var lastPassiveRefreshAt: Long = 0L
    private var loadInProgress = false

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

    init {
        viewModelScope.launch {
            refreshCoordinator.events.collect { event ->
                if (event is DataRefreshCoordinator.Event.CofresList) {
                    load(forceRefresh = true, showLoading = false)
                }
            }
        }
    }

    fun load(forceRefresh: Boolean = false, showLoading: Boolean? = null) {
        val now = SystemClock.elapsedRealtime()
        if (!forceRefresh && !loadInProgress && (now - lastLoadAtMs) <= cacheTtlMs && _cofres.value.isNotEmpty()) {
            return
        }
        if (loadInProgress && !forceRefresh) return
        viewModelScope.launch {
            loadInProgress = true
            val shouldShowLoading = showLoading ?: _cofres.value.isEmpty()
            try {
                if (shouldShowLoading) _isLoading.value = true
                _errorMessage.value = null
                coroutineScope {
                    val userDeferred = async { authRepository.getMe() }
                    val invitationsDeferred = async { cofreRepository.getPendingInvitations() }
                    val cofresDeferred = async {
                        cofreRepository.misCofres().first { it !is ApiResult.Loading }
                    }

                    when (val userResult = userDeferred.await()) {
                        is ApiResult.Success -> _user.value = userResult.data
                        is ApiResult.Error -> _errorMessage.value = userResult.message
                        else -> { }
                    }

                    when (val invitationsResult = invitationsDeferred.await()) {
                        is ApiResult.Success -> {
                            _featuredInvitation.value =
                                invitationsResult.data.firstOrNull { !it.accepted && !it.expired }
                        }
                        is ApiResult.Error -> {
                            if (_errorMessage.value == null) _errorMessage.value = invitationsResult.message
                        }
                        else -> Unit
                    }

                    when (val cofresResult = cofresDeferred.await()) {
                        is ApiResult.Success -> _cofres.value = cofresResult.data
                        is ApiResult.Error -> {
                            if (_errorMessage.value == null) _errorMessage.value = cofresResult.message
                        }
                        else -> { }
                    }
                }
                lastLoadAtMs = SystemClock.elapsedRealtime()
            } finally {
                _isLoading.value = false
                loadInProgress = false
            }
        }
    }

    fun refreshOnResume(minIntervalMs: Long = 1500L) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastPassiveRefreshAt < minIntervalMs) return
        lastPassiveRefreshAt = now
        load(forceRefresh = true, showLoading = false)
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
                    load(forceRefresh = true, showLoading = false)
                }
                is ApiResult.Error -> _invitationAcceptError.value = result.message
                else -> Unit
            }
            _invitationAcceptLoading.value = false
        }
    }
}
