package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model

import androidx.room.TypeConverter

import com.google.gson.Gson


class Converters {
    @TypeConverter
    fun fromString(value: String): Map<String, Float> {
        val mapType = object : com.google.common.reflect.TypeToken<Map<String, Float>>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMap(map: Map<String, Float>): String {
        return Gson().toJson(map)
    }
}
