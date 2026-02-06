package com.cocido.nonna.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.remote.UsuarioApi
import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.AuthRepository
import com.cocido.nonna.data.repository.NetworkErrorParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioApi: UsuarioApi,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = authRepository.getMe()) {
                is ApiResult.Success -> _user.value = result.data
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> { }
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        username: String?,
        avatarUri: Uri?
    ) {
        val current = _user.value ?: return
        val nombreTrimmed = firstName.trim()
        val apellidoTrimmed = lastName.trim()
        val usernameTrimmed = username?.trim().orEmpty()
        if (nombreTrimmed.isEmpty() && apellidoTrimmed.isEmpty() && usernameTrimmed.isEmpty() && avatarUri == null) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val nombreBody: RequestBody? = nombreTrimmed.takeIf { it.isNotEmpty() }
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())
                val apellidoBody: RequestBody? = apellidoTrimmed.takeIf { it.isNotEmpty() }
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())
                val nombreUsuarioBody: RequestBody? = usernameTrimmed.takeIf { it.isNotEmpty() }
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())

                val fotoPart: MultipartBody.Part? = avatarUri?.let { uri ->
                    try {
                        val input = context.contentResolver.openInputStream(uri) ?: return@let null
                        val ext = "jpg"
                        val file = File.createTempFile("avatar", ".$ext", context.cacheDir)
                        input.use { i ->
                            file.outputStream().use { o -> i.copyTo(o) }
                        }
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("fotoPerfil", file.name, requestFile)
                    } catch (_: Exception) {
                        null
                    }
                }

                val response = usuarioApi.update(
                    id = current.id,
                    nombre = nombreBody,
                    apellido = apellidoBody,
                    nombreUsuario = nombreUsuarioBody,
                    contrasena = null,
                    email = null,
                    activo = null,
                    fotoPerfil = fotoPart,
                    urlFotoPerfil = null
                )
                if (response.isSuccessful) {
                    _user.value = response.body()
                } else {
                    _errorMessage.value = NetworkErrorParser.parse(response.errorBody()?.string())
                        ?: "No se pudo actualizar el perfil"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al actualizar el perfil"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
