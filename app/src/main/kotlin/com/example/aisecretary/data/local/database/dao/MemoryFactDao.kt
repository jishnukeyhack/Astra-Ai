package com.example.aisecretary.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aisecretary.data.model.MemoryFact
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryFactDao {
    @Insert
    suspend fun insertMemoryFact(memoryFact: MemoryFact): Long
    
    @Query("SELECT * FROM memory_facts ORDER BY timestamp DESC")
    fun getAllMemoryFacts(): Flow<List<MemoryFact>>
    
    @Query("SELECT * FROM memory_facts WHERE key LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%'")
    fun searchMemoryFacts(query: String): Flow<List<MemoryFact>>
    
    @Query("DELETE FROM memory_facts WHERE id = :id")
    suspend fun deleteMemoryFact(id: Long)
    
    @Query("DELETE FROM memory_facts")
    suspend fun deleteAllMemoryFacts()
}