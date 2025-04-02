package com.example.firstapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.R
import com.example.firstapp.data.FavAuthor

class FavAuthorAdapter(
    private val authorList: List<FavAuthor>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<FavAuthorAdapter.ViewHolder>(){
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorNameTextView: TextView = itemView.findViewById(R.id.favAuthorTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fav_authors_fragment_recycle_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return authorList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val author = authorList[position]
        holder.authorNameTextView.text = author.authorName

        holder.itemView.setOnClickListener {
            onItemClick(author.authorId)
        }
    }
}