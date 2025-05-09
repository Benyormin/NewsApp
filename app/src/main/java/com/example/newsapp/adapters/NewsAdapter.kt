package com.example.newsapp.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.newsapp.R
import com.example.newsapp.model.NewsData
import com.example.newsapp.utils.HelperFuncitons
import com.example.newsapp.viewmodel.NewsViewModel
//FUTURE: add viewModel to this constructor and remove two click items
class NewsAdapter (private var newsDataList: List<NewsData>,
                   private val onItemClick: (NewsData) -> Unit,
                   private val onBookmarkClick: (NewsData) ->Unit,
                   private val onLikeClick: (NewsData)-> Unit
    )
    : RecyclerView.Adapter<NewsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_news_layout, parent,
            false
        )
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //val currentItem = newsDataList[position]
        //holder.rvimage.setImageResource(currentItem.image)
        //holder.rvTitle.text = currentItem.title
        val article = newsDataList[position]
        holder.bind(article)
    }

    override fun getItemCount(): Int {
        return newsDataList.size
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rvimage: ImageView = itemView.findViewById(R.id.ivNews)
        val rvTitle: TextView = itemView.findViewById(R.id.tvNewsTitle)

        val rvDes: TextView = itemView.findViewById(R.id.articleDescription)
        val rvSource: TextView = itemView.findViewById(R.id.articleSource)
        val rvDateTime: TextView = itemView.findViewById(R.id.articleDateTime)
        val rvLike: ImageView = itemView.findViewById(R.id.ivLike)
        val rvBookmark: ImageView = itemView.findViewById(R.id.ivBookmark)
        val rvShare: ImageView = itemView.findViewById(R.id.ivShare)


        fun bind(article: NewsData) {
            rvTitle.text = article.title

            // Load image using Glide
            if (article.imageUrl != null) {
                Glide.with(itemView.context)
                    .load(article.imageUrl)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.news)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(rvimage)
            } else {
                // Set a placeholder image if no image is available
                rvimage.setImageResource(R.drawable.news)
            }

            if (article.description != null) {
                rvDes.text = article.description
            } else {
                rvDes.text = ""
            }
            if (article.source.name != null) {
                rvSource.text = article.source.name
            } else {
                rvSource.text = ""
            }
            if (article.publishedAt != null) {
                rvDateTime.text = HelperFuncitons.getRelativeTimeAndroid(article.publishedAt)
            } else {
                rvDateTime.text = "  "

            }
            if (article.articleUrl == null) {
                Log.e("NewsAdapter", "URL is null")
            } else {
                Log.d("NewsAdapter", "URL is fetched: ${article.articleUrl}")
            }

            Log.d("NewsAdapter", "here")
            updateBookmarkIcon(article.isBookmarked)
            updateLikeIcon(article.isLike)

            rvLike.setOnClickListener {
                article.isLike = !article.isLike // toggle the state
                updateLikeIcon(article.isLike)
                onLikeClick(article)
                if(article.isLike)
                    Toast.makeText(itemView.context, "Liked !", Toast.LENGTH_SHORT).show()
            }
            rvBookmark.setOnClickListener {
                article.isBookmarked = !article.isBookmarked
                Log.d("Bookmark", "running onclick listener func")
                onBookmarkClick(article)
                Log.d("Bookmark", "after on click")
                updateBookmarkIcon(article.isBookmarked)
                if(article.isBookmarked)
                    Toast.makeText(itemView.context, "Saved to bookmarks!", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(itemView.context, "Removed from bookmarks!", Toast.LENGTH_SHORT).show()

            }
            rvShare.setOnClickListener {
                //intent
                val shareIntent = Intent().apply{
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, article.articleUrl)
                    type = "text/plain"
                }
                itemView.context.startActivity(Intent.createChooser(shareIntent, "Share via"))
            }


            itemView.setOnClickListener {
                onItemClick(article)
            }


        }
    //TODO: #BUG when I scroll the bookmark would disappear
        fun updateBookmarkIcon(isBookmarked: Boolean) {
            Log.d("NewsAdapter", "Update bookmarked has been called: $isBookmarked")
            if (isBookmarked) {
                rvBookmark.setImageResource(R.drawable.bookmark_filled)
            } else {
                rvBookmark.setImageResource(R.drawable.bookmark_24dp_secondary)
            }

        }

        fun updateLikeIcon(isLike: Boolean) {
            if (isLike) {
                rvLike.setImageResource(R.drawable.like_filled)
            } else {
                rvLike.setImageResource(R.drawable.favorite_24dp_secondary)

            }

        }

    }


    fun updateData (newsList: List<NewsData>){
        newsDataList = newsList
        notifyDataSetChanged()

    }
}

