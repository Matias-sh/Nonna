package com.cocido.nonna.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.CofreRepository
import com.cocido.nonna.ui.components.CofreUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditCofreViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val cofreRepository: CofreRepository
) : ViewModel() {

    val cofreId: String = savedStateHandle.get<String>("cofreId") ?: ""

    private val _cofre = MutableStateFlow<CofreUiModel?>(null)
    val cofre: StateFlow<CofreUiModel?> = _cofre.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _updateSuccess = MutableSharedFlow<CofreUiModel>()
    val updateSuccess: SharedFlow<CofreUiModel> = _updateSuccess.asSharedFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = cofreRepository.getCofre(cofreId)) {
                is ApiResult.Success -> _cofre.value = result.data
                is ApiResult.Error -> _errorMessage.emit(result.message)
                else -> { }
            }
            _isLoading.value = false
        }
    }

    fun update(name: String, relation: String, description: String?, coverImageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            val coverFile = coverImageUri?.let { uri -> uriToTempFile(uri) }
            when (val result = cofreRepository.updateCofre(
                id = cofreId,
                name = name,
                relation = relation,
                description = description,
                coverImageFile = coverFile,
                existingCoverUrl = _cofre.value?.coverImageUrl
            )) {
                is ApiResult.Success -> {
                    _cofre.value = result.data
                    _updateSuccess.emit(result.data)
                }
                is ApiResult.Error -> _errorMessage.emit(result.message)
                else -> { }
            }
            coverFile?.delete()
            _isLoading.value = false
        }
    }

    private suspend fun uriToTempFile(uri: Uri): File = withContext(Dispatchers.IO) {
        val ext = context.contentResolver.getType(uri)?.substringAfter("/") ?: "jpg"
        val file = File.createTempFile("cofre_cover", ".$ext", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file
    }
}
