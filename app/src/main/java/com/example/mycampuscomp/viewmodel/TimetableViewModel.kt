package com.example.mycampuscomp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mycampuscomp.db.AppDatabase
import com.example.mycampuscomp.model.TimetableClass
import com.example.mycampuscomp.repository.TimetableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@kotlinx.coroutines.ExperimentalCoroutinesApi
class TimetableViewModel(application: Application) : AndroidViewModel(application) {

    enum class ViewMode { DAY, WEEK }

    private val repository: TimetableRepository
    private val selectedDay = kotlinx.coroutines.flow.MutableStateFlow("Mon")
    private val viewMode = kotlinx.coroutines.flow.MutableStateFlow(ViewMode.DAY)
    private val TAG = "TimetableViewModel"

    val classes: LiveData<List<TimetableClass>> = kotlinx.coroutines.flow.combine(
        selectedDay,
        viewMode
    ) { day, mode -> day to mode }
        .flatMapLatest { (day, mode) ->
            if (mode == ViewMode.WEEK) {
                repository.getAllClassesLocal()
            } else {
                repository.getClassesForDayLocal(day)
            }
        }.asLiveData()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TimetableRepository(db.timetableDao())
        refreshData()
    }

    fun setDay(day: String) {
        selectedDay.value = day
    }

    fun setViewMode(mode: ViewMode) {
        viewMode.value = mode
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Refreshing data...")
            repository.refreshClassesFromFirestore()
            _isLoading.value = false
        }
    }

    fun addClass(subject: String, time: String, room: String, day: String, type: String) {
        viewModelScope.launch {
            Log.d(TAG, "Adding class: $subject for $day")
            val newClass = TimetableClass(
                subject = subject,
                time = time,
                room = room,
                day = day,
                type = type
            )
            repository.saveClass(newClass)
            Log.d(TAG, "Finished addClass call")
        }
    }
}