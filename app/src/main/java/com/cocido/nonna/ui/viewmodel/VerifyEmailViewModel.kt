package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.AuthRepository
import com.cocido.nonna.util.UserMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _cooldownSeconds = MutableStateFlow(0)
    val cooldownSeconds: StateFlow<Int> = _cooldownSeconds.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _infoMessage = MutableSharedFlow<String>()
    val infoMessage: SharedFlow<String> = _infoMessage.asSharedFlow()

    private val _verified = MutableSharedFlow<UserDto>()
    val verified: SharedFlow<UserDto> = _verified.asSharedFlow()

    /** Si el backend indica código vencido, mostramos un CTA destacado de reenvío en la UI. */
    private val _expiredCodeHighlight = MutableStateFlow(false)
    val expiredCodeHighlight: StateFlow<Boolean> = _expiredCodeHighlight.asStateFlow()

    private var cooldownJob: Job? = null

    fun verifyCode(code: String) {
        val trimmed = code.trim()
        if (trimmed.length != 6) {
            viewModelScope.launch {
                _expiredCodeHighlight.value = false
                _errorMessage.emit(UserMessages.INVALID_VERIFICATION_CODE)
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.verifyEmail(trimmed)) {
                is ApiResult.Success -> {
                    _expiredCodeHighlight.value = false
                    _verified.emit(result.data)
                }
                is ApiResult.Error -> {
                    _expiredCodeHighlight.value = result.message == UserMessages.EXPIRED_VERIFICATION_CODE
                    _errorMessage.emit(result.message)
                }
                ApiResult.Loading -> Unit
            }
            _isLoading.value = false
        }
    }

    fun resendCode() {
        if (_cooldownSeconds.value > 0) return
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.sendVerificationEmail()) {
                is ApiResult.Success -> {
                    _expiredCodeHighlight.value = false
                    _infoMessage.emit(result.data.ifBlank { UserMessages.VERIFICATION_CODE_SENT })
                    startCooldown()
                }
                is ApiResult.Error -> _errorMessage.emit(result.message)
                ApiResult.Loading -> Unit
            }
            _isLoading.value = false
        }
    }

    private fun startCooldown(durationSeconds: Int = 30) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            _cooldownSeconds.value = durationSeconds
            while (_cooldownSeconds.value > 0) {
                delay(1_000)
                _cooldownSeconds.value -= 1
            }
        }
    }
}
