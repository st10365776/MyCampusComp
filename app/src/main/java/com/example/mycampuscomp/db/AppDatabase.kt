package com.example.mycampuscomp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mycampuscomp.model.Assignment
import com.example.mycampuscomp.model.NotificationModel
import com.example.mycampuscomp.model.TimetableClass

@Database(entities = [TimetableClass::class, Assignment::class, NotificationModel::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Adds the offline-sync flag while keeping existing cached rows.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE assignments ADD COLUMN syncState TEXT NOT NULL DEFAULT 'SYNCED'")
                db.execSQL("ALTER TABLE timetable_classes ADD COLUMN syncState TEXT NOT NULL DEFAULT 'SYNCED'")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `notifications` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                        "`message` TEXT NOT NULL, `type` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, " +
                        "`read` INTEGER NOT NULL, `userId` TEXT NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mycampus_database"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}