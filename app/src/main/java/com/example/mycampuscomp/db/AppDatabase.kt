package com.example.mycampuscomp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mycampuscomp.model.Assignment
import com.example.mycampuscomp.model.TimetableClass

@Database(entities = [TimetableClass::class, Assignment::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
    abstract fun assignmentDao(): AssignmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mycampus_database"
                )
                    // The timetable_classes table gained a userId column; the local
                    // cache is just a mirror of Firestore, so it's safe to rebuild
                    // it from scratch rather than write a manual migration.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}