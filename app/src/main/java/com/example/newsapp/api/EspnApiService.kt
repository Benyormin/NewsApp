package com.example.newsapp.api

import com.example.newsapp.model.EspnNewsData
import retrofit2.Response
import retrofit2.http.GET

interface EspnApiService {
    @GET("apis/site/v2/sports/soccer/all/news")
    suspend fun getEspnNews(): Response<EspnNewsData>
}