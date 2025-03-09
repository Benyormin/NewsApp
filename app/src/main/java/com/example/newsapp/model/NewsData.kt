package com.example.newsapp.model


import android.text.format.DateUtils
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


@Entity(
    tableName = "articles"
)
class NewsData (
    @PrimaryKey(autoGenerate = true) var uid: Int? = null,
    @SerializedName("title") val title: String,
    @SerializedName("urlToImage") val imageUrl: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("publishedAt") val publishedAt: String?,
    @SerializedName("articleUrl") val articleUrl: String,
    @SerializedName("source") val source: Source
)

data class Source(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String
)
