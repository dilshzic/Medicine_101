package com.algorithmx.medicine101.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromContentList(list: List<ContentBlock>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }

    @TypeConverter
    fun toContentList(json: String?): List<ContentBlock>? {
        if (json.isNullOrEmpty()) return null
        val type = object : TypeToken<List<ContentBlock>>() {}.type
        return gson.fromJson(json, type)
    }
}