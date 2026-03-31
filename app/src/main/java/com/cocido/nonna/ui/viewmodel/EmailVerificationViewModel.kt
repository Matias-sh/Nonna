package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.AuthRepository
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
class EmailVerificationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    /** Email del usuario: se carga desde /auth/me al iniciar. */
    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Segundos restantes del cooldown para reenviar código.
     * 0 significa que ya se puede reenviar.
     */
    private val _resendCooldownSeconds = MutableStateFlow(0)
    val resendCooldownSeconds: StateFlow<Int> = _resendCooldownSeconds.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _resendSuccess = MutableSharedFlow<Unit>()
    val resendSuccess: SharedFlow<Unit> = _resendSuccess.asSharedFlow()

    private var cooldownJob: Job? = null

    init {
        loadUserEmail()
    }

    private fun loadUserEmail() {
        viewModelScope.launch {
            when (val result = authRepository.getMe()) {
                is ApiResult.Success -> _userEmail.value = result.data.email
                else -> { /* No crítico: la pantalla funciona sin mostrar el email */ }
            }
        }
    }

    /**
     * Verifica el email con el código de 6 dígitos ingresado.
     * En caso de éxito, AuthRepository actualiza DataStore → MainViewModel detecta → navega a Home.
     */
    fun verifyEmail(code: String) {
        if (code.length != 6) {
            viewModelScope.launch { _errorMessage.emit("Ingresá los 6 dígitos del código.") }
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.verifyEmail(code)) {
                is ApiResult.Success -> {
                    // DataStore actualiza emailVerificado=true → MainViewModel emite LoggedIn
                    // → key(authState) en MainActivity recrea NavHost → Home
                    // No hay que navegar manualmente desde acá.
                }
                is ApiResult.Error -> _errorMessage.emit(result.message)
                else -> { }
            }
            _isLoading.value = false
        }
    }

    /**
     * Reenvía el código de verificación al email del usuario.
     * Activa un cooldown de 60 segundos para evitar spam.
     */
    fun resendCode() {
        if (_resendCooldownSeconds.value > 0) return
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.sendVerificationEmail()) {
                is ApiResult.Success -> {
                    _resendSuccess.emit(Unit)
                    startResendCooldown(60)
                }
                is ApiResult.Error -> _errorMessage.emit(result.message)
                else -> { }
            }
            _isLoading.value = false
        }
    }

    private fun startResendCooldown(seconds: Int) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                _resendCooldownSeconds.value = remaining
                delay(1000)
                remaining--
            }
            _resendCooldownSeconds.value = 0
        }
    }

    override fun onCleared() {
        super.onCleared()
        cooldownJob?.cancel()
    }
}
