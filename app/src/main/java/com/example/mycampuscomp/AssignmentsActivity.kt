package com.example.mycampuscomp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycampuscomp.adapter.AssignmentAdapter
import com.example.mycampuscomp.model.Assignment
import com.example.mycampuscomp.viewmodel.AssignmentViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AssignmentsActivity : AppCompatActivity() {

    private val viewModel: AssignmentViewModel by viewModels()

    private lateinit var rvAssignments: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var fabAddAssignment: FloatingActionButton
    private lateinit var adapter: AssignmentAdapter

    private val displayDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignments)

        rvAssignments = findViewById(R.id.rvAssignments)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        fabAddAssignment = findViewById(R.id.fabAddAssignment)

        setupRecyclerView()
        observeViewModel()
        setupBottomNavigation()

        fabAddAssignment.setOnClickListener {
            showAssignmentDialog(existing = null)
        }
    }

    private fun setupRecyclerView() {
        adapter = AssignmentAdapter(
            onEdit = { assignment -> showAssignmentDialog(existing = assignment) },
            onDelete = { assignment -> confirmDelete(assignment) }
        )
        rvAssignments.layoutManager = LinearLayoutManager(this)
        rvAssignments.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.assignments.observe(this) { assignments ->
            adapter.submitList(assignments)
            tvEmptyState.visibility = if (assignments.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAssignmentDialog(existing: Assignment?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_assignment, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etAssignmentTitle)
        val etDue = dialogView.findViewById<TextInputEditText>(R.id.etAssignmentDue)
        val spinnerStatus = dialogView.findViewById<Spinner>(R.id.spinnerStatus)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveAssignment)

        val statusOptions = resources.getStringArray(R.array.status_array)

        if (existing != null) {
            tvDialogTitle.text = "Edit Assignment"
            btnSave.text = "Update Assignment"
            etTitle.setText(existing.title)
            etDue.setText(existing.dueDate)
            val statusIndex = statusOptions.indexOf(existing.status)
            if (statusIndex >= 0) spinnerStatus.setSelection(statusIndex)
        }

        etDue.setOnClickListener {
            showDatePicker(etDue)
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val dueDate = etDue.text.toString().trim()
            val status = spinnerStatus.selectedItem.toString()

            if (title.isEmpty() || dueDate.isEmpty()) {
                etTitle.error = if (title.isEmpty()) "Required" else null
                etDue.error = if (dueDate.isEmpty()) "Required" else null
                return@setOnClickListener
            }

            if (existing == null) {
                viewModel.addAssignment(title, dueDate, status)
            } else {
                viewModel.updateAssignment(existing, title, dueDate, status)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDatePicker(target: TextInputEditText) {
        val calendar = Calendar.getInstance()

        // Pre-populate the picker with the date already in the field, if any.
        val existingText = target.text.toString()
        if (existingText.isNotEmpty()) {
            runCatching { displayDateFormat.parse(existingText) }.getOrNull()?.let {
                calendar.time = it
            }
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance()
                picked.set(year, month, dayOfMonth)
                target.setText(displayDateFormat.format(picked.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun confirmDelete(assignment: Assignment) {
        AlertDialog.Builder(this)
            .setTitle("Delete Assignment")
            .setMessage("Delete \"${assignment.title}\"? This can't be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAssignment(assignment)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_assignments

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_timetable -> {
                    startActivity(Intent(this, TimetableActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_ai -> {
                    startActivity(Intent(this, AIStudyAssistantActivity::class.java))
                    true
                }
                R.id.nav_assignments -> true
                R.id.nav_more -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.putExtra("open_drawer", true)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}