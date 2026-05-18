package com.cocido.nonna.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.remote.UsuarioApi
import com.cocido.nonna.data.remote.dto.UserDto
import com.cocido.nonna.data.remote.dto.SuscripcionActualDto
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.AuthRepository
import com.cocido.nonna.data.repository.NetworkErrorParser
import com.cocido.nonna.data.repository.SuscripcionRepository
import com.cocido.nonna.util.ImageCompressor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val suscripcionRepository: SuscripcionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user.asStateFlow()

    private val _suscripcion = MutableStateFlow<SuscripcionActualDto?>(null)
    val suscripcion: StateFlow<SuscripcionActualDto?> = _suscripcion.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _updateSuccess = MutableSharedFlow<Unit>()
    val updateSuccess: SharedFlow<Unit> = _updateSuccess.asSharedFlow()

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            // 1) Datos básicos desde /auth/me (siempre se usa para saber si hay sesión)
            val basicUser = when (val result = authRepository.getMe()) {
                is ApiResult.Success -> {
                    result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    _isLoading.value = false
                    return@launch
                }
                else -> {
                    _isLoading.value = false
                    return@launch
                }
            }

            // 2) Intentar enriquecer con /usuario/{id}, que suele traer campos como fotoPerfil / urlFotoPerfil
            val detailedUser = try {
                val response = usuarioApi.getById(basicUser.id)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    // Si falla, nos quedamos con basicUser pero guardamos el mensaje para depurar si hace falta
                    _errorMessage.value = NetworkErrorParser.parseOrGeneric(
                        response.errorBody()?.string(),
                        response.code()
                    )
                        ?: _errorMessage.value
                    null
                }
            } catch (e: Exception) {
                // No rompemos la pantalla de perfil por un fallo puntual de este endpoint
                null
            }

            _user.value = detailedUser ?: basicUser

            when (val sub = suscripcionRepository.getMiSuscripcion()) {
                is ApiResult.Success -> _suscripcion.value = sub.data
                is ApiResult.Error -> {
                    // No bloqueamos el perfil si la suscripción falla (p. ej. seed de planes)
                    _suscripcion.value = null
                }
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
                        val file = ImageCompressor.compressForUpload(
                            context = context,
                            uri = uri,
                            maxBytes = 1024 * 1024
                        ) ?: run {
                            val input = context.contentResolver.openInputStream(uri) ?: return@let null
                            val ext = "jpg"
                            val fallbackFile = File.createTempFile("avatar", ".$ext", context.cacheDir)
                            input.use { i ->
                                fallbackFile.outputStream().use { o -> i.copyTo(o) }
                            }
                            fallbackFile
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
                    _updateSuccess.emit(Unit)
                } else {
                    _errorMessage.value = NetworkErrorParser.parseOrGeneric(
                        response.errorBody()?.string(),
                        response.code()
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = "No se pudo actualizar el perfil. Intentá nuevamente."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
