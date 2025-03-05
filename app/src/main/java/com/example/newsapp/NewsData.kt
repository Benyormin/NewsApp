package com.example.newsapp

import com.google.gson.annotations.SerializedName

class NewsData (

    @SerializedName("title") val title: String,
    @SerializedName("urlToImage") val imageUrl: String?,
    @SerializedName("description") val description: String?
)