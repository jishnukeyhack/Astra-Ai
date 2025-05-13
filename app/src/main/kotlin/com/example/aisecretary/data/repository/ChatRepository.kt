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
        
        // Check if the user message contains a memory request
        val memoryDetectionResult = memoryManager.detectAndExtractMemory(userMessage)
        
        if (memoryDetectionResult.wasMemoryDetected) {
            return Result.success("I've remembered that ${memoryDetectionResult.memoryKey} is ${memoryDetectionResult.memoryValue}.")
        }

        // If no memory request in user message, proceed with normal processing
        val recentMessages = messageDao.getAllMessages().first().takeLast(10)
        val relevantMemory = memoryManager.getRelevantMemory(userMessage)
        
        val context = ConversationContext(
            recentMessages = recentMessages,
            memoryFacts = relevantMemory
        )

        // Send message to LLM
        val llmResult = llamaClient.sendMessage(
            message = userMessage,
            context = context
        )
        
        // Check if the response contains a memory instruction (like JSON)
        if (llmResult.isSuccess) {
            val response = llmResult.getOrNull() ?: return llmResult
            
            // Analyze the LLM response for memory instructions
            val responseMemoryResult = memoryManager.detectAndExtractMemoryFromResponse(response)
            
            // If memory was detected in the response, we'll still return the original response
            // but the memory has now been saved to the database
            return if (responseMemoryResult.wasMemoryDetected && responseMemoryResult.isFromJson) {
                // For JSON memory detection, we might want to clean up the response 
                // by removing the raw JSON to make it more readable
                val cleanedResponse = cleanJsonFromResponse(response)
                Result.success(cleanedResponse)
            } else {
                // Return the original response
                llmResult
            }
        }
        
        return llmResult
    }
    
    // Remove JSON blocks from the response to make it more readable
    private fun cleanJsonFromResponse(response: String): String {
        // Improve pattern to find JSON blocks, even with nested structures
        // This finds the outermost JSON object only
        val jsonPattern = Regex("\\{(?:[^{}]|\\{[^{}]*\\})*\\}")
        return response.replace(jsonPattern, "")
            .replace(Regex("\\s+"), " ") // Clean up extra whitespace
            .trim()
    }

    // Clear all messages
    suspend fun clearAllMessages() {
        messageDao.deleteAllMessages()
    }
}