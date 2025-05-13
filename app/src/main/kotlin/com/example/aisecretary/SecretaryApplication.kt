package com.example.aisecretary

import android.app.Application
import androidx.room.Room
import com.example.aisecretary.data.local.database.AppDatabase

class SecretaryApplication : Application() {
    
    // Single instance of the database
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "secretary_database"
        ).build()
    }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here
    }
}