package com.cocido.nonna.domain.error

/**
 * Errores específicos del repositorio siguiendo mejores prácticas de manejo de errores
 */
sealed class RepositoryError : Throwable() {
    
    data class NetworkError(
        override val message: String,
        override val cause: Throwable? = null
    ) : RepositoryError()
    
    data class DatabaseError(
        override val message: String,
        override val cause: Throwable? = null
    ) : RepositoryError()
    
    data class AuthenticationError(
        override val message: String = "Usuario no autenticado"
    ) : RepositoryError()
    
    data class NotFoundError(
        override val message: String = "Recurso no encontrado"
    ) : RepositoryError()
    
    data class ValidationError(
        override val message: String
    ) : RepositoryError()
    
    data class SyncError(
        override val message: String,
        override val cause: Throwable? = null
    ) : RepositoryError()
    
    data class UnknownError(
        override val message: String = "Error desconocido",
        override val cause: Throwable? = null
    ) : RepositoryError()
}
