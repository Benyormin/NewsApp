package com.example.newsapp.model

import com.google.gson.annotations.SerializedName


data class GuardianNewsData(
    @SerializedName("response") val response: GuardianResponse
)
data class GuardianResponse(
    @SerializedName("results") val results: List<GuardianArticle>
)

data class GuardianArticle(
    @SerializedName("webTitle") val title: String,
    @SerializedName("webPublicationDate") val publishedDate: String,
    @SerializedName("webUrl") val url: String,
    @SerializedName("fields") val fields: GuardianFields,
    @SerializedName("sectionName") val sectionName: String
)
data class GuardianFields(
    @SerializedName("thumbnail") val imageUrl: String?,
    @SerializedName("headline") val headline: String,
    @SerializedName("trailText") val description: String?
)