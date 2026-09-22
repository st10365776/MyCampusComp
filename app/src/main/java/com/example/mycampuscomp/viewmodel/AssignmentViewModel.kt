package com.example.mycampuscomp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mycampuscomp.db.AppDatabase
import com.example.mycampuscomp.model.Assignment
import com.example.mycampuscomp.repository.AssignmentRepository
import com.example.mycampuscomp.repository.UserRepository
import kotlinx.coroutines.launch

class AssignmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AssignmentRepository
    private val userRepository = UserRepository(context = application)
    private val TAG = "AssignmentViewModel"

    val assignments: LiveData<List<Assignment>>

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AssignmentRepository(db.assignmentDao())
        assignments = repository.getAssignmentsLocal().asLiveData()
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Refreshing assignments...")
            repository.refreshAssignmentsFromFirestore()
            _isLoading.value = false
        }
    }

    fun addAssignment(title: String, dueDate: String, status: String) {
        viewModelScope.launch {
            Log.d(TAG, "Adding assignment: $title")
            val newAssignment = Assignment(title = title, dueDate = dueDate, status = status)
            repository.saveAssignment(newAssignment)
            if (status == "Completed") {
                userRepository.addGamificationPoints(50, "badge_first_assignment")
            } else {
                userRepository.addGamificationPoints(10)
            }
        }
    }

    fun updateAssignment(existing: Assignment, title: String, dueDate: String, status: String) {
        viewModelScope.launch {
            Log.d(TAG, "Updating assignment: ${existing.id}")
            repository.saveAssignment(
                existing.copy(title = title, dueDate = dueDate, status = status)
            )
            if (existing.status != "Completed" && status == "Completed") {
                userRepository.addGamificationPoints(50, "badge_first_assignment")
            }
        }
    }

    fun deleteAssignment(assignment: Assignment) {
        viewModelScope.launch {
            Log.d(TAG, "Deleting assignment: ${assignment.id}")
            repository.deleteAssignment(assignment)
        }
    }
}