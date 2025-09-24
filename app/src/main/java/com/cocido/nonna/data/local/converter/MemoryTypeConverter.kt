package com.cocido.nonna.data.local.converter

import androidx.room.TypeConverter
import com.cocido.nonna.domain.model.MemoryType

/**
 * Convertidor de tipos para MemoryType en Room
 */
class MemoryTypeConverter {
    
    @TypeConverter
    fun fromMemoryType(type: MemoryType): String {
        return type.name
    }
    
    @TypeConverter
    fun toMemoryType(type: String): MemoryType {
        return MemoryType.valueOf(type)
    }
}





