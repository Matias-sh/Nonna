package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ForgotPasswordStep {
    Email,
    Code,
    NewPassword
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _step = MutableStateFlow(ForgotPasswordStep.Email)
    val step: StateFlow<ForgotPasswordStep> = _step.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private var resetToken: String = ""

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _completed = MutableSharedFlow<Unit>()
    val completed: SharedFlow<Unit> = _completed.asSharedFlow()

    private val _codeSent = MutableSharedFlow<Unit>()
    val codeSent: SharedFlow<Unit> = _codeSent.asSharedFlow()

    fun setEmail(value: String) {
        _email.value = value
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun requestCode() {
        val em = _email.value.trim()
        if (em.isBlank()) {
            _errorMessage.value = "Ingresá tu email"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val r = authRepository.requestPasswordResetCode(em)) {
                is ApiResult.Success -> {
                    _step.value = ForgotPasswordStep.Code
                    _codeSent.emit(Unit)
                }
                is ApiResult.Error -> _errorMessage.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun verifyCode(code: String) {
        val c = code.trim()
        if (c.length != 6) {
            _errorMessage.value = "El código debe tener 6 dígitos"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val r = authRepository.verifyPasswordResetCode(c)) {
                is ApiResult.Success -> {
                    resetToken = r.data
                    _step.value = ForgotPasswordStep.NewPassword
                }
                is ApiResult.Error -> _errorMessage.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun confirmNewPassword(password: String, confirm: String) {
        if (password.length < 8) {
            _errorMessage.value = "La contraseña debe tener al menos 8 caracteres"
            return
        }
        if (password != confirm) {
            _errorMessage.value = "Las contraseñas no coinciden"
            return
        }
        if (resetToken.isBlank()) {
            _errorMessage.value = "Volvé a verificar el código"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val r = authRepository.confirmPasswordReset(resetToken, password, confirm)) {
                is ApiResult.Success -> {
                    resetToken = ""
                    _completed.emit(Unit)
                }
                is ApiResult.Error -> _errorMessage.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }
}
