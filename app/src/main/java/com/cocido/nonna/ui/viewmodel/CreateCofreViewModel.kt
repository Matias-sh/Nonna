package com.cocido.nonna.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.CofreRepository
import com.cocido.nonna.data.repository.DataRefreshCoordinator
import com.cocido.nonna.data.repository.SuscripcionRepository
import com.cocido.nonna.ui.components.CofreUiModel
import com.cocido.nonna.util.PhotoUploadPreparer
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
    private val cofreRepository: CofreRepository,
    private val suscripcionRepository: SuscripcionRepository,
    private val refreshCoordinator: DataRefreshCoordinator
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _created = MutableSharedFlow<CofreUiModel>()
    val created: SharedFlow<CofreUiModel> = _created.asSharedFlow()

    init {
        viewModelScope.launch {
            suscripcionRepository.getMiSuscripcion()
        }
    }

    fun create(
        name: String,
        relation: String,
        description: String?,
        coverImageUri: Uri?,
        inviteEmails: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            if (!canCreateAnotherCofre()) {
                _isLoading.value = false
                return@launch
            }
            val coverFile = coverImageUri?.let { uri -> uriToTempFile(uri) }
            when (val result = cofreRepository.createCofre(
                name = name,
                relation = relation,
                description = description,
                coverImageFile = coverFile,
                inviteEmails = inviteEmails
            )) {
                is ApiResult.Success -> {
                    refreshCoordinator.invalidateCofresList()
                    _created.emit(result.data)
                }
                is ApiResult.Error -> _errorMessage.emit(result.message)
                else -> { }
            }
            coverFile?.delete()
            _isLoading.value = false
        }
    }

    private suspend fun canCreateAnotherCofre(): Boolean {
        return when (val subResult = suscripcionRepository.getMiSuscripcion()) {
            is ApiResult.Success -> {
                val limites = subResult.data.limites
                val uso = subResult.data.uso
                val maxCofres = (limites?.maxCofres ?: subResult.data.plan?.maxCofres)?.coerceAtLeast(0)
                val cofresCreados = (uso?.cofresCreados ?: 0).coerceAtLeast(0)
                if (maxCofres != null && maxCofres > 0 && cofresCreados >= maxCofres) {
                    val planName = subResult.data.plan?.nombre?.trim().orEmpty().ifBlank { "actual" }
                    _errorMessage.emit(
                        "Alcanzaste el límite de $maxCofres cofre(s) para tu plan $planName. " +
                            "Para crear más, necesitás cambiar de plan."
                    )
                    false
                } else {
                    true
                }
            }
            else -> true
        }
    }

    private suspend fun uriToTempFile(uri: Uri): File? = withContext(Dispatchers.IO) {
        PhotoUploadPreparer.resolveFile(
            context = context,
            uri = uri,
            profile = PhotoUploadPreparer.Profile.Cover
        ) ?: run {
            val ext = context.contentResolver.getType(uri)?.substringAfter("/") ?: "jpg"
            val file = File.createTempFile("cofre_cover", ".$ext", context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file.takeIf { it.length() > 0L }
        }
    }
}
