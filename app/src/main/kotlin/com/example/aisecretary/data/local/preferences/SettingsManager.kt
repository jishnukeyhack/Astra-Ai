package com.example.aisecretary.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages application settings and preferences
 */
class SettingsManager(context: Context) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    // Memory feature settings
    private val _memoryEnabled = MutableStateFlow(isMemoryEnabled())
    val memoryEnabled: Flow<Boolean> = _memoryEnabled.asStateFlow()
    
    // Voice feature settings
    private val _voiceInputEnabled = MutableStateFlow(isVoiceInputEnabled())
    val voiceInputEnabled: Flow<Boolean> = _voiceInputEnabled.asStateFlow()
    
    private val _voiceOutputEnabled = MutableStateFlow(isVoiceOutputEnabled())
    val voiceOutputEnabled: Flow<Boolean> = _voiceOutputEnabled.asStateFlow()
    
    // Memory settings
    fun isMemoryEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_MEMORY_ENABLED, true)
    }
    
    fun setMemoryEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_MEMORY_ENABLED, enabled).apply()
        _memoryEnabled.value = enabled
    }
    
    // Voice input settings
    fun isVoiceInputEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_VOICE_INPUT_ENABLED, true)
    }
    
    fun setVoiceInputEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_VOICE_INPUT_ENABLED, enabled).apply()
        _voiceInputEnabled.value = enabled
    }
    
    // Voice output settings
    fun isVoiceOutputEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_VOICE_OUTPUT_ENABLED, true)
    }
    
    fun setVoiceOutputEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_VOICE_OUTPUT_ENABLED, enabled).apply()
        _voiceOutputEnabled.value = enabled
    }
    
    companion object {
        private const val PREFS_NAME = "ai_secretary_prefs"
        private const val KEY_MEMORY_ENABLED = "memory_enabled"
        private const val KEY_VOICE_INPUT_ENABLED = "voice_input_enabled"
        private const val KEY_VOICE_OUTPUT_ENABLED = "voice_output_enabled"
    }
}