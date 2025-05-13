package com.example.aisecretary.ai.llm

import com.example.aisecretary.BuildConfig
import com.example.aisecretary.ai.llm.model.OllamaOptions
import com.example.aisecretary.ai.llm.model.OllamaRequest
import com.example.aisecretary.data.model.ConversationContext
import com.example.aisecretary.data.model.MemoryFact
import com.example.aisecretary.data.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class LlamaClient(private val retrofit: Retrofit) {

    private val ollamaService: OllamaService by lazy {
        retrofit.create(OllamaService::class.java)
    }
    
    // Track the last error time to implement waiting period
    private var lastErrorTime: Long = 0
    private val isRetrying = AtomicBoolean(false)

    /**
     * Sends a message to the LLM and returns the response
     */
    suspend fun sendMessage(
        message: String,
        context: ConversationContext? = null,
        systemPrompt: String = DEFAULT_SYSTEM_PROMPT
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Check if we need to wait after an error
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastErrorTime < 60000 && lastErrorTime > 0) {
                val waitTimeRemaining = 60000 - (currentTime - lastErrorTime)
                if (waitTimeRemaining > 0 && !isRetrying.getAndSet(true)) {
                    try {
                        // Wait the remaining time of the 1-minute cooling period
                        delay(waitTimeRemaining)
                    } finally {
                        isRetrying.set(false)
                    }
                }
            }
            
            // Prepare the full prompt with memory and conversation history
            val fullSystemPrompt = buildEnhancedSystemPrompt(
                systemPrompt, 
                context?.memoryFacts,
                context?.recentMessages
            )

            val request = OllamaRequest(
                model = BuildConfig.LLAMA_MODEL_NAME,
                prompt = message,
                system = fullSystemPrompt,
                stream = false,
                options = OllamaOptions(
                    temperature = 0.7f,
                    maxTokens = 800
                ),
                keep_alive = 3600 // Keep model in memory for 1 hour
            )

            val response = ollamaService.generateCompletion(request)
            if (response.isSuccessful) {
                // Reset error time on success
                lastErrorTime = 0
                
                val ollamaResponse = response.body()
                if (ollamaResponse != null) {
                    return@withContext Result.success(ollamaResponse.response)
                } else {
                    // Record error time
                    lastErrorTime = System.currentTimeMillis()
                    return@withContext Result.failure(IOException("Empty response body"))
                }
            } else {
                // Record error time
                lastErrorTime = System.currentTimeMillis()
                return@withContext Result.failure(
                    IOException("API error: ${response.code()} ${response.message()}")
                )
            }
        } catch (e: Exception) {
            // Record error time
            lastErrorTime = System.currentTimeMillis()
            return@withContext Result.failure(e)
        }
    }

    /**
     * Unloads the model from memory
     */
    suspend fun unloadModel(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = OllamaRequest(
                model = BuildConfig.LLAMA_MODEL_NAME,
                prompt = "",
                stream = false,
                keep_alive = 0 // Immediately unload the model
            )
            
            val response = ollamaService.generateCompletion(request)
            return@withContext if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to unload model: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    /**
     * Builds an enhanced system prompt that includes memory and recent conversation history
     */
    private fun buildEnhancedSystemPrompt(
        systemPrompt: String,
        memoryFacts: List<MemoryFact>?,
        recentMessages: List<Message>?
    ): String {
        val promptBuilder = StringBuilder(systemPrompt)

        // Add memory section if available
        if (!memoryFacts.isNullOrEmpty()) {
            promptBuilder.append("\n\nHere is stored user memory:\n\n")
            
            val memorySection = memoryFacts.joinToString("\n") { fact ->
                "* ${fact.key}: ${fact.value}"
            }
            promptBuilder.append(memorySection)
        }

        // Add recent conversation history if available
        if (!recentMessages.isNullOrEmpty() && recentMessages.size > 1) {
            promptBuilder.append("\n\nRecent conversation history:\n\n")
            
            // Only include a reasonable number of messages to avoid token limits
            val relevantMessages = recentMessages.takeLast(6) 
            
            val conversationSection = relevantMessages.joinToString("\n") { message ->
                if (message.isFromUser) {
                    "User: ${message.content}"
                } else {
                    "Assistant: ${message.content}"
                }
            }
            
            promptBuilder.append(conversationSection)
        }

        return promptBuilder.toString()
    }

    companion object {
        const val DEFAULT_SYSTEM_PROMPT = """
        You are Astra, an AI secretary assistant built to help the user with daily tasks.
        
        Your primary functions are:
        1. Answer questions clearly and concisely
        2. Remember important information the user shares with you
        3. Provide context-aware assistance based on past conversations
        4. Be polite, helpful, and professional at all times
        
        When the user asks you to remember information, acknowledge that you've stored it.
        When responding to questions about information previously shared, reference your memory.
        
        If you don't know something or don't have it in your memory, simply say so.
        Keep responses brief and focused on the user's request.
        """
    }
}