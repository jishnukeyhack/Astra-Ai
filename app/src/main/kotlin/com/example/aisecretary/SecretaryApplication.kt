package com.example.aisecretary

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.room.Room
import com.example.aisecretary.ai.llm.LlamaClient
import com.example.aisecretary.data.local.database.AppDatabase
import com.example.aisecretary.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SecretaryApplication : Application() {
    
    // Application scope for coroutines
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Single instance of the database
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .fallbackToDestructiveMigration() // This will wipe data if migration fails, but prevents crashes
        .build()
    }
    
    // LlamaClient for global access (to unload model if needed)
    val llamaClient: LlamaClient by lazy {
        LlamaClient(AppModule.provideRetrofit())
    }
    
    // Track if any activities are running
    private var runningActivities = 0
    
    override fun onCreate() {
        super.onCreate()
        
        // Register to track activity lifecycle to know when app is truly in background
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            
            override fun onActivityStarted(activity: Activity) {
                runningActivities++
            }
            
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            
            override fun onActivityStopped(activity: Activity) {
                runningActivities--
                if (runningActivities <= 0) {
                    // No activities running - app is in background
                    // We could unload model here, but it's better to keep it for the 60-minute keep_alive
                    // as set in LlamaClient to allow quick resume
                }
            }
            
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            
            override fun onActivityDestroyed(activity: Activity) {
                if (activity.isFinishing && runningActivities <= 1) {
                    // This is likely the last activity and it's explicitly finishing (not due to config change)
                    unloadModel()
                }
            }
        })
    }
    
    private fun unloadModel() {
        applicationScope.launch {
            llamaClient.unloadModel()
        }
    }
}