package com.example.aisecretary.di

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.aisecretary.BuildConfig
import com.example.aisecretary.SecretaryApplication
import com.example.aisecretary.data.local.database.AppDatabase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * This class provides dependencies throughout the app.
 * In a real-world application, you might want to use Dagger/Hilt.
 */
object AppModule {
    
    // Retrofit instance
    fun provideRetrofit(): Retrofit {
        // Use a default URL if the BuildConfig.OLLAMA_BASE_URL is empty
        val baseUrl = if (BuildConfig.OLLAMA_BASE_URL.isBlank()) {
            "http://localhost:11434/"
        } else {
            // Ensure the URL ends with a slash
            if (BuildConfig.OLLAMA_BASE_URL.endsWith("/")) 
                BuildConfig.OLLAMA_BASE_URL 
            else 
                "${BuildConfig.OLLAMA_BASE_URL}/"
        }
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // Database
    fun provideDatabase(context: Context): AppDatabase {
        return (context.applicationContext as SecretaryApplication).database
    }
    
    // Text-to-Speech
    fun provideTextToSpeech(context: Context, listener: TextToSpeech.OnInitListener): TextToSpeech {
        return TextToSpeech(context, listener)
    }
}