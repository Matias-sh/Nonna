package com.cocido.nonna.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cocido.nonna.data.local.dao.MemoryDao
import com.cocido.nonna.data.remote.api.MemoryApiService
import com.cocido.nonna.data.remote.dto.toDto
import com.cocido.nonna.data.remote.dto.toEntity
import com.cocido.nonna.data.remote.dto.toRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker para sincronización offline-first
 * Sincroniza datos locales con el servidor
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val memoryDao: MemoryDao,
    private val memoryApiService: MemoryApiService
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("SyncWorker", "Iniciando sincronización...")
            
            // 1. Sincronizar recuerdos pendientes (subir al servidor)
            syncPendingMemories()
            
            // 2. Obtener actualizaciones del servidor
            syncFromServer()
            
            Log.d("SyncWorker", "Sincronización completada exitosamente")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error en sincronización: ${e.message}", e)
            Result.retry()
        }
    }
    
    private suspend fun syncPendingMemories() {
        try {
            // Obtener recuerdos pendientes de sincronización
            val pendingMemories = memoryDao.getPendingSyncMemories("vault_1") // TODO: Obtener vaultId real
            
            for (memoryEntity in pendingMemories) {
                try {
                    // Convertir entidad a DTO y enviar al servidor
                    val memoryDto = memoryEntity.toDto()
                    val response = memoryApiService.createMemory("vault_1", memoryDto.toRequest())
                    
                    // Actualizar entidad local con URLs remotas
                    val updatedEntity = memoryEntity.copy(
                        photoRemoteUrl = response.photoRemoteUrl,
                        audioRemoteUrl = response.audioRemoteUrl,
                        syncStatus = "SYNCED"
                    )
                    memoryDao.updateMemory(updatedEntity)
                    
                    Log.d("SyncWorker", "Recuerdo ${memoryEntity.id} sincronizado exitosamente")
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Error sincronizando recuerdo ${memoryEntity.id}: ${e.message}")
                    // Continuar con el siguiente recuerdo
                }
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error obteniendo recuerdos pendientes: ${e.message}")
        }
    }
    
    private suspend fun syncFromServer() {
        try {
            // Obtener recuerdos del servidor
            val serverMemories = memoryApiService.getMemories("vault_1") // TODO: Obtener vaultId real
            
            for (memoryDto in serverMemories) {
                try {
                    // Convertir DTO a entidad y guardar localmente
                    val memoryEntity = memoryDto.toEntity()
                    memoryDao.insertMemory(memoryEntity)
                    
                    Log.d("SyncWorker", "Recuerdo ${memoryDto.id} descargado del servidor")
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Error guardando recuerdo ${memoryDto.id}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error obteniendo recuerdos del servidor: ${e.message}")
        }
    }
}

