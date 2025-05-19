package com.example.newsapp.api


import retrofit2.Response

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MercuryService {
    @GET("parser")
    suspend fun parseArticle(
        @Query("url") url: String,
        @Header("x-api-key") apiKey: String = "YOUR_MERCURY_KEY"
    ): Response<MercuryResponse>
}

// Data Classes
data class MercuryResponse(
    val content: String?,
    val title: String?,
    val error: Boolean
)