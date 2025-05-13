package com.example.aisecretary.ai.rag

class VectorStore {
    private val vectors = mutableMapOf<String, FloatArray>()

    fun addVector(id: String, vector: FloatArray) {
        vectors[id] = vector
    }

    fun getVector(id: String): FloatArray? {
        return vectors[id]
    }

    fun search(queryVector: FloatArray, topK: Int): List<Pair<String, Float>> {
        return vectors.map { (id, vector) ->
            id to cosineSimilarity(queryVector, vector)
        }.sortedByDescending { it.second }
         .take(topK)
    }

    private fun cosineSimilarity(vecA: FloatArray, vecB: FloatArray): Float {
        val dotProduct = vecA.zip(vecB).map { (a, b) -> a * b }.sum()
        val magnitudeA = Math.sqrt(vecA.map { it * it }.sum().toDouble()).toFloat()
        val magnitudeB = Math.sqrt(vecB.map { it * it }.sum().toDouble()).toFloat()
        return if (magnitudeA == 0f || magnitudeB == 0f) 0f else dotProduct / (magnitudeA * magnitudeB)
    }
}