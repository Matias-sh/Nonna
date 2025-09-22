package com.cocido.nonna.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cocido.nonna.data.local.converter.ListStringConverter
import com.cocido.nonna.data.local.converter.MemoryTypeConverter
import com.cocido.nonna.data.local.dao.MemoryDao
import com.cocido.nonna.data.local.dao.PhraseDao
import com.cocido.nonna.data.local.dao.VaultDao
import com.cocido.nonna.data.local.entity.MemoryEntity
import com.cocido.nonna.data.local.entity.PhraseEntity
import com.cocido.nonna.data.local.entity.VaultEntity

/**
 * Base de datos local de Room para la aplicación Nonna
 */
@Database(
    entities = [
        MemoryEntity::class,
        PhraseEntity::class,
        VaultEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    MemoryTypeConverter::class,
    ListStringConverter::class
)
abstract class NonnaDatabase : RoomDatabase() {
    
    abstract fun memoryDao(): MemoryDao
    abstract fun phraseDao(): PhraseDao
    abstract fun vaultDao(): VaultDao
    
    companion object {
        const val DATABASE_NAME = "nonna_database"
    }
}
