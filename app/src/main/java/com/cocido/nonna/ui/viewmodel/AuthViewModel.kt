package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    /** Emitido cuando el login/signup fue exitoso Y el email está verificado (o no se pudo determinar). */
    private val _authSuccess = MutableSharedFlow<UserDto>()
    val authSuccess = _authSuccess.asSharedFlow()

    /**
     * Emitido cuando el login/signup fue exitoso PERO el email NO está verificado.
     * El MainViewModel detectará el cambio en DataStore y navegará a EmailVerificationScreen.
     * Este flow se puede usar en la pantalla para mostrar feedback inmediato al usuario.
     */
    private val _emailVerificationNeeded = MutableSharedFlow<Unit>()
    val emailVerificationNeeded = _emailVerificationNeeded.asSharedFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.login(email, password)) {
                is ApiResult.Success -> {
                    val user = result.data
                    if (user.emailVerificado == false) {
                        // Email no verificado: señalizar a la UI, el MainViewModel manejará la navegación
                        _emailVerificationNeeded.emit(Unit)
                    } else {
                        _authSuccess.emit(user)
                    }
                }
                is ApiResult.Error -> _errorMessage.emit(result.message)
                else -> { }
            }
            _isLoading.value = false
        }
    }

    fun signup(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        nombreUsuario: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.signup(email, password, nombre, apellido, nombreUsuario)) {
                is ApiResult.Success -> {
                    val user = result.data
                    // Después del signup, el email nunca está verificado (se guarda false en DataStore)
                    if (user.emailVerificado == false) {
                        _emailVerificationNeeded.emit(Unit)
                    } else {
                        _authSuccess.emit(user)
                    }
                }
                is ApiResult.Error -> _errorMessage.emit(result.message)
                else -> { }
            }
            _isLoading.value = false
        }
    }
}
