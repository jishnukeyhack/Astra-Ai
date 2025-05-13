package com.example.aisecretary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_facts")
data class MemoryFact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)