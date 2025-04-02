package com.example.firstapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.R
import com.example.firstapp.data.RecycleViewSearchData

class RecycleSearchViewAdapter(private var storyItemArray: ArrayList<RecycleViewSearchData>): RecyclerView.Adapter<RecycleSearchViewAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val title = itemView.findViewById<TextView>(R.id.recycleViewSearchTitle)
        var itemImg = itemView.findViewById<ImageView>(R.id.recycleViewSearchImg)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycle_view_search_item,parent,false))
    }

    override fun getItemCount(): Int {
        return storyItemArray.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = storyItemArray[position]

        holder.title.text = item.title
        holder.itemImg.setImageResource(item.img)

    }

    fun updateData(newData: ArrayList<RecycleViewSearchData>) {
        storyItemArray = newData
        notifyDataSetChanged()
    }


}