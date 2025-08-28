package com.enlightenment.ai.data.remote.api

import com.enlightenment.ai.BuildConfig

/**
 * AI Model configuration for cloud services
 */
object AIModelConfig {
    // Primary AI Models
    const val GEMINI_MODEL = "gemini-2.5-pro"
    const val GPT_MODEL = "gpt-5-pro"
    
    // Specialized Models
    const val EMBEDDING_MODEL = "Qwen3-Embedding-8B"
    const val RERANKER_MODEL = "BAAI/bge-reranker-v2-m3"
    const val IMAGE_GEN_MODEL = "grok-4-imageGen"
    
    // API Configuration
    val GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY
    val GPT_API_KEY = BuildConfig.GPT_API_KEY
    
    // Endpoints
    const val GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/"
    const val OPENAI_ENDPOINT = "https://api.openai.com/v1/"
    const val TENCENT_AI_ENDPOINT = BuildConfig.API_BASE_URL
    
    // Request Configuration
    const val MAX_TOKENS = 2048
    const val TEMPERATURE = 0.7f
    const val TOP_P = 0.9f
    
    // Timeout Configuration (in seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 60L
    const val WRITE_TIMEOUT = 30L
}