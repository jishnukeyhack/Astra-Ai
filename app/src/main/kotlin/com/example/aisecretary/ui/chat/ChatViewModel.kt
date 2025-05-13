package com.example.aisecretary.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aisecretary.SecretaryApplication
import com.example.aisecretary.ai.llm.LlamaClient
import com.example.aisecretary.ai.memory.MemoryManager
import com.example.aisecretary.data.local.database.AppDatabase
import com.example.aisecretary.data.model.Message
import com.example.aisecretary.data.repository.ChatRepository
import com.example.aisecretary.data.repository.VoiceRepository
import com.example.aisecretary.ai.voice.SpeechState
import com.example.aisecretary.di.AppModule
import com.example.aisecretary.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase = (application as SecretaryApplication).database
    private val voiceRepository = VoiceRepository(application)

    // Initialize LLM components
    private val retrofit = AppModule.provideRetrofit()
    private val llamaClient = LlamaClient(retrofit)
    private val memoryManager = MemoryManager(database.memoryFactDao())
    private val chatRepository = ChatRepository(
        database.messageDao(),
        llamaClient,
        memoryManager
    )

    // Settings manager
    private val settingsManager = SettingsManager(getApplication())

    // Messages in the chat
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // Current user input
    private val _currentInput = MutableStateFlow("")
    val currentInput: StateFlow<String> = _currentInput.asStateFlow()

    // UI state
    private val _uiState = MutableStateFlow<UiState>(UiState.Ready)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeSpeechRecognition()
        loadMessages()
        observeSettings()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getAllMessages().collectLatest { messagesList ->
                _messages.value = messagesList
            }
        }
    }

    private fun observeSpeechRecognition() {
        viewModelScope.launch {
            voiceRepository.speechState.collectLatest { state ->
                when (state) {
                    is SpeechState.Result -> {
                        _currentInput.value = state.text
                        _uiState.value = UiState.Ready
                    }
                    is SpeechState.PartialResult -> {
                        _currentInput.value = state.text
                    }
                    is SpeechState.Error -> {
                        _uiState.value = UiState.Error(state.message)
                    }
                    is SpeechState.Listening -> {
                        _uiState.value = UiState.Listening
                    }
                    is SpeechState.Processing -> {
                        _uiState.value = UiState.Processing
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsManager.memoryEnabled.collectLatest { enabled: Boolean ->
                // Update memory usage if needed
            }
        }

        viewModelScope.launch {
            settingsManager.voiceOutputEnabled.collectLatest { enabled: Boolean ->
                // Update voice output behavior if needed
            }
        }
    }

    fun onInputChanged(input: String) {
        _currentInput.value = input
    }

    fun startVoiceInput() {
        voiceRepository.startListening()
    }

    fun stopVoiceInput() {
        voiceRepository.stopListening()
    }

    fun sendMessage() {
        val inputText = currentInput.value.trim()
        if (inputText.isNotEmpty()) {
            // Create a user message
            val userMessage = Message(
                content = inputText,
                isFromUser = true
            )

            viewModelScope.launch {
                chatRepository.saveMessage(userMessage)
                _currentInput.value = ""
                _uiState.value = UiState.Processing

                chatRepository.processUserMessage(inputText).fold(
                    onSuccess = { response ->
                        val assistantMessage = Message(
                            content = response,
                            isFromUser = false
                        )

                        chatRepository.saveMessage(assistantMessage)

                        // Always try to speak the response, regardless of the setting
                        // This is a temporary fix to check if TTS is working at all
                        voiceRepository.speak(response)

                        // Comment this out for testing
                        // if (settingsManager.isVoiceOutputEnabled()) {
                        //     voiceRepository.speak(response)
                        // }

                        _uiState.value = UiState.Ready
                    },
                    onFailure = { error ->
                        _uiState.value = UiState.Error("Error: ${error.message}")

                        val errorMessage = Message(
                            content = "Sorry, I encountered a problem. Please try again.",
                            isFromUser = false
                        )
                        chatRepository.saveMessage(errorMessage)
                    }
                )
            }
        }
    }
    
    fun clearConversation() {
        viewModelScope.launch {
            chatRepository.clearAllMessages()
            // Messages will be cleared automatically through the Flow
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceRepository.cleanup()
    }
}

sealed class UiState {
    object Ready : UiState()
    object Listening : UiState()
    object Processing : UiState()
    data class Error(val message: String) : UiState()
}
