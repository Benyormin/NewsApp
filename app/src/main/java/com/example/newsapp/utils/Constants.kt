package com.example.newsapp.utils

public class Constants {

    companion object{

        //newsapi doc https://newsapi.org/docs/endpoints/everything
        // https://newsapi.org/v2/everything?q=football&apiKey=3d9947c603dd444d834205afbf6d514a

        val CBS_RSS_URL: String = "https://www.cbssports.com/rss/headlines/soccer/"
        val BBC_RSS_URL: String = "https://feeds.bbci.co.uk/sport/football/rss.xml"
        val BBC2_RSS_URL: String = "https://rss.app/feeds/qrUDYnGryavRfZPu.xml"
        val ESPN_RSS_URL: String = "https://www.espn.com/espn/rss/soccer/news"
        val GOAL_RSS_URL: String = "https://rss.app/feeds/mDeUDKHIm0Xda0qI.xml"
        val FOUR_FOUR_TWO_RSS_URL: String = "https://rss.app/feeds/VsKbiTOAhpFBq6Nx.xml"
        val NINETY_RSS_URL: String = "https://rss.app/feeds/z2JTg8NudzVzDANX.xml"
        val MIRROR_RSS_URL: String = "https://www.mirror.co.uk/sport/football/?service=rss.xml"
        val DAILY_MAIL_RSS_URL: String = "https://www.dailymail.co.uk/sport/football/index.rss"


        val ARSENAL_RSS_URL: String = "https://www.football.london/arsenal-fc/?service=rss"


        val GUARDIAN_BASE_URL: String = "https://content.guardianapis.com/"
        val NEWS_API_BASE_URL: String = "https://newsapi.org/v2/"
        val ESPN_BASE_URL: String = "https://site.api.espn.com/"

        val SUMMARY_BASE_URL: String ="https://59a2-104-28-221-41.ngrok-free.app"



        //https://content.guardianapis.com/search?section=football&type=article&show-fields=trailText,headline,thumbnail,short-url&order-by=newest&orderby=newest&api-key=e781c51d-7f1b-4293-8bf2-d32ef1d1ff28
        val NEWS_KEY = "3d9947c603dd444d834205afbf6d514a"
        val GAURDIAN_KEY = "e781c51d-7f1b-4293-8bf2-d32ef1d1ff28"



    }

     enum class GuardianSections{
        football

        //sports and so on
    }

}