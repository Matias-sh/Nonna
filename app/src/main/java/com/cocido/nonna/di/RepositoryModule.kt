package com.cocido.nonna.di

import android.content.Context
import android.content.SharedPreferences
import com.cocido.nonna.data.repository.AuthRepository
import com.cocido.nonna.data.repository.AuthRepositoryImpl
import com.cocido.nonna.data.repository.MemoryRepositoryImpl
import com.cocido.nonna.data.repository.VaultRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para repositorios
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("nonna_prefs", Context.MODE_PRIVATE)
    }
    
    @Provides
    @Singleton
    fun provideMemoryRepository(
        memoryRepositoryImpl: MemoryRepositoryImpl
    ): com.cocido.nonna.domain.repository.MemoryRepository {
        return memoryRepositoryImpl
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
    fun provideVaultRepository(
        vaultRepositoryImpl: VaultRepositoryImpl
    ): com.cocido.nonna.domain.repository.VaultRepository {
        return vaultRepositoryImpl
    }
}

