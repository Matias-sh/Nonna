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

    private val _authSuccess = MutableSharedFlow<UserDto>()
    val authSuccess = _authSuccess.asSharedFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.login(email, password)) {
                is ApiResult.Success -> _authSuccess.emit(result.data)
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
                is ApiResult.Success -> _authSuccess.emit(result.data)
                is ApiResult.Error -> _errorMessage.emit(result.message)
                else -> { }
            }
            _isLoading.value = false
        }
    }
}
