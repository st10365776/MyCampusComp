package com.example.mycampuscomp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.R
import com.example.mycampuscomp.model.TimetableClass

// One row in the week view: either a "Monday" / "Tuesday" / ... section
// header, or a class card underneath it.
sealed class ScheduleListItem {
    data class DayHeader(val dayLabel: String) : ScheduleListItem()
    data class ClassRow(val timetableClass: TimetableClass) : ScheduleListItem()
}

class TimetableWeekAdapter :
    ListAdapter<ScheduleListItem, RecyclerView.ViewHolder>(ScheduleDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CLASS = 1

        // "Mon" -> "Monday", etc. Falls back to the raw value for anything
        // unexpected rather than crashing.
        val fullDayNames = mapOf(
            "Mon" to "Monday",
            "Tue" to "Tuesday",
            "Wed" to "Wednesday",
            "Thu" to "Thursday",
            "Fri" to "Friday",
            "Sat" to "Saturday",
            "Sun" to "Sunday"
        )

        // Groups a flat, already day-and-time-sorted list of classes into
        // [DayHeader, ClassRow, ClassRow, DayHeader, ClassRow, ...].
        fun buildWeekList(classes: List<TimetableClass>): List<ScheduleListItem> {
            val items = mutableListOf<ScheduleListItem>()
            var lastDay: String? = null
            for (cls in classes) {
                if (cls.day != lastDay) {
                    items.add(ScheduleListItem.DayHeader(fullDayNames[cls.day] ?: cls.day))
                    lastDay = cls.day
                }
                items.add(ScheduleListItem.ClassRow(cls))
            }
            return items
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ScheduleListItem.DayHeader -> VIEW_TYPE_HEADER
            is ScheduleListItem.ClassRow -> VIEW_TYPE_CLASS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = inflater.inflate(R.layout.item_schedule_day_header, parent, false)
            DayHeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_timetable_class, parent, false)
            ClassViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ScheduleListItem.DayHeader -> (holder as DayHeaderViewHolder).bind(item)
            is ScheduleListItem.ClassRow -> (holder as ClassViewHolder).bind(item.timetableClass)
        }
    }

    class DayHeaderViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayHeader: TextView = itemView.findViewById(R.id.tvDayHeader)

        fun bind(header: ScheduleListItem.DayHeader) {
            tvDayHeader.text = header.dayLabel
        }
    }

    class ClassViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        private val tvRoom: TextView = itemView.findViewById(R.id.tvRoom)
        private val iconBox: android.view.View = itemView.findViewById(R.id.iconBox)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)

        fun bind(timetableClass: TimetableClass) {
            tvTime.text = timetableClass.time
            tvSubject.text = timetableClass.subject
            tvRoom.text = timetableClass.room

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

    class ScheduleDiffCallback : DiffUtil.ItemCallback<ScheduleListItem>() {
        override fun areItemsTheSame(oldItem: ScheduleListItem, newItem: ScheduleListItem): Boolean {
            return when {
                oldItem is ScheduleListItem.DayHeader && newItem is ScheduleListItem.DayHeader ->
                    oldItem.dayLabel == newItem.dayLabel
                oldItem is ScheduleListItem.ClassRow && newItem is ScheduleListItem.ClassRow ->
                    oldItem.timetableClass.id == newItem.timetableClass.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: ScheduleListItem, newItem: ScheduleListItem): Boolean {
            return oldItem == newItem
        }
    }
}