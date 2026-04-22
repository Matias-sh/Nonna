package com.cocido.nonna

import android.app.Application
import com.cocido.nonna.util.AppContextProvider
import dagger.hilt.android.HiltAndroidApp

/**
 * Application principal de Nonna
 * Configuración global de Hilt para inyección de dependencias
 */
@HiltAndroidApp
class NonnaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        AppContextProvider.init(this)
        // Inicialización global de la aplicación
    }
}

