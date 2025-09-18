package com.cocido.nonna.sync

import android.content.Context
import androidx.work.*
import com.cocido.nonna.workers.SyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para manejar la sincronización de datos
 * Programa y ejecuta tareas de sincronización con WorkManager
 */
@Singleton
class SyncManager @Inject constructor(
    private val workManager: WorkManager
) {
    
    companion object {
        private const val SYNC_WORK_NAME = "sync_work"
        private const val SYNC_TAG = "sync"
    }
    
    /**
     * Programa una sincronización inmediata
     */
    fun scheduleImmediateSync() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(SYNC_TAG)
            .build()
        
        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
    
    /**
     * Programa sincronización periódica
     * @param intervalMinutes Intervalo en minutos
     */
    fun schedulePeriodicSync(intervalMinutes: Long = 30) {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            intervalMinutes,
            TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(SYNC_TAG)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
    
    /**
     * Cancela todas las tareas de sincronización
     */
    fun cancelSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
    }
    
    /**
     * Obtiene el estado de la sincronización
     */
    fun getSyncStatus(): WorkInfo.State? {
        val workInfos = workManager.getWorkInfosForUniqueWork(SYNC_WORK_NAME).get()
        return workInfos.firstOrNull()?.state
    }
}

