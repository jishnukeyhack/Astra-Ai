package com.example.aisecretary.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aisecretary.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: Message): Long
    
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE isImportantInfo = 1")
    fun getImportantInfoMessages(): Flow<List<Message>>
    
    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}