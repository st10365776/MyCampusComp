package com.example.mycampuscomp

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val icon: TextView = itemView.findViewById(R.id.tvNotificationIcon)
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

        holder.icon.text = when (notification.type) {
            "assignment" -> "\uD83D\uDCDD"
            "class" -> "\uD83D\uDCDA"
            "reminder" -> "\u23F0"
            "sync" -> "\u2601\uFE0F"
            "badge" -> "\uD83C\uDFC6"
            "marketplace" -> "\uD83D\uDED2"
            "event" -> "\uD83D\uDCC5"
            "announcement" -> "\uD83D\uDCE2"
            else -> "\uD83D\uDD14"
        }

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
