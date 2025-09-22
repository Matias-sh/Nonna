package com.cocido.nonna.domain.result

import com.cocido.nonna.domain.error.RepositoryError

/**
 * Result wrapper robusto para manejo de errores siguiendo mejores prácticas
 */
sealed class AppResult<out T> {
    
    data class Success<T>(val data: T) : AppResult<T>()
    
    data class Error(
        val error: RepositoryError
    ) : AppResult<Nothing>()
    
    object Loading : AppResult<Nothing>()
    
    /**
     * Mapea el resultado si es exitoso
     */
    inline fun <R> map(transform: (T) -> R): AppResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
    }
    
    /**
     * Mapea el error si existe
     */
    inline fun mapError(transform: (RepositoryError) -> RepositoryError): AppResult<T> {
        return when (this) {
            is Success -> this
            is Error -> Error(transform(error))
            is Loading -> this
        }
    }
    
    /**
     * Ejecuta una acción si el resultado es exitoso
     */
    inline fun onSuccess(action: (T) -> Unit): AppResult<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }
    
    /**
     * Ejecuta una acción si el resultado es un error
     */
    inline fun onError(action: (RepositoryError) -> Unit): AppResult<T> {
        if (this is Error) {
            action(error)
        }
        return this
    }
    
    /**
     * Obtiene los datos o un valor por defecto
     */
    fun getOrElse(defaultValue: @UnsafeVariance T): T {
        return when (this) {
            is Success -> data
            is Error -> defaultValue
            is Loading -> defaultValue
        }
    }
    
    /**
     * Obtiene los datos o null
     */
    fun getOrNull(): T? {
        return when (this) {
            is Success -> data
            is Error -> null
            is Loading -> null
        }
    }
    
    /**
     * Verifica si el resultado es exitoso
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Verifica si el resultado es un error
     */
    fun isError(): Boolean = this is Error
    
    /**
     * Verifica si está cargando
     */
    fun isLoading(): Boolean = this is Loading
}
