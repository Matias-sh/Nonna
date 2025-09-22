package com.cocido.nonna.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.cocido.nonna.data.local.NonnaDatabase
import com.cocido.nonna.data.local.dao.MemoryDao
import com.cocido.nonna.data.local.dao.PhraseDao
import com.cocido.nonna.data.local.dao.VaultDao
import com.cocido.nonna.core.network.NetworkManager
import com.cocido.nonna.core.security.SecurityManager
import com.cocido.nonna.data.repository.AuthRepositoryImpl
import com.cocido.nonna.data.repository.MemoryRepositoryImpl
import com.cocido.nonna.data.repository.VaultRepositoryImpl
import com.cocido.nonna.data.repository.sync.VaultSyncManager
import com.cocido.nonna.domain.repository.AuthRepository
import com.cocido.nonna.domain.repository.MemoryRepository
import com.cocido.nonna.domain.repository.VaultRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para la aplicación
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideNonnaDatabase(@ApplicationContext context: Context): NonnaDatabase {
        return Room.databaseBuilder(
            context,
            NonnaDatabase::class.java,
            "nonna_database"
        ).build()
    }
    
    @Provides
    fun provideMemoryDao(database: NonnaDatabase): MemoryDao {
        return database.memoryDao()
    }
    
    @Provides
    fun providePhraseDao(database: NonnaDatabase): PhraseDao {
        return database.phraseDao()
    }
    
    @Provides
    fun provideVaultDao(database: NonnaDatabase): VaultDao {
        return database.vaultDao()
    }
    
    @Provides
    @Singleton
    fun provideMemoryRepository(
        memoryDao: MemoryDao,
        phraseDao: PhraseDao,
        memoryRepositoryImpl: MemoryRepositoryImpl
    ): MemoryRepository {
        return memoryRepositoryImpl
    }
    
    @Provides
    @Singleton
    fun provideVaultRepository(
        vaultDao: VaultDao,
        vaultRepositoryImpl: VaultRepositoryImpl
    ): VaultRepository {
        return vaultRepositoryImpl
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository {
        return authRepositoryImpl
    }
    
    @Provides
    @Singleton
    fun provideNetworkManager(
        @ApplicationContext context: Context
    ): NetworkManager {
        return NetworkManager(context)
    }
    
    @Provides
    @Singleton
    fun provideSecurityManager(
        @ApplicationContext context: Context
    ): SecurityManager {
        return SecurityManager(context)
    }
    
    @Provides
    @Singleton
    fun provideVaultSyncManager(
        vaultDao: VaultDao,
        vaultApiService: com.cocido.nonna.data.remote.api.VaultApiService
    ): VaultSyncManager {
        return VaultSyncManager(vaultDao, vaultApiService)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("nonna_prefs", Context.MODE_PRIVATE)
    }
}
