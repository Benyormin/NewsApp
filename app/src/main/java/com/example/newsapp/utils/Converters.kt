package com.example.newsapp.utils
// Converters.kt
import androidx.room.TypeConverter
import com.example.newsapp.model.Source
import com.google.gson.Gson

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromSource(source: Source): String = gson.toJson(source)

    @TypeConverter
    fun toSource(sourceString: String): Source = gson.fromJson(sourceString, Source::class.java)
}