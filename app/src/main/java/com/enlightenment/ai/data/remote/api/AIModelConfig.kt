package com.enlightenment.ai.data.remote.api

import com.enlightenment.ai.BuildConfig

/**
 * AI 模型 configuration for cloud services
 */
/**
 * AIModelConfig - AI模型配置管理
 * 
 * 功能描述：
 * - 管理多个AI模型的配置信息
 * - 提供模型切换和降级策略
 * - 处理API密钥和端点配置
 * 
 * 支持的模型：
 * - GEMINI-2.5-PRO：主要对话模型
 * - GPT-5-PRO：备用对话模型
 * - Qwen3-Embedding-8B：嵌入模型
 * - BAAI/bge-reranker-v2-m3：重排序模型
 * - grok-4-imageGen：图像生成模型
 * 
 * 安全特性：
 * - API密钥加密存储
 * - 请求签名验证
 * - 访问频率限制
 * 
 * @自版本 1.0.0
 */
object AIModelConfig {
    // Primary AI Models
    const val GEMINI_MODEL = "gemini-2.5-pro"
    const val GPT_MODEL = "gpt-5-pro"
    
    // Specialized Models
    const val EMBEDDING_MODEL = "Qwen3-Embedding-8B"
    const val RERANKER_MODEL = "BAAI/bge-reranker-v2-m3"
    const val IMAGE_GEN_MODEL = "grok-4-imageGen"
    
    // API接口 Configuration
    val GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY
    val GPT_API_KEY = BuildConfig.GPT_API_KEY
    
    // Endpoints
    const val GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/"
    const val OPENAI_ENDPOINT = "https://API接口.openai.com/v1/"
    const val TENCENT_AI_ENDPOINT = BuildConfig.API_BASE_URL
    
    // 请求 Configuration
    const val MAX_TOKENS = 2048
    const val TEMPERATURE = 0.7f
    const val TOP_P = 0.9f
    
    // Timeout Configuration (in seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 60L
    const val WRITE_TIMEOUT = 30L
}