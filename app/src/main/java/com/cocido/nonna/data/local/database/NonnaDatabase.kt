package com.cocido.nonna.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.cocido.nonna.data.local.converter.ListStringConverter
import com.cocido.nonna.data.local.converter.MemoryTypeConverter
import com.cocido.nonna.data.local.converter.RelationTypeConverter
import com.cocido.nonna.data.local.dao.*
import com.cocido.nonna.data.local.entity.*

/**
 * Base de datos Room para Nonna
 * Incluye todas las entidades y DAOs del sistema
 */
@Database(
    entities = [
        MemoryEntity::class,
        PersonEntity::class,
        RelationEntity::class,
        VaultEntity::class,
        PhraseEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    MemoryTypeConverter::class,
    RelationTypeConverter::class,
    ListStringConverter::class
)
abstract class NonnaDatabase : RoomDatabase() {
    
    abstract fun memoryDao(): MemoryDao
    abstract fun personDao(): PersonDao
    abstract fun relationDao(): RelationDao
    abstract fun vaultDao(): VaultDao
    abstract fun phraseDao(): PhraseDao
    
    companion object {
        @Volatile
        private var INSTANCE: NonnaDatabase? = null
        
        fun getDatabase(context: Context): NonnaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NonnaDatabase::class.java,
                    "nonna_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}




