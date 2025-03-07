package com.example.newsapp.api

import com.example.newsapp.model.NewsData
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface NewsApiService {
    @GET("everything")
    //@GET("top-headlines")
    suspend fun getNewsByCategory(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String
    ): Response<NewsResponse>
}

public data class NewsResponse(
    @SerializedName("articles") val articles: List<NewsData>
)