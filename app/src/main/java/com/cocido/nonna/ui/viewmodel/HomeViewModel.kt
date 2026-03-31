package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.AuthRepository
import com.cocido.nonna.data.repository.CofreRepository
import com.cocido.nonna.ui.components.CofreUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    /** Cantidad de invitaciones pendientes. > 0 muestra banner en Home. */
    private val _invitacionesPendientesCount = MutableStateFlow(0)
    val invitacionesPendientesCount: StateFlow<Int> = _invitacionesPendientesCount.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val userResult = authRepository.getMe()) {
                is ApiResult.Success -> _user.value = userResult.data
                is ApiResult.Error -> _errorMessage.value = userResult.message
                else -> { }
            }
            cofreRepository.misCofres().collect { result ->
                when (result) {
                    is ApiResult.Success -> _cofres.value = result.data
                    is ApiResult.Error -> if (_errorMessage.value == null) _errorMessage.value = result.message
                    else -> { }
                }
            }
            // Cargar invitaciones pendientes en segundo plano (no bloquea la UI)
            loadInvitacionesPendientes()
            _isLoading.value = false
        }
    }

    private suspend fun loadInvitacionesPendientes() {
        when (val result = cofreRepository.invitacionesPendientes()) {
            is ApiResult.Success -> _invitacionesPendientesCount.value = result.data.size
            else -> { /* Silencioso: el banner no aparece si falla */ }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
