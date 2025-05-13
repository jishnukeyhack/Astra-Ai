package com.example.aisecretary.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.aisecretary.data.local.database.dao.MessageDao
import com.example.aisecretary.data.local.database.dao.MemoryFactDao
import com.example.aisecretary.data.model.Message
import com.example.aisecretary.data.model.MemoryFact
import android.content.Context

@Database(entities = [Message::class, MemoryFact::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun memoryFactDao(): MemoryFactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}