package com.example.aisecretary.ai.llm.model

import com.example.aisecretary.BuildConfig

data class OllamaRequest(
    val model: String = BuildConfig.LLAMA_MODEL_NAME,
    val prompt: String,
    val system: String? = null,
    val stream: Boolean = false,
    val options: OllamaOptions? = OllamaOptions()
)

data class OllamaOptions(
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val topK: Int = 40,
    val maxTokens: Int = 800,
    val presencePenalty: Float = 0.0f,
    val frequencyPenalty: Float = 0.0f
)