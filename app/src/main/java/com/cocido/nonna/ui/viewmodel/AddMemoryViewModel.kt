package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.RecuerdosRepository
import com.cocido.nonna.ui.components.MemoryUiModel
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
class AddMemoryViewModel @Inject constructor(
    private val recuerdosRepository: RecuerdosRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _saved = MutableSharedFlow<MemoryUiModel>()
    val saved: SharedFlow<MemoryUiModel> = _saved.asSharedFlow()

    fun save(
        cofreRecuerdosId: String,
        titulo: String,
        file: java.io.File,
        descripcion: String? = null,
        fecha: String? = null,
        emocionId: String? = null,
        emocionPersonalizada: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = recuerdosRepository.createRecuerdo(
                cofreRecuerdosId = cofreRecuerdosId,
                titulo = titulo,
                file = file,
                descripcion = descripcion,
                fecha = fecha,
                emocionId = emocionId,
                emocionPersonalizada = emocionPersonalizada
            )) {
                is ApiResult.Success -> _saved.emit(result.data)
                is ApiResult.Error -> _errorMessage.emit(result.message)
                else -> { }
            }
            _isLoading.value = false
        }
    }
}
