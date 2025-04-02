package com.example.firstapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.data.SearchBook
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class SearchAdapter(
    private val books: List<SearchBook>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImageView: ImageView = itemView.findViewById(R.id.recycleViewSearchImg)
        val titleTextView: TextView = itemView.findViewById(R.id.recycleViewSearchTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.recycleViewSearchDescription)
        val genresChipGroup: ChipGroup = itemView.findViewById(R.id.recycleViewSearchGenres)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycle_view_search_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]

        // Встановлення текстових даних
        holder.titleTextView.text = book.title
        holder.descriptionTextView.text = book.description

        // Завантаження зображення
        Glide.with(holder.itemView.context)
            .load(book.coverImageUrl)
            .placeholder(R.drawable.translate_language_svgrepo_com)
            .into(holder.coverImageView)

        // Очищення попередніх чіпів
        holder.genresChipGroup.removeAllViews()

        // Додавання жанрів як чіпів
        for ((index, genre) in book.genres.withIndex()) {
            val chip = Chip(holder.genresChipGroup.context).apply {
                text = genre
                isClickable = false
                isFocusable = false
                chipBackgroundColor = ContextCompat.getColorStateList(holder.itemView.context, R.color.genre_chip_background)
                chipStrokeColor = ContextCompat.getColorStateList(holder.itemView.context, R.color.genre_chip_border)
                chipStrokeWidth = 1f
                setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.genre_chip_text))
            }
            holder.genresChipGroup.addView(chip)

            // Якщо більше 2 жанрів, додаємо чіп "+ other"
            if (index == 1 && book.genres.size > 2) {
                val otherChip = Chip(holder.genresChipGroup.context).apply {
                    text = "..."
                    isClickable = false
                    isFocusable = false
                    chipBackgroundColor = ContextCompat.getColorStateList(holder.itemView.context, R.color.genre_chip_background)
                    chipStrokeColor = ContextCompat.getColorStateList(holder.itemView.context, R.color.genre_chip_border)
                    chipStrokeWidth = 1f
                    setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.genre_chip_text))
                }
                holder.genresChipGroup.addView(otherChip)
                break  // Якщо додали "+ other", припиняємо додавати наступні жанри
            }
        }
        holder.itemView.setOnClickListener {
            onItemClick(book.id)
        }
    }

    override fun getItemCount(): Int {
        return books.size
    }
}
