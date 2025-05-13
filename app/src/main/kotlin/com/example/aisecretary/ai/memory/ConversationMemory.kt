package com.example.aisecretary.ai.memory

class ConversationMemory {
    private val memoryStore: MutableList<String> = mutableListOf()

    fun addMemory(memory: String) {
        memoryStore.add(memory)
    }

    fun getMemories(): List<String> {
        return memoryStore
    }

    fun clearMemories() {
        memoryStore.clear()
    }
}