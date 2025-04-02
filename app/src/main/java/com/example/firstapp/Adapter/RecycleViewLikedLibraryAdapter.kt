package com.example.firstapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.data.LickedLibraryRecycleViewDataClass

class RecycleViewLikedLibraryAdapter(
    private val books: ArrayList<LickedLibraryRecycleViewDataClass>,
    private val onItemClick: (String) -> Unit
): RecyclerView.Adapter<RecycleViewLikedLibraryAdapter.ViewHolder>() {


    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var title = itemView.findViewById<TextView>(R.id.recycleViewLibraryLickedTitle)
        var img = itemView.findViewById<ImageView>(R.id.recycleViewLibraryLickedImg)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycle_view_licked_library_item, parent, false)
            )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val book = books[position]
        holder.title.text = book.title
        Glide.with(holder.itemView.context)
            .load(book.coverImageUrl)
            .placeholder(R.drawable.translate_language_svgrepo_com)
            .into(holder.img)

        holder.itemView.setOnClickListener {
            onItemClick(book.bookId)
        }

    }

    override fun getItemCount(): Int {
        return books.size
    }
}