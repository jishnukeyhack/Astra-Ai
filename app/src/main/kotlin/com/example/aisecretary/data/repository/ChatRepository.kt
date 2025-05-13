package com.example.aisecretary.data.repository

import com.example.aisecretary.ai.llm.LlamaClient
import com.example.aisecretary.ai.memory.MemoryManager
import com.example.aisecretary.data.local.database.dao.MessageDao
import com.example.aisecretary.data.model.ConversationContext
import com.example.aisecretary.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ChatRepository(
    private val messageDao: MessageDao,
    private val llamaClient: LlamaClient,
    private val memoryManager: MemoryManager
) {
    // Get all messages from the database
    fun getAllMessages(): Flow<List<Message>> = messageDao.getAllMessages()

    // Save a message to the database
    suspend fun saveMessage(message: Message): Long {
        return messageDao.insertMessage(message)
    }

    // Process a user message and get AI response
    suspend fun processUserMessage(userMessage: String, memoryEnabled: Boolean = true): Result<String> {
        // Skip memory processing if disabled
        if (!memoryEnabled) {
            // Just send message to LLM without memory context
            return llamaClient.sendMessage(
                message = userMessage,
                context = ConversationContext(
                    recentMessages = messageDao.getAllMessages().first().takeLast(10),
                    memoryFacts = emptyList()
                )
            )
        }
        
        // Existing memory-enabled code...
        val memoryDetectionResult = memoryManager.detectAndExtractMemory(userMessage)
        
        if (memoryDetectionResult.wasMemoryDetected) {
            return Result.success("I've remembered that ${memoryDetectionResult.memoryKey} is ${memoryDetectionResult.memoryValue}.")
        }

        val recentMessages = messageDao.getAllMessages().first().takeLast(10)
        val relevantMemory = memoryManager.getRelevantMemory(userMessage)
        
        val context = ConversationContext(
            recentMessages = recentMessages,
            memoryFacts = relevantMemory
        )

        return llamaClient.sendMessage(
            message = userMessage,
            context = context
        )
    }

    // Clear all messages
    suspend fun clearAllMessages() {
        messageDao.deleteAllMessages()
    }
}