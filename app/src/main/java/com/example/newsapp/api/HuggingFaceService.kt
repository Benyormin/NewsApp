package com.example.newsapp.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface HuggingFaceService {
    @Headers("Authorization: Bearer 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")
    @POST("models/facebook/bart-large-cnn")
    suspend fun summarize(
        @Body request: SummaryRequest
    ): Response<List<SummaryResponse>>
}
data class SummaryRequest(
    val inputs: String,
    val parameters: Parameters = Parameters()
)
data class Parameters(
    val max_length: Int = 150,
    val min_length: Int = 30
)

data class SummaryResponse(
    @SerializedName("summary_text") val summary: String
)