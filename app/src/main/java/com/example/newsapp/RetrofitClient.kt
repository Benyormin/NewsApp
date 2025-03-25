package com.example.newsapp

import com.example.newsapp.api.EspnApiService
import com.example.newsapp.api.GuardianApiService

import com.example.newsapp.api.NewsApiService
import com.example.newsapp.utils.Constants
import com.example.newsapp.view.InsecureTrustManager
import com.example.newsapp.view.createInsecureSslSocketFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// RetrofitClient.kt
class RetrofitClient {
    /*
    companion object {


        private val unsafeHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(createInsecureSslSocketFactory(), InsecureTrustManager())
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        private val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .client(unsafeHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val newsApiService: NewsApiService = retrofit.create(NewsApiService::class.java)

   }
 */

    /*companion object{
        private val retrofit by lazy{
            /*val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
*/
            val unsafeHttpClient = OkHttpClient.Builder()
                .sslSocketFactory(createInsecureSslSocketFactory(), InsecureTrustManager())
                .hostnameVerifier { _, _ -> true }
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(unsafeHttpClient)
                .build()
        }

        val api by lazy{
            retrofit.create(NewsApiService::class.java)

        }
    }

    */

    companion object {
        // Reusable OkHttp Client (if configurations are shared)
        val unsafeHttpClient by lazy {
            OkHttpClient.Builder()
                .sslSocketFactory(createInsecureSslSocketFactory(), InsecureTrustManager())
                .hostnameVerifier { _, _ -> true }
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        }

        // Generic Retrofit builder
        private fun getRetrofit(baseUrl: String): Retrofit {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(unsafeHttpClient) // Use client with shared config
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        // Existing News API Service
        val newsApiService: NewsApiService by lazy {
            getRetrofit(Constants.NEWS_API_BASE_URL).create(NewsApiService::class.java)
        }

        // New Guardian API Service
        val guardianApiService: GuardianApiService by lazy {
            getRetrofit(Constants.GUARDIAN_BASE_URL).create(GuardianApiService::class.java)
        }

        val espnApiService: EspnApiService by lazy{
            getRetrofit(Constants.ESPN_BASE_URL).create(EspnApiService::class.java)
        }
    }

}