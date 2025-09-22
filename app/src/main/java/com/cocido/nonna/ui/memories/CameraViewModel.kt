package com.cocido.nonna.ui.memories

import android.content.Context
import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.camera.CameraXManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el fragment de cámara
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraXManager: CameraXManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
    
    private var isFlashOn = false
    
    init {
        cameraXManager.initialize()
    }
    
    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Loading
            
            cameraXManager.startCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                onImageCaptured = { uri ->
                    _uiState.value = CameraUiState.PhotoCaptured(uri)
                },
                onError = { error ->
                    _uiState.value = CameraUiState.Error(error)
                }
            )
        }
    }
    
    fun takePhoto(context: Context) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Loading
            
            cameraXManager.takePhoto(
                context = context,
                onImageCaptured = { uri ->
                    _uiState.value = CameraUiState.PhotoCaptured(uri)
                },
                onError = { error ->
                    _uiState.value = CameraUiState.Error(error)
                }
            )
        }
    }
    
    fun switchCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        viewModelScope.launch {
            cameraXManager.switchCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                onError = { error ->
                    _uiState.value = CameraUiState.Error(error)
                }
            )
        }
    }
    
    fun toggleFlash() {
        isFlashOn = !isFlashOn
        _uiState.value = CameraUiState.FlashToggled(isFlashOn)
        // TODO: Implementar control de flash en CameraXManager
    }
    
    fun stopCamera() {
        cameraXManager.stopCamera()
    }
    
    override fun onCleared() {
        super.onCleared()
        cameraXManager.cleanup()
    }
}

/**
 * Estados de la UI para la cámara
 */
sealed class CameraUiState {
    object Idle : CameraUiState()
    object Loading : CameraUiState()
    data class PhotoCaptured(val photoUri: Uri) : CameraUiState()
    data class Error(val message: String) : CameraUiState()
    data class FlashToggled(val isFlashOn: Boolean) : CameraUiState()
}


