package com.enlightenment.ai.data.remote

import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.min

/**
 * 网络重试策略
 */
/**
 * NetworkRetryPolicy - 网络重试策略
 * 
 * 远程数据访问组件，负责与服务端API通信
 * 
 * @自版本 1.0.0
 */
class NetworkRetryPolicy(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000,
    private val maxDelayMs: Long = 16000,
    private val factor: Double = 2.0
) {
    suspend fun <T> executeWithRetry(
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {  // 捕获并处理异常
                lastException = e
                
                // 判断是否应该重试
                if (!shouldRetry(e) || attempt == maxRetries - 1) {
                    throw e
                }
                
                // 指数退避
                delay(currentDelay)
                currentDelay = min(
                    (currentDelay * factor).toLong(),
                    maxDelayMs
                )
            }
        }
        
        throw lastException ?: IOException("Unknown error after $maxRetries retries")
    }
    
    private fun shouldRetry(exception: Exception): Boolean {
        return when (exception) {
            is IOException -> true // 网络错误
            is HttpException -> {
                // 只重试特定的HTTP错误
                exception.code() in listOf(408, 429, 500, 502, 503, 504)
            }
            else -> false
        }
    }
}

/**
 * 扩展函数：带重试的网络请求
 */
suspend fun <T> retryableNetworkCall(
    retryPolicy: NetworkRetryPolicy = NetworkRetryPolicy(),
    call: suspend () -> T
): Result<T> {
    return try {
        Result.success(retryPolicy.executeWithRetry(call))
    } catch (e: Exception) {  // 捕获并处理异常
        Result.failure(e)
    }
}