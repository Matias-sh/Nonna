package com.cocido.nonna.sync

import androidx.work.*
// import com.cocido.nonna.workers.SyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio para manejar la sincronización de datos
 * Configura y ejecuta trabajos de sincronización
 * TODO: Reimplementar cuando SyncWorker esté disponible
 */
@Singleton
class SyncService @Inject constructor(
    private val workManager: WorkManager
) {
    
    fun schedulePeriodicSync() {
        // TODO: Reimplementar cuando SyncWorker esté disponible
        /*
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES, // Frecuencia mínima
            5, TimeUnit.MINUTES   // Flexibilidad
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
        */
    }
    
    fun scheduleImmediateSync() {
        // TODO: Reimplementar cuando SyncWorker esté disponible
        /*
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniqueWork(
            "immediate_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
        */
    }
    
    fun cancelAllSync() {
        workManager.cancelUniqueWork("sync_work")
        workManager.cancelUniqueWork("immediate_sync")
    }
}




