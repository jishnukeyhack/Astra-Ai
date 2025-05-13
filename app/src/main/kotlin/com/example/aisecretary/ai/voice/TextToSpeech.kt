package com.example.aisecretary.ai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * Manages Text-to-Speech functionality for the app.
 */
class TextToSpeechManager(
    context: Context
) {
    private var textToSpeech: TextToSpeech? = null
    private val _ttsState = MutableStateFlow<TtsState>(TtsState.Idle)
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    _ttsState.value = TtsState.Error("Language not supported")
                } else {
                    _ttsState.value = TtsState.Ready
                    setupTtsListener()
                }
            } else {
                _ttsState.value = TtsState.Error("Initialization failed")
            }
        }
    }

    private fun setupTtsListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _ttsState.value = TtsState.Speaking
            }

            override fun onDone(utteranceId: String?) {
                _ttsState.value = TtsState.Ready
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _ttsState.value = TtsState.Error("Error during speech")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                super.onError(utteranceId, errorCode)
                _ttsState.value = TtsState.Error("Error code: $errorCode")
            }
        })
    }

    fun speak(text: String) {
        if (_ttsState.value is TtsState.Ready) {
            val utteranceId = UUID.randomUUID().toString()
            textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
            )
        }
    }

    fun stop() {
        textToSpeech?.stop()
        _ttsState.value = TtsState.Ready
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}

/**
 * Represents different states of text-to-speech operations.
 */
sealed class TtsState {
    object Idle : TtsState()
    object Ready : TtsState()
    object Speaking : TtsState()
    data class Error(val message: String) : TtsState()
}