package com.example.firstapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.data.EpisodesDataClass

class EpisodeAdapter(private val episodeList: List<EpisodesDataClass>,
                     private val onItemClick: (String, String) -> Unit
    ) :
    RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {


    class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val episodeTitle: TextView = itemView.findViewById(R.id.chapterTitle)
        val chapterNumberTextView: TextView = itemView.findViewById(R.id.chapterNumberTextView)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycle_view_clicked_episodes_item, parent, false)
        return EpisodeViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodeList[position]
        holder.chapterNumberTextView.text = "${position + 1}"
        holder.episodeTitle.text = episode.title

        holder.itemView.setOnClickListener{
            onItemClick(episode.id, episode.bookId)
        }


    }


    override fun getItemCount(): Int {
        return episodeList.size
    }
}