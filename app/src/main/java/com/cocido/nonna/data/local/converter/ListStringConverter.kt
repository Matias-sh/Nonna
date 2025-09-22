package com.cocido.nonna.data.local.converter

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Convertidor de tipos para List<String> en Room usando Moshi
 */
class ListStringConverter {
    
    private val moshi = Moshi.Builder().build()
    private val listStringType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter: JsonAdapter<List<String>> = moshi.adapter(listStringType)
    
    @TypeConverter
    fun fromString(value: String): List<String> {
        return adapter.fromJson(value) ?: emptyList()
    }
    
    @TypeConverter
    fun toString(value: List<String>): String {
        return adapter.toJson(value)
    }
}




