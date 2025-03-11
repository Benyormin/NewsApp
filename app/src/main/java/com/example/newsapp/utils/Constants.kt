package com.example.newsapp.utils

public class Constants {

    companion object{

        //newsapi doc https://newsapi.org/docs/endpoints/everything
        // https://newsapi.org/v2/everything?q=football&apiKey=3d9947c603dd444d834205afbf6d514a

        val GUARDIAN_BASE_URL: String = "https://content.guardianapis.com/"
        val NEWS_API_BASE_URL: String = "https://newsapi.org/v2/"

        //https://content.guardianapis.com/search?section=football&type=article&show-fields=trailText,headline,thumbnail,short-url&order-by=newest&orderby=newest&api-key=e781c51d-7f1b-4293-8bf2-d32ef1d1ff28
        val NEWS_KEY = "3d9947c603dd444d834205afbf6d514a"
        //
        val GAURDIAN_KEY = "e781c51d-7f1b-4293-8bf2-d32ef1d1ff28"



    }

     enum class GuardianSections{
        football
        //sports and so on
    }

}