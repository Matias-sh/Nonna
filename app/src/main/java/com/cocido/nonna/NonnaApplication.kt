package com.cocido.nonna

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application principal de Nonna
 * Configuración global de Hilt para inyección de dependencias
 */
@HiltAndroidApp
class NonnaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Inicialización global de la aplicación
    }
}

