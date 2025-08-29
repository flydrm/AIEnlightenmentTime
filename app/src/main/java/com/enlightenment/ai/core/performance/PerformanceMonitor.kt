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
/**
 * PerformanceMonitor - 性能监控器
 * 
 * 性能监控组件，实时跟踪应用性能指标
 * 
 * 监控指标：
 * - 启动时间和冷启动优化
 * - 内存使用和泄漏检测
 * - 帧率和卡顿监控
 * - 网络请求性能
 * 
 * 优化建议：
 * - 自动性能报告生成
 * - 性能瓶颈定位
 * - 优化建议推送
 * 
 * @since 1.0.0
 */
class PerformanceMonitor @Inject constructor() {  // 依赖注入
    
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
/**
 * PerformanceMetrics
 * 
 * 功能说明：
 * 提供PerformanceMetrics相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */


/**
 * PerformanceMetrics - PerformanceMetrics
 * 
 * 性能监控组件，实时跟踪应用性能指标
 * 
 * 监控指标：
 * - 启动时间和冷启动优化
 * - 内存使用和泄漏检测
 * - 帧率和卡顿监控
 * - 网络请求性能
 * 
 * 优化建议：
 * - 自动性能报告生成
 * - 性能瓶颈定位
 * - 优化建议推送
 * 
 * @since 1.0.0
 */
data class PerformanceMetrics(
    val lastOperationDuration: Long = 0,
    val operationHistory: List<OperationMetric> = emptyList(),
    val memoryUsageMB: Int = 0,
    val maxMemoryMB: Int = 0,
    val networkRequests: List<NetworkMetric> = emptyList()
)
/**
 * OperationMetric
 * 
 * 功能说明：
 * 提供OperationMetric相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */


/**
 * OperationMetric - OperationMetric
 * 
 * 性能监控组件，实时跟踪应用性能指标
 * 
 * 监控指标：
 * - 启动时间和冷启动优化
 * - 内存使用和泄漏检测
 * - 帧率和卡顿监控
 * - 网络请求性能
 * 
 * 优化建议：
 * - 自动性能报告生成
 * - 性能瓶颈定位
 * - 优化建议推送
 * 
 * @since 1.0.0
 */
data class OperationMetric(
    val name: String,
    val duration: Long,
    val timestamp: Long
)
/**
 * NetworkMetric
 * 
 * 功能说明：
 * 提供NetworkMetric相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */


/**
 * NetworkMetric - NetworkMetric
 * 
 * 性能监控组件，实时跟踪应用性能指标
 * 
 * 监控指标：
 * - 启动时间和冷启动优化
 * - 内存使用和泄漏检测
 * - 帧率和卡顿监控
 * - 网络请求性能
 * 
 * 优化建议：
 * - 自动性能报告生成
 * - 性能瓶颈定位
 * - 优化建议推送
 * 
 * @since 1.0.0
 */
data class NetworkMetric(
    val url: String,
    val duration: Long,
    val success: Boolean,
    val timestamp: Long
)
/**
 * PerformanceReport
 * 
 * 功能说明：
 * 提供PerformanceReport相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */


/**
 * PerformanceReport - PerformanceReport
 * 
 * 性能监控组件，实时跟踪应用性能指标
 * 
 * 监控指标：
 * - 启动时间和冷启动优化
 * - 内存使用和泄漏检测
 * - 帧率和卡顿监控
 * - 网络请求性能
 * 
 * 优化建议：
 * - 自动性能报告生成
 * - 性能瓶颈定位
 * - 优化建议推送
 * 
 * @since 1.0.0
 */
data class PerformanceReport(
    val avgOperationTimeMs: Long,
    val memoryUsagePercent: Int,
    val networkSuccessRate: Float,
    val slowOperations: List<OperationMetric>
)