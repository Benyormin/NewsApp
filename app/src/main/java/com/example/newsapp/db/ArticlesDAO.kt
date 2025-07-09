package com.example.newsapp.db

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.newsapp.model.NewsData


@Dao
interface ArticlesDAO {

    data class LikeState(
        @ColumnInfo(name = "articleUrl") val url: String,
        @ColumnInfo(name = "isLike") val isLiked: Boolean,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: NewsData): Long



    @Query ("SELECT * FROM articles")
    fun getAllArticles(): LiveData<List<NewsData>>

    @Delete
    suspend fun deleteArticle(article: NewsData)

    @Update
    suspend fun updateArticle(article: NewsData)

    @Query("SELECT * FROM articles WHERE isBookmarked = 1")
    fun getBookmarkedArticles(): LiveData<List<NewsData>>

    @Query("SELECT articleUrl, isLike FROM articles WHERE isLike = 1")
    suspend fun getLikedStates(): List<LikeState>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRss(rssUrl: RssUrl): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rssList: List<RssUrl>)

    @Query("SELECT * FROM rss_urls")
    fun getAllRssUrlStrings(): LiveData<List<RssUrl>>

    @Query("SELECT * FROM rss_urls")
    suspend fun getAllRssUrls(): List<RssUrl>
    @Delete
    suspend fun deleteRss(rssUrl: RssUrl)

    @Query("SELECT * FROM user_preferences WHERE id = 0")
    fun getAllCategories(): LiveData<Preferences>

    @Query("SELECT * FROM user_preferences WHERE id = 0")
    suspend fun getPreferences(): Preferences?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCategories(prefs: Preferences): Long

    // Add this for raw data inspection
    @Query("SELECT categories FROM user_preferences WHERE id = 0")
    suspend fun getRawCategories(): String?
}