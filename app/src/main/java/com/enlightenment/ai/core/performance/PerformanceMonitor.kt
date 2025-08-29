package com.enlightenment.ai.core.performance

import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 性能监控器
 * 跟踪应用的关键性能指标
 */
@Singleton
class PerformanceMonitor @Inject constructor() {
    
    private val _metrics = MutableStateFlow(PerformanceMetrics())
    val metrics: StateFlow<PerformanceMetrics> = _metrics.asStateFlow()
    
    private val operationStartTimes = mutableMapOf<String, Long>()
    
    /**
     * 开始跟踪操作
     */
    fun startOperation(operationName: String) {
        operationStartTimes[operationName] = SystemClock.elapsedRealtime()
    }
    
    /**
     * 结束跟踪操作并记录耗时
     */
    fun endOperation(operationName: String) {
        val startTime = operationStartTimes.remove(operationName) ?: return
        val duration = SystemClock.elapsedRealtime() - startTime
        
        Log.d("PerformanceMonitor", "$operationName took ${duration}ms")
        
        // 更新指标
        _metrics.value = _metrics.value.copy(
            lastOperationDuration = duration,
            operationHistory = _metrics.value.operationHistory + OperationMetric(
                name = operationName,
                duration = duration,
                timestamp = System.currentTimeMillis()
            )
        )
        
        // 检查性能问题
        if (duration > SLOW_OPERATION_THRESHOLD_MS) {
            Log.w("PerformanceMonitor", "Slow operation detected: $operationName (${duration}ms)")
        }
    }
    
    /**
     * 记录内存使用情况
     */
    fun recordMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 // MB
        val maxMemory = runtime.maxMemory() / 1024 / 1024 // MB
        
        _metrics.value = _metrics.value.copy(
            memoryUsageMB = usedMemory.toInt(),
            maxMemoryMB = maxMemory.toInt()
        )
        
        // 检查内存警告
        if (usedMemory > maxMemory * MEMORY_WARNING_THRESHOLD) {
            Log.w("PerformanceMonitor", "High memory usage: ${usedMemory}MB / ${maxMemory}MB")
        }
    }
    
    /**
     * 记录网络请求
     */
    fun recordNetworkRequest(url: String, duration: Long, success: Boolean) {
        _metrics.value = _metrics.value.copy(
            networkRequests = _metrics.value.networkRequests + NetworkMetric(
                url = url,
                duration = duration,
                success = success,
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * 获取性能报告
     */
    fun getPerformanceReport(): PerformanceReport {
        val metrics = _metrics.value
        val avgOperationTime = if (metrics.operationHistory.isNotEmpty()) {
            metrics.operationHistory.map { it.duration }.average().toLong()
        } else 0L
        
        val networkSuccessRate = if (metrics.networkRequests.isNotEmpty()) {
            metrics.networkRequests.count { it.success }.toFloat() / metrics.networkRequests.size
        } else 1f
        
        return PerformanceReport(
            avgOperationTimeMs = avgOperationTime,
            memoryUsagePercent = if (metrics.maxMemoryMB > 0) {
                (metrics.memoryUsageMB.toFloat() / metrics.maxMemoryMB * 100).toInt()
            } else 0,
            networkSuccessRate = networkSuccessRate,
            slowOperations = metrics.operationHistory.filter { 
                it.duration > SLOW_OPERATION_THRESHOLD_MS 
            }
        )
    }
    
    companion object {
        private const val SLOW_OPERATION_THRESHOLD_MS = 1000L // 1秒
        private const val MEMORY_WARNING_THRESHOLD = 0.8f // 80%内存使用率
    }
}

data class PerformanceMetrics(
    val lastOperationDuration: Long = 0,
    val operationHistory: List<OperationMetric> = emptyList(),
    val memoryUsageMB: Int = 0,
    val maxMemoryMB: Int = 0,
    val networkRequests: List<NetworkMetric> = emptyList()
)

data class OperationMetric(
    val name: String,
    val duration: Long,
    val timestamp: Long
)

data class NetworkMetric(
    val url: String,
    val duration: Long,
    val success: Boolean,
    val timestamp: Long
)

data class PerformanceReport(
    val avgOperationTimeMs: Long,
    val memoryUsagePercent: Int,
    val networkSuccessRate: Float,
    val slowOperations: List<OperationMetric>
)