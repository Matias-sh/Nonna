package com.cocido.nonna.ui.viewmodel

import android.content.Context
import android.net.Uri
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
class CreateCofreViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cofreRepository: CofreRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _created = MutableSharedFlow<CofreUiModel>()
    val created: SharedFlow<CofreUiModel> = _created.asSharedFlow()

    fun create(
        name: String,
        relation: String,
        description: String?,
        coverImageUri: Uri?,
        inviteEmails: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val coverFile = coverImageUri?.let { uri -> uriToTempFile(uri) }
            when (val result = cofreRepository.createCofre(
                name = name,
                relation = relation,
                description = description,
                coverImageFile = coverFile,
                inviteEmails = inviteEmails
            )) {
                is ApiResult.Success -> _created.emit(result.data)
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
