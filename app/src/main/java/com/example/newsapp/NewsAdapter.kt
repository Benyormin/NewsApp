package com.example.newsapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NewsAdapter (private val newsDataList: List<NewsData>)
    : RecyclerView.Adapter<NewsAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_news_layout, parent,
            false)
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

    inner class MyViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        val rvimage: ImageView = itemView.findViewById(R.id.ivNews)
        val rvTitle: TextView = itemView.findViewById(R.id.tvNewsTitle)


        fun bind(article: NewsData) {
            rvTitle.text = article.title

            // Load image using Glide
            if (article.imageUrl != null) {
                Glide.with(itemView.context)
                    .load(article.imageUrl)
                    .into(rvimage)
            } else {
                // Set a placeholder image if no image is available
                rvimage.setImageResource(R.drawable.news)
            }
        }

    }



}