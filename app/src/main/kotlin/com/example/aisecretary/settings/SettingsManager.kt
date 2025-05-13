package com.example.aisecretary.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Memory enabled setting
    private val _memoryEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_MEMORY_ENABLED, true)
    )
    val memoryEnabled: StateFlow<Boolean> = _memoryEnabled.asStateFlow()

    // Voice output enabled setting
    private val _voiceOutputEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_VOICE_OUTPUT_ENABLED, true)
    )
    val voiceOutputEnabled: StateFlow<Boolean> = _voiceOutputEnabled.asStateFlow()

    fun isMemoryEnabled(): Boolean {
        return preferences.getBoolean(KEY_MEMORY_ENABLED, true)
    }

    fun setMemoryEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_MEMORY_ENABLED, enabled).apply()
        _memoryEnabled.value = enabled
    }

    fun isVoiceOutputEnabled(): Boolean {
        return preferences.getBoolean(KEY_VOICE_OUTPUT_ENABLED, true)
    }

    fun setVoiceOutputEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_VOICE_OUTPUT_ENABLED, enabled).apply()
        _voiceOutputEnabled.value = enabled
    }

    companion object {
        private const val PREFS_NAME = "secretary_settings"
        private const val KEY_MEMORY_ENABLED = "memory_enabled"
        private const val KEY_VOICE_OUTPUT_ENABLED = "voice_output_enabled"
    }
} 