# AI启蒙时光 - 云原生架构设计 v3.0

## 1. 架构概览

### 1.1 设计原则
- **云端优先**: 所有AI计算在云端完成
- **智能缓存**: 多级缓存减少延迟
- **优雅降级**: 完善的失败处理机制
- **用户体验**: 流畅的交互体验

### 1.2 系统架构图
```
┌─────────────────────────────────────────────────────────┐
│                  AI启蒙时光 系统架构                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐  │
│  │              Android 客户端                       │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────┐   │  │
│  │  │   UI层    │ │ViewModel│ │  Repository  │   │  │
│  │  │(Compose) │ │  (MVVM)  │ │   (缓存)     │   │  │
│  │  └──────────┘ └──────────┘ └──────────────┘   │  │
│  │  ┌──────────────────────────────────────────┐   │  │
│  │  │         网络层 (Retrofit + OkHttp)        │   │  │
│  │  │  - 请求队列管理                           │   │  │
│  │  │  - 智能重试机制                           │   │  │
│  │  │  - 响应缓存                              │   │  │
│  │  └──────────────────────────────────────────┘   │  │
│  │  ┌──────────────────────────────────────────┐   │  │
│  │  │           本地存储 (Room + DataStore)     │   │  │
│  │  │  - 用户数据  - 缓存管理  - 离线内容      │   │  │
│  │  └──────────────────────────────────────────┘   │  │
│  └─────────────────────────────────────────────────┘  │
│                          ⇅                             │
│               [HTTPS/JSON + 请求签名]                   │
│                          ⇅                             │
│  ┌─────────────────────────────────────────────────┐  │
│  │                  API Gateway                     │  │
│  │  - 认证授权  - 限流控制  - 请求路由             │  │
│  └─────────────────────────────────────────────────┘  │
│                          ⇅                             │
│  ┌─────────────────────────────────────────────────┐  │
│  │                 AI 服务编排层                     │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────┐   │  │
│  │  │ 请求分发  │ │模型选择  │ │  响应聚合     │   │  │
│  │  │          │ │          │ │              │   │  │
│  │  └──────────┘ └──────────┘ └──────────────┘   │  │
│  └─────────────────────────────────────────────────┘  │
│                          ⇅                             │
│  ┌─────────────────────────────────────────────────┐  │
│  │                  AI 模型服务                      │  │
│  │  ┌───────────────┐ ┌────────────────────────┐  │  │
│  │  │  对话模型     │ │   专项模型              │  │  │
│  │  │ ┌──────────┐ │ │ ┌─────────┐┌─────────┐│  │  │
│  │  │ │GEMINI    │ │ │ │Qwen3    ││BAAI     ││  │  │
│  │  │ │2.5-PRO   │ │ │ │Embed    ││Reranker ││  │  │
│  │  │ └──────────┘ │ │ └─────────┘└─────────┘│  │  │
│  │  │ ┌──────────┐ │ │ ┌─────────┐           │  │  │
│  │  │ │GPT-5-PRO │ │ │ │grok-4   │           │  │  │
│  │  │ │(备用)    │ │ │ │imageGen │           │  │  │
│  │  │ └──────────┘ │ │ └─────────┘           │  │  │
│  │  └───────────────┘ └────────────────────────┘  │  │
│  └─────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## 2. 客户端架构

### 2.1 分层架构实现
```kotlin
// Presentation Layer - UI组件
@Composable
fun StoryScreen(
    viewModel: StoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (uiState) {
        is UiState.Loading -> LoadingAnimation()
        is UiState.Success -> StoryContent(uiState.data)
        is UiState.Error -> ErrorDisplay(uiState.message)
        is UiState.Offline -> OfflineContent()
    }
}

// Domain Layer - 业务逻辑
class GenerateStoryUseCase @Inject constructor(
    private val repository: StoryRepository,
    private val cacheManager: CacheManager
) {
    suspend operator fun invoke(params: StoryParams): Result<Story> {
        // 先检查缓存
        cacheManager.getCachedStory(params)?.let {
            return Result.success(it)
        }
        
        // 调用云端API
        return repository.generateStory(params)
            .onSuccess { story ->
                cacheManager.cacheStory(params, story)
            }
    }
}

