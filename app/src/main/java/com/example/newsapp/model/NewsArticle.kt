package com.example.newsapp.model

import android.content.Context
import com.example.newsapp.R

data class NewsArticle (val image: Int, val title: String)
{

    companion object {


        fun createData(contex: Context): ArrayList<NewsArticle> {
            lateinit var imageList: Array<Int>
            lateinit var titleList: Array<String>


            imageList = arrayOf(
                R.drawable.news,
                R.drawable.news,
                R.drawable.news,
                R.drawable.news,
                R.drawable.news,
                R.drawable.news,
                R.drawable.news,
                R.drawable.news
            )
            //TODO Wierd syntax
            titleList = arrayOf(
                contex.getString(R.string.lorem),
                contex.getString(R.string.lorem),
                contex.getString(R.string.lorem),
                contex.getString(R.string.lorem),
                contex.getString(R.string.lorem),
                contex.getString(R.string.lorem),
                contex.getString(R.string.lorem),
                contex.getString(R.string.lorem),
                )

            val news = ArrayList<NewsArticle>()
            for (i in imageList.indices) {
                val data = NewsArticle(imageList[i], titleList[i])
                news.add(data)
            }
            return news
        }
    }
}