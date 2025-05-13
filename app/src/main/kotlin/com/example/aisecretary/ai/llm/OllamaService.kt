package com.example.aisecretary.ai.llm

import com.example.aisecretary.ai.llm.model.OllamaRequest
import com.example.aisecretary.ai.llm.model.OllamaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaService {
    @POST("/api/generate")
    suspend fun generateCompletion(
        @Body request: OllamaRequest
    ): Response<OllamaResponse>
}