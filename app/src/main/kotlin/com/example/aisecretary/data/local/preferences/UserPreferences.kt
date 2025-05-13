package com.example.aisecretary.data.local.preferences

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isVoiceInputEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_VOICE_INPUT, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_VOICE_INPUT, value).apply()

    var isMemoryStorageEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_MEMORY_STORAGE, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_MEMORY_STORAGE, value).apply()

    companion object {
        private const val PREFS_NAME = "user_preferences"
        private const val KEY_VOICE_INPUT = "key_voice_input"
        private const val KEY_MEMORY_STORAGE = "key_memory_storage"
    }
}