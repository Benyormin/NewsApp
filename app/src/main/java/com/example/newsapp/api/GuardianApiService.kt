package com.example.newsapp.api

import com.example.newsapp.utils.Constants
import com.example.newsapp.model.GuardianNewsData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface GuardianApiService {
    @GET("search")
    suspend fun getGuardianNews(
        @Query("q") query: String ="",
        @Query("section") section:String = Constants.GuardianSections.football.toString(),
        @Query("type") type: String = "article",
        @Query("show-fields") fields: String = "trainText,headline,thumbnail",
        @Query("order-by") orderBy: String = "newest",
        @Query("api-key") apiKey: String = Constants.GAURDIAN_KEY,

    ): Response<GuardianNewsData>


    @GET("search")
    suspend fun searchGuardianNews(
        @Query("q") query: String ="",
        @Query("type") type: String = "article",
        @Query("show-fields") fields: String = "trainText,headline,thumbnail",
        @Query("order-by") orderBy: String = "newest",
        @Query("api-key") apiKey: String = Constants.GAURDIAN_KEY,
    ):Response<GuardianNewsData>

}
//TODO: change fields to fields?