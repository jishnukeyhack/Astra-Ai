package com.example.aisecretary.data.repository

import android.content.Context
import com.example.aisecretary.ai.voice.SpeechRecognizerManager
import com.example.aisecretary.ai.voice.SpeechState
import com.example.aisecretary.ai.voice.TextToSpeechManager
import com.example.aisecretary.ai.voice.TtsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository that manages voice input/output operations.
 */
class VoiceRepository(private val context: Context) {

    private val speechRecognizerManager = SpeechRecognizerManager(context)
    private val textToSpeechManager = TextToSpeechManager(context)

    val speechState: StateFlow<SpeechState> = speechRecognizerManager.speechState
    val ttsState: StateFlow<TtsState> = textToSpeechManager.ttsState

    init {
        speechRecognizerManager.initialize()
    }

    fun startListening() {
        speechRecognizerManager.startListening()
    }

    fun stopListening() {
        speechRecognizerManager.stopListening()
    }

    fun speak(text: String) {
        textToSpeechManager.speak(text)
    }

    fun stopSpeaking() {
        textToSpeechManager.stop()
    }

    fun cleanup() {
        speechRecognizerManager.destroy()
        textToSpeechManager.shutdown()
    }
}