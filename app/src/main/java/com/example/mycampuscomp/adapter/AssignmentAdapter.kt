package com.example.mycampuscomp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.R
import com.example.mycampuscomp.model.Assignment

class AssignmentAdapter(
    private val onEdit: (Assignment) -> Unit,
    private val onDelete: (Assignment) -> Unit
) : ListAdapter<Assignment, AssignmentAdapter.AssignmentViewHolder>(AssignmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_assignment, parent, false)
        return AssignmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        holder.bind(getItem(position), onEdit, onDelete)
    }

    class AssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvAssignmentTitle)
        private val tvDue: TextView = itemView.findViewById(R.id.tvAssignmentDue)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvAssignmentStatus)
        private val ivEdit: View = itemView.findViewById(R.id.ivEditAssignment)
        private val ivDelete: View = itemView.findViewById(R.id.ivDeleteAssignment)

        fun bind(assignment: Assignment, onEdit: (Assignment) -> Unit, onDelete: (Assignment) -> Unit) {
            tvTitle.text = assignment.title
            tvDue.text = itemView.context.getString(R.string.assignment_due_prefix, assignment.dueDate)
            tvStatus.text = assignment.status

            val context = itemView.context
            when (assignment.status) {
                "Urgent" -> {
                    tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_urgent_bg))
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_urgent_text))
                }
                "Completed" -> {
                    tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_completed_bg))
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_completed_text))
                }
                else -> {
                    tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_pending_bg))
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_pending_text))
                }
            }

            ivEdit.setOnClickListener { onEdit(assignment) }
            ivDelete.setOnClickListener { onDelete(assignment) }
        }
    }

    class AssignmentDiffCallback : DiffUtil.ItemCallback<Assignment>() {
        override fun areItemsTheSame(oldItem: Assignment, newItem: Assignment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Assignment, newItem: Assignment): Boolean {
            return oldItem == newItem
        }
    }
}