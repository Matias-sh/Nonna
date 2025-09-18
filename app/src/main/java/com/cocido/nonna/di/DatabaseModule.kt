package com.cocido.nonna.di

import android.content.Context
import androidx.room.Room
import com.cocido.nonna.data.local.database.NonnaDatabase
import com.cocido.nonna.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para la base de datos Room
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideNonnaDatabase(@ApplicationContext context: Context): NonnaDatabase {
        return Room.databaseBuilder(
            context,
            NonnaDatabase::class.java,
            "nonna_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideMemoryDao(database: NonnaDatabase): MemoryDao {
        return database.memoryDao()
    }
    
    @Provides
    fun providePersonDao(database: NonnaDatabase): PersonDao {
        return database.personDao()
    }
    
    @Provides
    fun provideRelationDao(database: NonnaDatabase): RelationDao {
        return database.relationDao()
    }
    
    @Provides
    fun provideVaultDao(database: NonnaDatabase): VaultDao {
        return database.vaultDao()
    }
    
    @Provides
    fun providePhraseDao(database: NonnaDatabase): PhraseDao {
        return database.phraseDao()
    }
}


