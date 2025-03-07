package com.example.newsapp.model


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName



@Entity(
    tableName = "articles"
)
class NewsData (
    @PrimaryKey(autoGenerate = true) var uid: Int? = null,
    @SerializedName("title") val title: String,
    @SerializedName("urlToImage") val imageUrl: String?,
    @SerializedName("description") val description: String?
)