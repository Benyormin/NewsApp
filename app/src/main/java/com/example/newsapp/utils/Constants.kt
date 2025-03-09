package com.example.newsapp.utils

public class Constants {

    companion object{

        //newsapi doc https://newsapi.org/docs/endpoints/everything
        // https://newsapi.org/v2/everything?q=football&apiKey=3d9947c603dd444d834205afbf6d514a

        //https://content.guardianapis.com/search?section=football&type=article&show-fields=headline,thumbnail,short-url&order-by=newest&orderby=newest&api-key=e781c51d-7f1b-4293-8bf2-d32ef1d1ff28
        val NEWS_KEY = "3d9947c603dd444d834205afbf6d514a"
        //
        val GAURDIAN_KEY = "e781c51d-7f1b-4293-8bf2-d32ef1d1ff28"
        val BASE_URL = "https://newsapi.org/v2/"


    }

     enum class GuardianSections{
        FOOTBALL
        //sports and so on
    }

}