// Data Layer - 数据管理
@Singleton
class StoryRepositoryImpl @Inject constructor(
    private val apiService: AIApiService,
    private val localDao: StoryDao,
    private val networkMonitor: NetworkMonitor
) : StoryRepository {
    
    override suspend fun generateStory(params: StoryParams): Result<Story> {
        if (!networkMonitor.isConnected()) {
            // 离线时返回本地内容
            return getOfflineStory(params)
        }
        
        return withContext(Dispatchers.IO) {
            try {
                // 主模型调用
                val story = apiService.generateStoryWithGemini(params)
                    .timeout(3.seconds)
                    
                localDao.insertStory(story.toEntity())
                Result.success(story)
                
            } catch (e: TimeoutCancellationException) {
                // 超时切换备用模型
                tryBackupModel(params)
                
            } catch (e: Exception) {
                // 其他错误使用缓存
                Result.failure(e)
            }
        }
    }
}
```

### 2.2 网络层优化
```kotlin
// 网络配置
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        cacheManager: CacheManager
    ): OkHttpClient {
        val cache = Cache(
            directory = File(context.cacheDir, "http_cache"),
            maxSize = 50L * 1024L * 1024L // 50MB
        )
        
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(AuthInterceptor())
            .addInterceptor(CacheInterceptor(cacheManager))
            .addInterceptor(RetryInterceptor())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .build()
    }
}

// 智能缓存拦截器
class CacheInterceptor(
    private val cacheManager: CacheManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // 如果是可缓存的请求
        if (request.method == "GET" && cacheManager.isCacheable(request)) {
            // 检查本地缓存
            cacheManager.getCachedResponse(request)?.let { cached ->
                if (!cached.isExpired()) {
                    return cached.toResponse()
                }
            }
        }
        
        // 执行网络请求
        val response = chain.proceed(request)
        
        // 缓存成功响应
        if (response.isSuccessful) {
            cacheManager.cacheResponse(request, response)
        }
        
        return response
    }
}
```

### 2.3 缓存策略实现
```kotlin
// 多级缓存管理
@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val memoryCache: MemoryCache,
    private val diskCache: DiskCache
) {
    // 内存缓存 - 快速访问
    private val inMemoryCache = LruCache<String, CacheEntry>(
        maxSize = 50 * 1024 * 1024 // 50MB
    )
    
    // 缓存策略配置
    private val cacheConfig = CacheConfig(
        defaultExpiration = 7.days,
        storyExpiration = 30.days,
        imageExpiration = 90.days
    )
    
    // 智能缓存键生成
    fun generateCacheKey(request: Any): String {
        return when (request) {
            is StoryParams -> "story_${request.age}_${request.theme}_${request.hashCode()}"
            is DialogueRequest -> "dialogue_${request.context.hashCode()}"
            is ImagePrompt -> "image_${request.prompt.hashCode()}"
            else -> request.hashCode().toString()
        }
    }
    
    // 预加载常用内容
    suspend fun preloadContent() {
        withContext(Dispatchers.IO) {
            // 基于使用频率预加载
            val frequentRequests = analyzeUsagePatterns()
            
            frequentRequests.forEach { request ->
                if (!hasCache(request)) {
                    // 后台静默加载
                    loadInBackground(request)
                }
            }
        }
    }
    
    // 缓存清理策略
    fun cleanupCache() {
        // 清理过期缓存
        removeExpiredEntries()
        
        // 如果空间不足，清理最少使用的缓存
        if (isDiskSpaceLow()) {
            removeLeastRecentlyUsed()
        }
    }
}
```

## 3. 云端服务架构

### 3.1 API设计
```yaml
# API端点设计
endpoints:
  # 故事生成
  /api/v1/story/generate:
    method: POST
    request:
      childProfile:
        age: integer
        name: string
        interests: array
      storyParams:
        theme: string
        length: enum[short, medium, long]
        educationalGoals: array
    response:
      story:
        id: string
        title: string
        content: string
        imageUrl: string
        duration: integer
      metadata:
        generatedBy: string
        quality_score: float
    
  # 对话交互
  /api/v1/dialogue/chat:
    method: POST
    request:
      message: string
      context:
        conversationId: string
        childAge: integer
        history: array
    response:
      reply:
        text: string
        emotion: string
        suggestions: array
      
  # 图像生成
  /api/v1/image/generate:
    method: POST
    request:
      prompt: string
      style: string
      childAge: integer
    response:
      image:
        url: string
        thumbnail: string
        generationTime: integer
