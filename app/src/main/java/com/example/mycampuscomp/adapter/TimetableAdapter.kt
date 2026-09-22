package com.example.mycampuscomp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.R
import com.example.mycampuscomp.model.TimetableClass

class TimetableAdapter : ListAdapter<TimetableClass, TimetableAdapter.TimetableViewHolder>(TimetableDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetableViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timetable_class, parent, false)
        return TimetableViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimetableViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TimetableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        private val tvRoom: TextView = itemView.findViewById(R.id.tvRoom)
        private val iconBox: View = itemView.findViewById(R.id.iconBox)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)

        fun bind(timetableClass: TimetableClass) {
            tvTime.text = timetableClass.time
            tvSubject.text = timetableClass.subject
            tvRoom.text = timetableClass.room

            // Customize colors based on subject or type
            val context = itemView.context
            when (timetableClass.type) {
                "Lab" -> {
                    iconBox.backgroundTintList = ContextCompat.getColorStateList(context, R.color.icon_bg_green)
                    ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.green_dark))
                }
                else -> {
                    iconBox.backgroundTintList = ContextCompat.getColorStateList(context, R.color.icon_bg_blue)
                    ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.blue_dark))
                }
            }
        }
    }

    class TimetableDiffCallback : DiffUtil.ItemCallback<TimetableClass>() {
        override fun areItemsTheSame(oldItem: TimetableClass, newItem: TimetableClass): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TimetableClass, newItem: TimetableClass): Boolean {
            return oldItem == newItem
        }
    }
}