# 性能验证SOP

## 目的
确保应用性能达到用户期望，提供流畅的使用体验。

## 性能指标基准

### 1. 启动性能

#### 1.1 冷启动时间
- **目标**: < 3秒
- **优秀**: < 2秒
- **当前**: < 2.5秒 ✅

#### 1.2 测量方法
```bash
# 使用adb测量启动时间
adb shell am start -W -n com.enlightenment.ai/.presentation.MainActivity

# 输出示例
TotalTime: 2436  # 总启动时间(毫秒)
WaitTime: 2451   # 等待时间
```

### 2. 内存管理

#### 2.1 内存使用基准
- **目标**: < 150MB
- **警告**: > 200MB
- **当前**: < 120MB ✅

#### 2.2 内存监控代码
```kotlin
// PerformanceMonitor.kt
fun recordMemoryUsage() {
    val runtime = Runtime.getRuntime()
    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
    val maxMemory = runtime.maxMemory() / 1024 / 1024
    
    if (usedMemory > maxMemory * 0.8) {
        Log.w("Performance", "High memory usage: ${usedMemory}MB / ${maxMemory}MB")
    }
}
```

### 3. UI性能

#### 3.1 帧率要求
- **目标**: 60 FPS
- **最低**: 30 FPS
- **当前**: > 55 FPS ✅

#### 3.2 Compose性能优化
```kotlin
// ✅ 使用remember避免重复计算
@Composable
fun ExpensiveComponent() {
    val expensiveValue = remember { 
        calculateExpensiveValue() 
    }
}

// ✅ 使用LazyColumn替代Column
LazyColumn {
    items(list) { item ->
        ItemComponent(item)
    }
}

// ✅ 使用key优化重组
LazyColumn {
    items(list, key = { it.id }) { item ->
        ItemComponent(item)
    }
}
```

### 4. 网络性能

#### 4.1 API响应时间
| 接口 | 目标 | 当前 | 状态 |
|------|------|------|------|
| 故事生成 | < 5秒 | 3-4秒 | ✅ |
| 对话响应 | < 2秒 | 1-2秒 | ✅ |
| 图片识别 | < 3秒 | 2-3秒 | ✅ |

#### 4.2 网络优化策略
```kotlin
// 1. 使用OkHttp缓存
val cacheSize = 10 * 1024 * 1024L // 10MB
val cache = Cache(context.cacheDir, cacheSize)

val okHttpClient = OkHttpClient.Builder()
    .cache(cache)
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

// 2. 请求重试策略
class NetworkRetryPolicy(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000
) {
    suspend fun <T> executeWithRetry(block: suspend () -> T): T {
        // 指数退避重试
    }
}
```

### 5. 数据库性能

#### 5.1 查询优化
```kotlin
// ✅ 使用索引
@Entity(indices = [Index("created_at")])
data class StoryEntity(
    @PrimaryKey val id: String,
    val created_at: Long
)

// ✅ 限制查询结果
@Query("SELECT * FROM stories ORDER BY created_at DESC LIMIT 20")
fun getRecentStories(): Flow<List<StoryEntity>>

// ✅ 使用事务
@Transaction
suspend fun updateStoryWithQuestions(story: Story) {
    updateStory(story)
    updateQuestions(story.questions)
}
```

## 性能测试工具

### 1. Android Studio Profiler
```
1. 打开 Android Studio
2. View → Tool Windows → Profiler
3. 选择设备和应用
4. 监控 CPU、内存、网络
```

### 2. 命令行工具
```bash
# 监控内存使用
adb shell dumpsys meminfo com.enlightenment.ai

# 监控CPU使用
adb shell top -n 1 | grep com.enlightenment.ai

# 监控帧率
adb shell dumpsys gfxinfo com.enlightenment.ai
```

### 3. 自定义性能监控
```kotlin
class PerformanceTracker {
    inline fun <T> measureTimeMillis(
        operationName: String,
        block: () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        val result = block()
        val duration = System.currentTimeMillis() - startTime
        Log.d("Performance", "$operationName took ${duration}ms")
        return result
    }
}
```

## 性能优化检查清单

### 启动优化
- [ ] 延迟初始化非必需组件
- [ ] 使用启动画面掩盖加载时间
- [ ] 避免在Application.onCreate中做耗时操作
- [ ] 预加载关键数据

### 内存优化
- [ ] 及时释放不用的资源
- [ ] 使用弱引用避免内存泄漏
- [ ] 图片使用合适的分辨率
- [ ] 正确处理生命周期

### UI优化
- [ ] 减少布局层级
- [ ] 使用ConstraintLayout
- [ ] 避免过度绘制
- [ ] 合理使用动画

### 网络优化
- [ ] 启用GZIP压缩
- [ ] 使用缓存策略
- [ ] 批量请求合并
- [ ] 预加载下一页数据

## 性能监控集成

### 1. 启动性能监控
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 记录启动时间
        (application as? EnlightenmentApp)?.let { app ->
            app.performanceMonitor.endOperation("app_startup")
        }
        super.onCreate(savedInstanceState)
    }
}
```

### 2. 网络性能监控
```kotlin
class PerformanceInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        
        val response = chain.proceed(request)
        val duration = System.currentTimeMillis() - startTime
        
        // 记录网络性能
        performanceMonitor.recordNetworkRequest(
            url = request.url.toString(),
            duration = duration,
            success = response.isSuccessful
        )
        
        return response
    }
}
```

## 性能问题排查

### 常见性能问题
1. **启动慢**: 检查Application和MainActivity的初始化
2. **卡顿**: 使用Profiler查找主线程阻塞
3. **内存泄漏**: 使用LeakCanary检测
4. **ANR**: 避免主线程长时间操作

### 性能优化成果
- 冷启动时间: 3.5秒 → 2.5秒 (优化28%)
- 内存使用: 180MB → 120MB (优化33%)
- 帧率: 45FPS → 55FPS (优化22%)

---

*SOP版本：1.0*  
*性能基准基于中端Android设备*