package com.enlightenment.ai.core.analytics

import android.content.Context
import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.analyticsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "analytics_preferences"
)

/**
 * 分析管理器
 * 负责用户行为分析和崩溃报告（隐私优先）
 */
@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dataStore = context.analyticsDataStore
    
    // 用户偏好设置
    private var isAnalyticsEnabled = true
    private var userId: String? = null
    
    init {
        scope.launch {
            // 加载用户设置
            dataStore.data.first().let { prefs ->
                isAnalyticsEnabled = prefs[ANALYTICS_ENABLED_KEY] ?: true
                userId = prefs[USER_ID_KEY] ?: generateUserId()
            }
        }
    }
    
    /**
     * 记录事件
     */
    fun logEvent(eventName: String, params: Bundle? = null) {
        if (!isAnalyticsEnabled) return
        
        scope.launch {
            val event = AnalyticsEvent(
                name = eventName,
                params = params?.toMap() ?: emptyMap(),
                timestamp = System.currentTimeMillis(),
                userId = userId ?: "anonymous"
            )
            
            // 保存到本地
            saveEventLocal(event)
            
            // 批量上传（如果有网络）
            if (shouldUploadEvents()) {
                uploadEvents()
            }
        }
    }
    
    /**
     * 记录屏幕访问
     */
    fun logScreenView(screenName: String) {
        logEvent("screen_view", Bundle().apply {
            putString("screen_name", screenName)
        })
    }
    
    /**
     * 记录用户行为
     */
    fun logUserAction(action: String, category: String, value: String? = null) {
        logEvent("user_action", Bundle().apply {
            putString("action", action)
            putString("category", category)
            value?.let { putString("value", it) }
        })
    }
    
    /**
     * 记录错误
     */
    fun logError(error: Throwable, context: String? = null) {
        if (!isAnalyticsEnabled) return
        
        scope.launch {
            val errorEvent = ErrorEvent(
                message = error.message ?: "Unknown error",
                stackTrace = error.stackTraceToString(),
                context = context,
                timestamp = System.currentTimeMillis(),
                userId = userId ?: "anonymous"
            )
            
            saveErrorLocal(errorEvent)
        }
    }
    
    /**
     * 记录性能指标
     */
    fun logPerformance(metric: String, value: Long) {
        logEvent("performance", Bundle().apply {
            putString("metric", metric)
            putLong("value", value)
        })
    }
    
    /**
     * 设置用户属性
     */
    fun setUserProperty(key: String, value: String) {
        if (!isAnalyticsEnabled) return
        
        scope.launch {
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("user_$key")] = value
            }
        }
    }
    
    /**
     * 启用/禁用分析
     */
    fun setAnalyticsEnabled(enabled: Boolean) {
        isAnalyticsEnabled = enabled
        scope.launch {
            dataStore.edit { prefs ->
                prefs[ANALYTICS_ENABLED_KEY] = enabled
            }
            
            if (!enabled) {
                // 清除已收集的数据
                clearLocalData()
            }
        }
    }
    
    /**
     * 获取崩溃报告
     */
    suspend fun getCrashReport(): CrashReport? {
        val errors = getLocalErrors()
        if (errors.isEmpty()) return null
        
        return CrashReport(
            errors = errors,
            deviceInfo = DeviceInfo(
                model = android.os.Build.MODEL,
                osVersion = android.os.Build.VERSION.SDK_INT,
                appVersion = getAppVersion()
            ),
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun generateUserId(): String {
        val id = UUID.randomUUID().toString()
        scope.launch {
            dataStore.edit { prefs ->
                prefs[USER_ID_KEY] = id
            }
        }
        return id
    }
    
    private suspend fun saveEventLocal(event: AnalyticsEvent) {
        // 实际实现中应该保存到数据库
        // 这里简化为内存存储
    }
    
    private suspend fun saveErrorLocal(error: ErrorEvent) {
        // 实际实现中应该保存到数据库
    }
    
    private suspend fun getLocalErrors(): List<ErrorEvent> {
        // 从本地数据库获取错误
        return emptyList()
    }
    
    private suspend fun uploadEvents() {
        // 批量上传事件到服务器
        // 注意：要遵守隐私政策，匿名化数据
    }
    
    private fun shouldUploadEvents(): Boolean {
        // 检查是否应该上传（有网络、达到批量阈值等）
        return false // 暂时不上传
    }
    
    private suspend fun clearLocalData() {
        // 清除本地分析数据
    }
    
    private fun getAppVersion(): String {
        return try {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun Bundle.toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        keySet().forEach { key ->
            get(key)?.let { value ->
                map[key] = value
            }
        }
        return map
    }
    
    companion object {
        private val ANALYTICS_ENABLED_KEY = booleanPreferencesKey("analytics_enabled")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }
}

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any>,
    val timestamp: Long,
    val userId: String
)

data class ErrorEvent(
    val message: String,
    val stackTrace: String,
    val context: String?,
    val timestamp: Long,
    val userId: String
)

data class CrashReport(
    val errors: List<ErrorEvent>,
    val deviceInfo: DeviceInfo,
    val timestamp: Long
)

data class DeviceInfo(
    val model: String,
    val osVersion: Int,
    val appVersion: String
)

/**
 * 崩溃处理器
 */
class CrashHandler(
    private val analyticsManager: AnalyticsManager
) : Thread.UncaughtExceptionHandler {
    
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // 记录崩溃
        analyticsManager.logError(throwable, "Uncaught exception")
        
        // 调用默认处理器
        defaultHandler?.uncaughtException(thread, throwable)
    }
}