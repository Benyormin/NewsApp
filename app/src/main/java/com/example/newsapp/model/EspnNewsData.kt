package com.example.newsapp.model

import com.google.gson.annotations.SerializedName

data class EspnNewsData (
    @SerializedName("articles") val article: List<EspnNewsArticle>
)
data class EspnNewsArticle(
    @SerializedName("type") val type: String,
    @SerializedName("headline") val headline: String,
    @SerializedName("description") val description: String,
    @SerializedName("published") val published: String,
    @SerializedName("images") val images: List<EspnImages>,
    @SerializedName("links") val links: EspnLinks
)

data class EspnLinks(
    @SerializedName("web") val web: Href?
)
data class Href(
    @SerializedName("href") val articleUrl: String?
)

data class EspnImages (
    @SerializedName("type") val imageType: String,
    @SerializedName("url") val imageUrl: String?,
    @SerializedName("height") val height: Int,
    @SerializedName("width") val with: Int
)



