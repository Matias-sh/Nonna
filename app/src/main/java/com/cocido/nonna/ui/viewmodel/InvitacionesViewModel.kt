package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.remote.dto.InvitacionUiModel
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.CofreRepository
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
class InvitacionesViewModel @Inject constructor(
    private val cofreRepository: CofreRepository
) : ViewModel() {

    private val _invitaciones = MutableStateFlow<List<InvitacionUiModel>>(emptyList())
    val invitaciones: StateFlow<List<InvitacionUiModel>> = _invitaciones.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** Emitido cuando una invitación fue aceptada con éxito. */
    private val _aceptarSuccess = MutableSharedFlow<String>() // nombre del cofre
    val aceptarSuccess: SharedFlow<String> = _aceptarSuccess.asSharedFlow()

    /** Emitido cuando una invitación fue rechazada con éxito. */
    private val _rechazarSuccess = MutableSharedFlow<Unit>()
    val rechazarSuccess: SharedFlow<Unit> = _rechazarSuccess.asSharedFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = cofreRepository.invitacionesPendientes()) {
                is ApiResult.Success -> _invitaciones.value = result.data
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
            _isLoading.value = false
        }
    }

    fun aceptar(invitacionId: String, nombreCofre: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = cofreRepository.aceptarInvitacion(invitacionId)) {
                is ApiResult.Success -> {
                    // Remover de la lista local inmediatamente para feedback rápido
                    _invitaciones.value = _invitaciones.value.filter { it.id != invitacionId }
                    _aceptarSuccess.emit(nombreCofre)
                }
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
            _isLoading.value = false
        }
    }

    fun rechazar(invitacionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = cofreRepository.rechazarInvitacion(invitacionId)) {
                is ApiResult.Success -> {
                    _invitaciones.value = _invitaciones.value.filter { it.id != invitacionId }
                    _rechazarSuccess.emit(Unit)
                }
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
