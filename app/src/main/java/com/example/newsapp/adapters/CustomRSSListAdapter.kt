package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.db.RssUrl

//data class RssUrls(val url: String)
class CustomRSSListAdapter (
    private var rssFeed: MutableList<RssUrl>,
    private val onEditClick: (RssUrl, Int)-> Unit,
    private val onDeleteClick: (RssUrl, Int)-> Unit,

): RecyclerView.Adapter<CustomRSSListAdapter.RssViewHolder>(){
    inner class RssViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvRssUrl: TextView = itemView.findViewById(R.id.tvRssUrl)
        val tvRssName: TextView = itemView.findViewById(R.id.tvrssName)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditRss)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteRss)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RssViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rss_url_item, parent, false)
        return RssViewHolder(view)
    }

    override fun getItemCount(): Int {
        return rssFeed.size
    }

    override fun onBindViewHolder(holder: RssViewHolder, position: Int) {
        val item = rssFeed[position]
        holder.tvRssUrl.text = item.url
        holder.tvRssName.text = item.name
        holder.btnEdit.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                onEditClick(item, currentPos)
            }
        }

        holder.btnDelete.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                onDeleteClick(item, currentPos)
            }
        }
    }

    fun addRssFeed(item: RssUrl){
        rssFeed.add(item)
        notifyItemInserted(rssFeed.size - 1)
    }
    fun removeRssFeed(position: Int){
        if (position < rssFeed.size){
            rssFeed.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, rssFeed.size - position)
        }

    }
    fun updateData (rssList: MutableList<RssUrl>){
        rssFeed = rssList
        notifyDataSetChanged()

    }
    //for editing purpose
    fun updateRssFeed(position: Int, updatedFeed: RssUrl) {
        if (position in 0 until rssFeed.size) {
            rssFeed[position] = updatedFeed
            notifyItemChanged(position)
        }
    }

}


