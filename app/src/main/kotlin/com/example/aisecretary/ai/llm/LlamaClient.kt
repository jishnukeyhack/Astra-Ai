package com.example.aisecretary.ai.llm

import com.example.aisecretary.BuildConfig
import com.example.aisecretary.ai.llm.model.OllamaOptions
import com.example.aisecretary.ai.llm.model.OllamaRequest
import com.example.aisecretary.data.model.ConversationContext
import com.example.aisecretary.data.model.MemoryFact
import com.example.aisecretary.data.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.io.IOException

class LlamaClient(private val retrofit: Retrofit) {

    private val ollamaService: OllamaService by lazy {
        retrofit.create(OllamaService::class.java)
    }

    /**
     * Sends a message to the LLM and returns the response
     */
    suspend fun sendMessage(
        message: String,
        context: ConversationContext? = null,
        systemPrompt: String = DEFAULT_SYSTEM_PROMPT
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
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
                )
            )

            val response = ollamaService.generateCompletion(request)
            if (response.isSuccessful) {
                val ollamaResponse = response.body()
                if (ollamaResponse != null) {
                    return@withContext Result.success(ollamaResponse.response)
                } else {
                    return@withContext Result.failure(IOException("Empty response body"))
                }
            } else {
                return@withContext Result.failure(
                    IOException("API error: ${response.code()} ${response.message()}")
                )
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