package com.example.newsapp.utils

import androidx.room.TypeConverter
import com.example.newsapp.model.Source
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromSource(source: Source): String = gson.toJson(source)

    @TypeConverter
    fun toSource(sourceString: String): Source = gson.fromJson(sourceString, Source::class.java)

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
    }
}