package com.example.mycampuscomp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.model.NotificationModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private val notifications: MutableList<NotificationModel>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val icon: TextView =
            itemView.findViewById(R.id.tvNotificationIcon)

        val title: TextView =
            itemView.findViewById(R.id.tvNotificationTitle)

        val message: TextView =
            itemView.findViewById(R.id.tvNotificationMessage)

        val time: TextView =
            itemView.findViewById(R.id.tvNotificationTime)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_notification,
                parent,
                false
            )

        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: NotificationViewHolder,
        position: Int
    ) {

        val notification = notifications[position]

        holder.title.text = notification.title
        holder.message.text = notification.message

        holder.icon.text = when (notification.type) {
            "assignment" -> "📝"
            "class" -> "📚"
            "marketplace" -> "🛒"
            "event" -> "📅"
            "announcement" -> "📢"
            else -> "🔔"
        }

        holder.time.text =
            if (notification.timestamp > 0) {

                val date = Date(notification.timestamp)

                SimpleDateFormat(
                    "dd MMM yyyy, HH:mm",
                    Locale.getDefault()
                ).format(date)

            } else {
                ""
            }
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    fun updateNotifications(
        newNotifications: List<NotificationModel>
    ) {

        notifications.clear()
        notifications.addAll(newNotifications)

        notifyDataSetChanged()
    }
}