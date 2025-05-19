package com.example.newsapp.api

import retrofit2.http.Body
import retrofit2.http.POST

interface SummaryApiService {
    @POST("summarize")
    suspend fun summarizeArticle(@Body request: UrlRequest): SummaryResponse2
}

data class UrlRequest(val url: String)
data class SummaryResponse2(val title: String, val summary: String)