package com.example.mycampuscomp

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.model.NotificationModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private val notifications: MutableList<NotificationModel>,
    private val onClick: (NotificationModel) -> Unit = {}
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.cardNotification)
        val icon: ImageView = itemView.findViewById(R.id.tvNotificationIcon)
        val title: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        val message: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        val time: TextView = itemView.findViewById(R.id.tvNotificationTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        val ctx = holder.itemView.context

        holder.title.text = notification.title
        holder.message.text = notification.message
        holder.title.setTypeface(null, if (notification.read) Typeface.NORMAL else Typeface.BOLD)
        holder.card.setCardBackgroundColor(
            ContextCompat.getColor(ctx, if (notification.read) R.color.card_surface else R.color.accent_surface)
        )

        holder.icon.setImageResource(
            when (notification.type) {
                "assignment" -> R.drawable.ic_assignment
                "class" -> R.drawable.ic_class_book
                "reminder", "event" -> R.drawable.ic_timetable
                "badge" -> R.drawable.ic_trophy
                "marketplace" -> R.drawable.ic_marketplace
                else -> R.drawable.ic_stat_notification
            }
        )

        holder.time.text = if (notification.timestamp > 0) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(notification.timestamp))
        } else ""

        holder.itemView.setOnClickListener { onClick(notification) }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<NotificationModel>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }
}
