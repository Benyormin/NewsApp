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

        val SUMMARY_BASE_URL: String ="http://summarizer.sagezendegi.ir:8888"



        //https://content.guardianapis.com/search?section=football&type=article&show-fields=trailText,headline,thumbnail,short-url&order-by=newest&orderby=newest&api-key=e781c51d-7f1b-4293-8bf2-d32ef1d1ff28
        val NEWS_KEY = "3d9947c603dd444d834205afbf6d514a"
        val GAURDIAN_KEY = "e781c51d-7f1b-4293-8bf2-d32ef1d1ff28"


        // Tech:
        val WIRED_RSS_URL = "https://www.wired.com/feed/tag/ai/latest/rss"
        val COMPUTERWEEKLY_RSS_URL = "https://www.computerweekly.com/rss/RSS-Feed.xml"


               //Science:

        const val WIREDSCIENCE_RSS_URL = "https://www.wired.com/feed/category/science/latest/rss"
        const val SCIENCEDAILY_RSS_URL = "https://www.sciencedaily.com/rss/top/science.xml"


                      // Environment:
        const val GRIST_RSS_URL = "https://grist.org/feed/"
                      // http://earth911.com/feed/

                            //Business:
        const val  TECHREPUBLIC_RSS_URL = "https://www.techrepublic.com/rssfeeds/articles/"
        const val  WIREDBUSINESS_RSS_URL = "https://www.wired.com/feed/category/business/latest/rss"



                            //Health:
        const val SCIENCEDAILYHEALTH_RSS_URL= "https://www.sciencedaily.com/rss/top/health.xml"
        const val ENVIRONMENTALFACTOR_RSS_URL = "https://factor.niehs.nih.gov/rss_feed.xml" // //TODO:: the image is not received well

                                    //Education:
        const val TEDTALK_RSS_URL = "https://feeds.feedburner.com/tedtalks_video"


                                            //Games:
                                            //https://www.eurogamer.net/feed/features --> filter, + unsuported date -> simple fix
        const val THEGAMER_RSS_URL = "https://www.thegamer.com/feed/category/tg-originals/"

                                        //Crypto:
        const val COINTELEGRAPH_RSS_URL = "https://cointelegraph.com/rss"  //--> looks unsupported
        const val CRYPTOPODCAST_RSS_URL = "https://media.rss.com/bitcoin-and-crypto-news-by-protos/feed.xml"
                                           //https://www.coindesk.com/arc/outboundfeeds/rss -> needs a custom parser


    //Sports
        const val CBSSPORT_RSS_URL = "https://www.cbssports.com/rss/headlines"
        const val SPORTSTAR_RSS_URL = "https://sportstar.thehindu.com/feeder/default.rss" //--> images have some issues
                    //http://feeds.bbci.co.uk/sport/rss.xml
                    //https://www.skysports.com/rss/12040 --> unknown time
        const val YAHOOSPORTS_RSS_URL = "" //images issues

        //Esports, finance

    }

     enum class GuardianSections{
        football

        //sports and so on
    }

}