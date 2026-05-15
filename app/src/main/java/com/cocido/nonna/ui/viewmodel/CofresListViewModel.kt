package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.CofreRepository
import com.cocido.nonna.ui.components.CofreUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.os.SystemClock
import javax.inject.Inject

@HiltViewModel
class CofresListViewModel @Inject constructor(
    private val cofreRepository: CofreRepository
) : ViewModel() {
    private val cacheTtlMs = 30_000L
    private var lastLoadAtMs: Long = 0L
    private var loadInProgress = false

    private val _cofres = MutableStateFlow<List<CofreUiModel>>(emptyList())
    val cofres: StateFlow<List<CofreUiModel>> = _cofres.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun load(forceRefresh: Boolean = false) {
        val now = SystemClock.elapsedRealtime()
        if (!forceRefresh && !loadInProgress && (now - lastLoadAtMs) <= cacheTtlMs && _cofres.value.isNotEmpty()) {
            return
        }
        if (loadInProgress) return
        viewModelScope.launch {
            loadInProgress = true
            try {
                _isLoading.value = true
                _errorMessage.value = null
                cofreRepository.misCofres().collect { result ->
                    when (result) {
                        is ApiResult.Success -> _cofres.value = result.data
                        is ApiResult.Error -> _errorMessage.value = result.message
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

    fun clearError() {
        _errorMessage.value = null
    }
}
