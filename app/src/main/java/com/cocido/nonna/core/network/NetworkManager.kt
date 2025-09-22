package com.cocido.nonna.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.cocido.nonna.core.logging.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager de red siguiendo mejores prácticas de DevSecOps
 * - Monitorea el estado de la conectividad
 * - Proporciona información sobre el tipo de conexión
 * - Maneja cambios de red de forma reactiva
 */
@Singleton
class NetworkManager @Inject constructor(
    private val context: Context
) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Flujo que emite el estado actual de la conectividad
     */
    val isConnected: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Logger.d("Network available: ${network}")
                trySend(true)
            }
            
            override fun onLost(network: Network) {
                Logger.d("Network lost: ${network}")
                trySend(false)
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                Logger.d("Network capabilities changed: hasInternet=$hasInternet")
                trySend(hasInternet)
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
            
        connectivityManager.registerNetworkCallback(request, callback)
        
        // Emitir estado inicial
        trySend(isCurrentlyConnected())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
    
    /**
     * Verifica si hay conectividad actual
     */
    fun isCurrentlyConnected(): Boolean {
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            Logger.e("Error checking network connectivity", throwable = e)
            false
        }
    }
    
    /**
     * Obtiene el tipo de conexión actual
     */
    fun getConnectionType(): ConnectionType {
        return try {
            val network = connectivityManager.activeNetwork ?: return ConnectionType.NONE
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectionType.NONE
            
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                else -> ConnectionType.UNKNOWN
            }
        } catch (e: Exception) {
            Logger.e("Error getting connection type", throwable = e)
            ConnectionType.NONE
        }
    }
    
    /**
     * Verifica si la conexión es de alta calidad (WiFi o Ethernet)
     */
    fun isHighQualityConnection(): Boolean {
        val connectionType = getConnectionType()
        return connectionType == ConnectionType.WIFI || connectionType == ConnectionType.ETHERNET
    }
}

/**
 * Tipos de conexión de red
 */
enum class ConnectionType {
    WIFI,
    CELLULAR,
    ETHERNET,
    UNKNOWN,
    NONE
}
