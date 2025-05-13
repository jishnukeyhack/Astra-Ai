package com.example.aisecretary.ai.rag

class DocumentStore {
    private val documents = mutableListOf<String>()

    fun addDocument(document: String) {
        documents.add(document)
    }

    fun getDocuments(): List<String> {
        return documents
    }

    fun clearDocuments() {
        documents.clear()
    }
}