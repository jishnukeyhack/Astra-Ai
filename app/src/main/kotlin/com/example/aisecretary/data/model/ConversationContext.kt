package com.example.aisecretary.data.model

data class ConversationContext(
    val recentMessages: List<Message> = emptyList(),
    val memoryFacts: List<MemoryFact> = emptyList()
)