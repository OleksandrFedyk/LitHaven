package com.example.firstapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstapp.Adapter.RecycleViewAdapter.ViewHolder
import com.example.firstapp.R
import com.example.firstapp.data.RecycleViewDataClassItem1
import com.example.firstapp.data.RecycleViewItemData

class RecycleViewHomeItem1Adapter(
    private var booksList2 : ArrayList<RecycleViewDataClassItem1>,
    private var onItemClick: (String) -> Unit
): RecyclerView.Adapter<RecycleViewHomeItem1Adapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val title = itemView.findViewById<TextView>(R.id.recycle_view_home_item_title)
        var img = itemView.findViewById<ImageView>(R.id.recycle_view_home_item_image)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycle_view_home_item1, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = booksList2[position]
        holder.title.text = book.title
        Glide.with(holder.itemView.context)
            .load(book.itemImg)
            .placeholder(R.drawable.translate_language_svgrepo_com)
            .into(holder.img)


        holder.itemView.setOnClickListener {
            onItemClick(book.bookId)
        }
    }

    override fun getItemCount(): Int {
         return booksList2.size
    }
}