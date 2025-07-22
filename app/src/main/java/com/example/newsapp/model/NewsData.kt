package com.example.newsapp.model


import android.os.Parcelable
import android.text.format.DateUtils
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Parcelize
@Entity(
    tableName = "articles"
)
data class NewsData (
    var uid: Int? = null,
    @ColumnInfo(name = "title") @SerializedName("title") val title: String = "",
    @ColumnInfo(name = "imageUrl") @SerializedName("urlToImage") val imageUrl: String? = null,
    @ColumnInfo(name = "description") @SerializedName("description") val description: String? = null,
    @ColumnInfo(name = "Date") @SerializedName("publishedAt") val publishedAt: String? = null,
    @PrimaryKey @ColumnInfo(name = "articleUrl") @SerializedName("url") val articleUrl: String = "",
    @SerializedName("source") @Embedded val source: Source = Source(),
    @ColumnInfo(name = "isBookmarked")var isBookmarked: Boolean = false,
    @ColumnInfo(name = "isLike") var isLike: Boolean = false,
    @ColumnInfo(name = "category") var category: String = ""
) : Parcelable

@Parcelize
data class Source(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String = ""
) : Parcelable
