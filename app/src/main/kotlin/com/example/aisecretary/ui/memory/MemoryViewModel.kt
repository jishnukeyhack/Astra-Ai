package com.example.aisecretary.ui.memory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aisecretary.SecretaryApplication
import com.example.aisecretary.ai.memory.MemoryManager
import com.example.aisecretary.data.model.MemoryFact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MemoryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as SecretaryApplication).database
    private val memoryManager = MemoryManager(database.memoryFactDao())
    
    private val _memories = MutableStateFlow<List<MemoryFact>>(emptyList())
    val memories: StateFlow<List<MemoryFact>> = _memories.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        loadMemories()
    }
    
    private fun loadMemories() {
        viewModelScope.launch {
            val query = searchQuery.value
            if (query.isBlank()) {
                _memories.value = database.memoryFactDao().getAllMemoryFacts().first()
            } else {
                _memories.value = database.memoryFactDao().searchMemoryFacts(query).first()
            }
        }
    }
    
    fun searchMemories(query: String) {
        _searchQuery.value = query
        loadMemories()
    }
    
    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryManager.deleteMemory(id)
            loadMemories()
        }
    }
    
    fun clearAllMemories() {
        viewModelScope.launch {
            memoryManager.clearAllMemory()
            _memories.value = emptyList()
        }
    }
    
    fun addOrUpdateMemory(key: String, value: String, id: Long = 0) {
        if (key.isBlank() || value.isBlank()) return
        
        val memoryFact = MemoryFact(
            id = id,
            key = key,
            value = value
        )
        
        viewModelScope.launch {
            database.memoryFactDao().insertMemoryFact(memoryFact)
            loadMemories()
        }
    }
}