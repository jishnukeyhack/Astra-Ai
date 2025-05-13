package com.example.aisecretary.ai.memory

import com.example.aisecretary.data.local.database.dao.MemoryFactDao
import com.example.aisecretary.data.local.database.dao.MessageDao
import com.example.aisecretary.data.model.MemoryFact
import com.example.aisecretary.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.regex.Pattern

class MemoryManager(
    private val memoryFactDao: MemoryFactDao,
    private val messageDao: MessageDao? = null
) {
    // Enhanced retriever
    private val memoryRetriever = MemoryRetriever()
    
    // Patterns to detect memory statements
    private val rememberPatterns = listOf(
        Pattern.compile("(?i)remember that (?:my|the) (.+?) (?:is|are) (.+)"),
        Pattern.compile("(?i)save (.+?) as (.+)"),
        Pattern.compile("(?i)store (.+?) as (.+)"),
        Pattern.compile("(?i)note that (?:my|the) (.+?) (?:is|are) (.+)"),
        Pattern.compile("(?i)my (.+?) (?:is|are) (.+)")
    )

    // Detect and extract memory from user message
    suspend fun detectAndExtractMemory(message: String): MemoryDetectionResult {
        for (pattern in rememberPatterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val key = matcher.group(1)?.trim() ?: continue
                val value = matcher.group(2)?.trim() ?: continue
                
                val memoryFact = MemoryFact(
                    key = key,
                    value = value
                )
                
                // Save to database
                memoryFactDao.insertMemoryFact(memoryFact)
                
                return MemoryDetectionResult(
                    wasMemoryDetected = true,
                    memoryKey = key,
                    memoryValue = value
                )
            }
        }
        
        return MemoryDetectionResult(wasMemoryDetected = false)
    }

    // Get all memory facts
    suspend fun getAllMemory(): List<MemoryFact> {
        return memoryFactDao.getAllMemoryFacts().first()
    }

    // Get memory facts relevant to a query using enhanced retrieval
    suspend fun getRelevantMemory(query: String): List<MemoryFact> {
        val allMemory = getAllMemory()
        
        // Get recent messages for context if messageDao is available
        val recentMessages = messageDao?.let {
            it.getAllMessages().first().takeLast(10)
        } ?: emptyList()
        
        return memoryRetriever.retrieveRelevantMemory(
            query = query,
            allMemoryFacts = allMemory,
            recentMessages = recentMessages
        )
    }

    // Search memory with a specific query
    suspend fun searchMemory(query: String): List<MemoryFact> {
        return memoryFactDao.searchMemoryFacts(query).first()
    }

    // Delete a specific memory fact
    suspend fun deleteMemory(id: Long) {
        memoryFactDao.deleteMemoryFact(id)
    }

    // Clear all memory
    suspend fun clearAllMemory() {
        memoryFactDao.deleteAllMemoryFacts()
    }
}

data class MemoryDetectionResult(
    val wasMemoryDetected: Boolean,
    val memoryKey: String = "",
    val memoryValue: String = ""
)