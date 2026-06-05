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
import com.cocido.nonna.data.repository.DataRefreshCoordinator
import com.cocido.nonna.data.repository.NetworkErrorParser
import com.cocido.nonna.data.repository.SuscripcionRepository
import android.os.SystemClock
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    private val refreshCoordinator: DataRefreshCoordinator,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private var lastPassiveRefreshAt: Long = 0L

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

    init {
        viewModelScope.launch {
            refreshCoordinator.events.collect { event ->
                if (event is DataRefreshCoordinator.Event.Profile) {
                    load(showLoading = false)
                }
            }
        }
    }

    fun load(showLoading: Boolean? = null) {
        viewModelScope.launch {
            val shouldShowLoading = showLoading ?: (_user.value == null)
            if (shouldShowLoading) _isLoading.value = true
            _errorMessage.value = null
            val basicUser = when (val result = authRepository.getMe()) {
                is ApiResult.Success -> result.data
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

            coroutineScope {
                val detailedDeferred = async {
                    try {
                        val response = usuarioApi.getById(basicUser.id)
                        if (response.isSuccessful) {
                            response.body()
                        } else {
                            _errorMessage.value = NetworkErrorParser.parseOrGeneric(
                                response.errorBody()?.string(),
                                response.code()
                            ) ?: _errorMessage.value
                            null
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
                val suscripcionDeferred = async { suscripcionRepository.getMiSuscripcion() }

                _user.value = detailedDeferred.await() ?: basicUser
                when (val sub = suscripcionDeferred.await()) {
                    is ApiResult.Success -> _suscripcion.value = sub.data
                    is ApiResult.Error -> _suscripcion.value = null
                    else -> { }
                }
            }

            _isLoading.value = false
        }
    }

    fun refreshOnResume(minIntervalMs: Long = 2500L) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastPassiveRefreshAt < minIntervalMs) return
        lastPassiveRefreshAt = now
        load(showLoading = false)
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
                    authRepository.invalidateMeCache()
                    _user.value = response.body()
                    refreshCoordinator.invalidateProfile()
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
