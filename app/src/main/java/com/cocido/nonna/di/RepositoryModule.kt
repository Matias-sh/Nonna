package com.cocido.nonna.di

import android.content.Context
import android.content.SharedPreferences
import com.cocido.nonna.data.repository.MemoryRepositoryImpl
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
    ): MemoryRepositoryImpl {
        return memoryRepositoryImpl
    }
}

