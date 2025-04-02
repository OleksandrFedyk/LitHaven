package com.example.firstapp.Adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.data.CreateRecycleViewDataClass

class CreateFragmentRecycleViewAdapter(
    private val itemList: List<CreateRecycleViewDataClass>,
    private val onItemClick: (String) -> Unit,
    private val onAddChapterClick: (String) -> Unit,
    private val onEditStoryClick: (String) -> Unit,
    private val onFinishStoryClick: (String) -> Unit
) : RecyclerView.Adapter<CreateFragmentRecycleViewAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addChapterButton: ImageButton = itemView.findViewById(R.id.addNewChapterButton)
        val editStoryButton: ImageButton = itemView.findViewById(R.id.editStoryButtonItem)
        val itemImage: ImageView = itemView.findViewById(R.id.createdStoryItemImage)
        val itemTitle: TextView = itemView.findViewById(R.id.createdStoryItemTitle)
        val finishStoryButton: Button = itemView.findViewById(R.id.finishButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.create_fragment_recycle_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemTitle.text = item.title

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.books_book_svgrepo_com)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.itemImage)

        holder.editStoryButton.isEnabled = !item.finished
        holder.addChapterButton.isEnabled = !item.finished

        if (item.finished) {
            holder.finishStoryButton.text = "Finished"
            holder.finishStoryButton.isEnabled = false
        } else {
            holder.finishStoryButton.text = "Finish Story"
            holder.finishStoryButton.isEnabled = true

            holder.finishStoryButton.setOnClickListener {
                showConfirmationDialog(holder.itemView, item.id)
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(item.id)
        }

        holder.addChapterButton.setOnClickListener {
            onAddChapterClick(item.id)
        }

        holder.editStoryButton.setOnClickListener {
            onEditStoryClick(item.id)
        }
    }

    private fun showConfirmationDialog(view: View, bookId: String) {
        val context = view.context
        AlertDialog.Builder(context)
            .setTitle("Confirm Finish Story")
            .setMessage("Are you sure you want to finish this story?")
            .setPositiveButton("Yes") { _, _ ->
                onFinishStoryClick(bookId)
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun getItemCount(): Int = itemList.size
}