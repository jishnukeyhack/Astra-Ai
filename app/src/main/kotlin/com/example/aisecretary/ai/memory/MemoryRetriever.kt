package com.example.aisecretary.ai.memory

import com.example.aisecretary.data.model.MemoryFact
import com.example.aisecretary.data.model.Message
import java.util.*

/**
 * Provides more advanced retrieval functionality for memory facts
 */
class MemoryRetriever {

    /**
     * Retrieves memory facts based on relevance to the query
     */
    fun retrieveRelevantMemory(
        query: String,
        allMemoryFacts: List<MemoryFact>,
        recentMessages: List<Message> = emptyList(),
        maxResults: Int = 5
    ): List<MemoryFact> {
        if (allMemoryFacts.isEmpty()) {
            return emptyList()
        }

        // Extract important terms from the query
        val queryTerms = extractImportantTerms(query.lowercase(Locale.getDefault()))
        if (queryTerms.isEmpty()) {
            return emptyList()
        }

        // Calculate relevance scores for each memory fact
        val scoredFacts = allMemoryFacts.map { fact ->
            val factText = "${fact.key} ${fact.value}".lowercase(Locale.getDefault())
            val score = calculateRelevanceScore(factText, queryTerms, recentMessages)
            Pair(fact, score)
        }

        // Sort by relevance score (descending) and take top results
        return scoredFacts
            .sortedByDescending { it.second }
            .take(maxResults)
            .map { it.first }
    }

    /**
     * Extracts important terms from the query
     */
    private fun extractImportantTerms(query: String): List<String> {
        // Remove common stopwords
        val stopwords = setOf(
            "a", "an", "the", "and", "or", "but", "is", "are", "was", "were", 
            "has", "have", "had", "do", "does", "did", "will", "would", "can", 
            "could", "may", "might", "must", "should", "what", "when", "where", 
            "who", "why", "how", "my", "your", "his", "her", "their", "our", "its"
        )
        
        return query.split(Regex("\\W+"))
            .filter { it.length > 2 && it !in stopwords }
            .distinct()
    }

    /**
     * Calculates relevance score between memory fact and query terms
     */
    private fun calculateRelevanceScore(
        factText: String,
        queryTerms: List<String>,
        recentMessages: List<Message>
    ): Double {
        var score = 0.0
        
        // Direct term matching
        for (term in queryTerms) {
            if (factText.contains(term)) {
                // Count occurrences
                val occurrences = factText.windowed(term.length).count { it == term }
                score += 1.0 * occurrences
            }
        }
        
        // Boost score for exact match queries like "What is my X?"
        if (factText.split(" ").any { term -> 
            queryTerms.any { query -> query == term }
        }) {
            score *= 1.5
        }

        // Boost score if the fact was recently mentioned in conversation
        if (recentMessages.isNotEmpty()) {
            val recentContextScore = recentMessages
                .takeLast(5) // Consider only last 5 messages
                .mapIndexed { index, message ->
                    val messageText = message.content.lowercase(Locale.getDefault())
                    val isRelevant = queryTerms.any { messageText.contains(it) }
                    
                    if (isRelevant) {
                        // Give higher weight to more recent messages
                        val recencyWeight = (5.0 - index) / 5.0
                        recencyWeight * 0.5
                    } else {
                        0.0
                    }
                }
                .sum()
            
            score += recentContextScore
        }
        
        return score
    }
}