```

### 3.2 服务编排层
```kotlin
// AI服务编排器
@Service
class AIServiceOrchestrator(
    private val modelSelector: ModelSelector,
    private val rateLimiter: RateLimiter,
    private val metricsCollector: MetricsCollector
) {
    
    suspend fun processRequest(
        request: AIRequest,
        userId: String
    ): AIResponse {
        // 限流检查
        rateLimiter.checkLimit(userId)
        
        // 选择最优模型
        val model = modelSelector.selectModel(
            capability = request.requiredCapability,
            priority = request.priority
        )
        
        // 记录请求开始
        val requestId = UUID.randomUUID()
        metricsCollector.recordRequestStart(requestId, model)
        
        return try {
            // 执行模型调用
            val response = when (model) {
                ModelType.GEMINI -> callGemini(request)
                ModelType.GPT5 -> callGPT5(request)
                else -> throw UnsupportedModelException()
            }
            
            // 记录成功
            metricsCollector.recordSuccess(requestId)
            response
            
        } catch (e: Exception) {
            // 记录失败并降级
            metricsCollector.recordFailure(requestId, e)
            handleFailure(request, e)
        }
    }
    
    // 智能降级处理
    private suspend fun handleFailure(
        request: AIRequest,
        error: Exception
    ): AIResponse {
        return when (error) {
            is TimeoutException -> {
                // 超时切换备用模型
                callBackupModel(request)
            }
            is RateLimitException -> {
                // 限流返回缓存
                getCachedResponse(request)
            }
            else -> {
                // 其他错误返回友好提示
                createFallbackResponse(request)
            }
        }
    }
}

// 模型选择器
@Component
class ModelSelector(
    private val modelHealth: ModelHealthMonitor
) {
    fun selectModel(
        capability: AICapability,
        priority: Priority
    ): ModelType {
        // 根据能力需求和模型健康度选择
        val candidates = getModelsForCapability(capability)
        
        return candidates
            .filter { modelHealth.isHealthy(it) }
            .sortedBy { modelHealth.getLatency(it) }
            .firstOrNull()
            ?: throw NoAvailableModelException()
    }
}
```

### 3.3 模型服务封装
```kotlin
// Gemini模型服务
@Service
class GeminiModelService(
    private val geminiClient: GeminiClient,
    private val promptOptimizer: PromptOptimizer
) {
    suspend fun generateStory(params: StoryParams): Story {
        // 优化提示词
        val optimizedPrompt = promptOptimizer.optimizeForStory(
            """
            为${params.age}岁的孩子创作一个关于${params.theme}的故事。
            要求：
            1. 时长${params.duration}分钟
            2. 包含教育元素：${params.educationalGoals.joinToString()}
            3. 语言简单易懂，充满想象力
            4. 主角名字：${params.childName}
            """
        )
        
        // 调用模型
        val response = geminiClient.generate(
            prompt = optimizedPrompt,
            maxTokens = calculateTokens(params.duration),
            temperature = 0.8
        )
        
        // 后处理
        return StoryPostProcessor.process(response, params)
    }
    
    suspend fun processDialogue(request: DialogueRequest): DialogueResponse {
        val contextPrompt = buildContextPrompt(request)
        
        return geminiClient.chat(
            messages = contextPrompt,
            systemPrompt = """
            你是一个友善的AI学习伙伴，正在和${request.childAge}岁的孩子对话。
            请用适合孩子年龄的语言回复，保持耐心和鼓励。
            如果孩子问到不适合的内容，请巧妙地转移话题。
            """
        )
    }
}
```

## 4. 数据流设计

### 4.1 请求处理流程
```
用户操作 → UI事件 → ViewModel → UseCase → Repository
    ↓                                           ↓
    ↓                                    检查本地缓存
    ↓                                     ↓        ↓
    ↓                                   命中    未命中
    ↓                                     ↓        ↓
    ↓                                 返回缓存   API调用
    ↓                                            ↓
    ↓                                     成功 ← → 失败
    ↓                                      ↓        ↓
    ↓                                  更新缓存   降级处理
    ↓                                      ↓        ↓
    ←──────────────── UI更新 ←─────────────┴────────┘
