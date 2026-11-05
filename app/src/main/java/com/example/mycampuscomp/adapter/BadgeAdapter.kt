package com.example.mycampuscomp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.R
import com.example.mycampuscomp.model.Badge
import com.google.android.material.card.MaterialCardView

class BadgeAdapter(private val badges: MutableList<Badge> = mutableListOf()) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardBadge: MaterialCardView = itemView.findViewById(R.id.cardBadge)
        val tvEmoji: ImageView = itemView.findViewById(R.id.tvBadgeEmoji)
        val tvTitle: TextView = itemView.findViewById(R.id.tvBadgeTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvBadgeDescription)
        val tvStatus: TextView = itemView.findViewById(R.id.tvBadgeStatus)
    }

    fun updateBadges(newBadges: List<Badge>) {
        if (this.badges != newBadges) {
            this.badges.clear()
            this.badges.addAll(newBadges)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        val context = holder.itemView.context

        holder.tvEmoji.setImageResource(badge.iconRes)
        holder.tvTitle.text = badge.title
        holder.tvDescription.text = badge.description

        if (badge.isUnlocked) {
            holder.cardBadge.alpha = 1.0f
            holder.tvStatus.text = "Unlocked"
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_text))
            holder.tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.badge_earned_bg))
        } else {
            holder.cardBadge.alpha = 0.5f
            holder.tvStatus.text = "Locked"
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_muted))
            holder.tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.badge_locked_bg))
        }
    }

    override fun getItemCount(): Int = badges.size
}
