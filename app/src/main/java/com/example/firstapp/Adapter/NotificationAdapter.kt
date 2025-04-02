package com.example.firstapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firstapp.R
import com.example.firstapp.data.notificationDataClass

class NotificationAdapter(
    private val notificationList: List<notificationDataClass>,
): RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val notificationObject: TextView = itemView.findViewById(R.id.notificationObject)
        val notificationText: TextView = itemView.findViewById(R.id.notificationText)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notificaton_recycle_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notificationList[position]
        holder.notificationObject.text = notification.title
        holder.notificationText.text = notification.description
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}