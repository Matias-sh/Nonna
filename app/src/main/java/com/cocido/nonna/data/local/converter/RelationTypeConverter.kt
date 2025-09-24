package com.cocido.nonna.data.local.converter

import androidx.room.TypeConverter
import com.cocido.nonna.domain.model.RelationType

/**
 * Convertidor de tipos para RelationType en Room
 */
class RelationTypeConverter {
    
    @TypeConverter
    fun fromRelationType(type: RelationType): String {
        return type.name
    }
    
    @TypeConverter
    fun toRelationType(type: String): RelationType {
        return RelationType.valueOf(type)
    }
}