```

### 4.2 缓存更新策略
```kotlin
// 智能缓存更新
class CacheUpdateStrategy {
    // 后台更新过期缓存
    suspend fun refreshExpiredCache() {
        val expiredEntries = cacheManager.getExpiredEntries()
        
        expiredEntries.forEach { entry ->
            if (entry.isFrequentlyUsed()) {
                // 高频使用的内容后台刷新
                backgroundScope.launch {
                    try {
                        val fresh = apiService.refresh(entry.request)
                        cacheManager.update(entry.key, fresh)
                    } catch (e: Exception) {
                        // 静默失败，保留旧缓存
                    }
                }
            }
        }
    }
    
    // 预测性预加载
    suspend fun predictivePreload(userContext: UserContext) {
        val predictions = PredictionEngine.predict(
            timeOfDay = userContext.currentTime,
            dayOfWeek = userContext.dayOfWeek,
            recentUsage = userContext.recentActivities
        )
        
        predictions.forEach { prediction ->
            if (prediction.probability > 0.7) {
                preloadContent(prediction.content)
            }
        }
    }
}
```

## 5. 错误处理与用户反馈

### 5.1 统一错误处理
```kotlin
// 错误处理器
sealed class AppError : Exception() {
    object NetworkError : AppError()
    object TimeoutError : AppError()
    object ServerError : AppError()
    data class ApiError(val code: Int, val message: String) : AppError()
    object CacheError : AppError()
}

// 用户友好的错误消息
fun AppError.toUserMessage(): String = when (this) {
    is NetworkError -> "小熊猫找不到网络，请检查网络连接"
    is TimeoutError -> "小熊猫在思考，请稍等一下"
    is ServerError -> "服务器开小差了，请稍后再试"
    is ApiError -> "出了点小问题：$message"
    is CacheError -> "本地存储出错了"
}

// UI层错误显示
@Composable
fun ErrorDisplay(
    error: AppError,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 错误动画
        LottieAnimation(
            composition = rememberLottieComposition(R.raw.error_panda),
            iterations = LottieConstants.IterateForever
        )
        
        Text(
            text = error.toUserMessage(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}
```

### 5.2 加载状态优化
```kotlin
// 智能加载显示
@Composable
fun SmartLoadingIndicator(
    loadingState: LoadingState
) {
    when (loadingState) {
        is LoadingState.Initial -> {
            // 初始加载 - 全屏动画
            FullScreenPandaAnimation(
                message = "小熊猫正在准备..."
            )
        }
        
        is LoadingState.Refreshing -> {
            // 刷新 - 顶部进度条
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        is LoadingState.Background -> {
            // 后台加载 - 不显示
        }
        
        is LoadingState.Timeout -> {
            // 超时提示
            TimeoutHint(
                onCancel = loadingState.onCancel,
                onWait = loadingState.onWait
            )
        }
    }
}
```

## 6. 性能监控

### 6.1 性能指标收集
```kotlin
// 性能监控
class PerformanceMonitor {
    fun trackApiCall(
        endpoint: String,
        duration: Long,
        success: Boolean
    ) {
        Firebase.performance.newTrace("api_$endpoint").apply {
            putMetric("duration", duration)
            putAttribute("success", success.toString())
            stop()
        }
    }
    
    fun trackCachePerformance() {
        val stats = CacheStats(
            hitRate = cacheManager.getHitRate(),
            missRate = cacheManager.getMissRate(),
            evictionCount = cacheManager.getEvictionCount()
        )
        
        Analytics.logEvent("cache_performance", stats.toBundle())
    }
}
```

## 7. 架构优势

1. **用户体验优先**
   - 智能缓存减少等待
   - 优雅降级保证可用性
   - 离线支持基础功能

2. **高可靠性**
   - 多模型备份
   - 完善的错误处理
   - 自动重试机制

3. **性能优化**
   - 请求合并
   - 预测性加载
   - 分级缓存

4. **可扩展性**
   - 模块化设计
   - 易于添加新模型
   - 灵活的缓存策略

这个云原生架构确保了在依赖云端AI服务的同时，通过智能缓存、优雅降级等机制，为用户提供流畅、可靠的使用体验。