package com.example.newsapp.model


import android.text.format.DateUtils
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Embedded
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
    @ColumnInfo(name = "title") @SerializedName("title") val title: String,
    @ColumnInfo(name = "imageUrl") @SerializedName("urlToImage") val imageUrl: String?,
    @ColumnInfo(name = "description") @SerializedName("description") val description: String?,
    @ColumnInfo(name = "Date") @SerializedName("publishedAt") val publishedAt: String?,
    @ColumnInfo(name = "articleUrl") @SerializedName("url") val articleUrl: String,
    @ColumnInfo(name = "source") @SerializedName("source") val source: Source,
    var isBookmarked: Boolean = false,
    @ColumnInfo(name = "isLike") var isLike: Boolean = false
)

data class Source(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String
)
