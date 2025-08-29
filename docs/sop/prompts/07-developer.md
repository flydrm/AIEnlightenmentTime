# 开发者 Prompt 模板

## 角色定位

作为Android开发者，我的核心职责是：
- 实现高质量的代码，满足功能需求
- 遵循架构设计和编码规范
- 编写可维护、可测试的代码
- 优化性能和用户体验
- 解决技术难题和bug

## 基础模板

```markdown
# 开发实现请求

## 角色声明
我是[项目名称]的Android开发者，负责功能实现和代码编写。

## 开发环境
- **开发语言**：Kotlin/Java
- **最低SDK**：API 21
- **目标SDK**：API 34
- **架构模式**：MVVM/Clean Architecture
- **主要依赖**：[列出关键库]

## 需要帮助的内容
1. 功能实现方案
2. 代码示例和最佳实践
3. 性能优化建议
4. Bug解决方案
5. 测试用例编写
6. 代码审查建议

## 期望输出
- 可运行的代码示例
- 详细的实现步骤
- 单元测试代码
- 性能优化方案
- 注意事项说明
```

## 场景化模板

### 1. Clean Architecture 实现

```markdown
# Clean Architecture 功能实现

## 功能需求
- **功能名称**：[如用户故事生成]
- **业务逻辑**：[详细描述]
- **数据来源**：[API/本地数据库]
- **UI交互**：[用户操作流程]

## 需要实现的架构层次

### 1. Domain层实现
#### 数据模型
```kotlin
// 领域模型设计
data class Story(
    val id: String,
    val title: String,
    val content: String,
    val theme: String,
    val ageGroup: AgeGroup,
    val createdAt: Long,
    val questions: List<Question>
)

data class Question(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int
)

enum class AgeGroup {
    AGE_3_4,
    AGE_4_5, 
    AGE_5_6
}
```

#### Repository接口
```kotlin
interface StoryRepository {
    suspend fun generateStory(theme: String, ageGroup: AgeGroup): Result<Story>
    suspend fun getStoryHistory(): Flow<List<Story>>
    suspend fun saveStory(story: Story)
    suspend fun deleteStory(storyId: String)
}
```

#### UseCase实现
```kotlin
class GenerateStoryUseCase @Inject constructor(
    private val repository: StoryRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(theme: String): Result<Story> {
        return try {
            val user = userRepository.getCurrentUser()
            val story = repository.generateStory(theme, user.ageGroup)
            story.onSuccess { 
                repository.saveStory(it) 
            }
            story
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 2. Data层实现
#### API定义
```kotlin
interface StoryApiService {
    @POST("api/stories/generate")
    suspend fun generateStory(
        @Body request: GenerateStoryRequest
    ): StoryResponse
}

data class GenerateStoryRequest(
    val theme: String,
    val ageGroup: String,
    val language: String = "zh-CN"
)
```

#### Repository实现
```kotlin
class StoryRepositoryImpl @Inject constructor(
    private val apiService: StoryApiService,
    private val storyDao: StoryDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StoryRepository {
    
    override suspend fun generateStory(
        theme: String, 
        ageGroup: AgeGroup
    ): Result<Story> = withContext(dispatcher) {
        try {
            val response = apiService.generateStory(
                GenerateStoryRequest(theme, ageGroup.name)
            )
            Result.success(response.toDomainModel())
        } catch (e: Exception) {
            // 降级到本地缓存
            val cachedStory = storyDao.getRandomStory(theme, ageGroup.name)
            if (cachedStory != null) {
                Result.success(cachedStory.toDomainModel())
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getStoryHistory(): Flow<List<Story>> {
        return storyDao.getAllStories()
            .map { entities -> entities.map { it.toDomainModel() } }
    }
}
```

### 3. Presentation层实现
#### ViewModel
```kotlin
@HiltViewModel
class StoryViewModel @Inject constructor(
    private val generateStoryUseCase: GenerateStoryUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StoryUiState())
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()
    
    fun generateStory(theme: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            generateStoryUseCase(theme)
                .onSuccess { story ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            story = story,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }
}

data class StoryUiState(
    val isLoading: Boolean = false,
    val story: Story? = null,
    val error: String? = null
)
```

#### Compose UI
```kotlin
@Composable
fun StoryScreen(
    viewModel: StoryViewModel = hiltViewModel(),
    onNavigateToQuestions: (Story) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 主题输入
        var theme by remember { mutableStateOf("") }
        
        OutlinedTextField(
            value = theme,
            onValueChange = { theme = it },
            label = { Text("故事主题") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 生成按钮
        Button(
            onClick = { viewModel.generateStory(theme) },
            enabled = theme.isNotBlank() && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("生成故事")
            }
        }
        
        // 故事内容显示
        uiState.story?.let { story ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = story.title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = story.content,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    if (story.questions.isNotEmpty()) {
                        TextButton(
                            onClick = { onNavigateToQuestions(story) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("开始答题")
                        }
                    }
                }
            }
        }
        
        // 错误提示
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
```

### 4. 依赖注入配置
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    abstract fun bindStoryRepository(
        storyRepositoryImpl: StoryRepositoryImpl
    ): StoryRepository
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideStoryApiService(retrofit: Retrofit): StoryApiService {
        return retrofit.create(StoryApiService::class.java)
    }
}
```

请帮我完善这个功能的实现细节。
```

### 2. 性能优化实现

```markdown
# Android性能优化实现

## 性能问题描述
- **问题类型**：[启动慢/卡顿/内存泄漏/耗电]
- **影响范围**：[具体功能或页面]
- **性能指标**：[当前值 vs 目标值]
- **用户影响**：[体验问题描述]

## 需要优化的方面

### 1. 启动性能优化
#### 问题分析
- 使用Systrace分析启动流程
- 识别耗时操作
- 关键路径确定

#### 优化方案
```kotlin
// 1. 延迟初始化
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 只初始化必要的组件
        initCriticalComponents()
        
        // 延迟初始化非关键组件
        GlobalScope.launch(Dispatchers.Default) {
            delay(5000) // 等待启动完成
            initNonCriticalComponents()
        }
    }
    
    private fun initCriticalComponents() {
        // 崩溃收集
        CrashHandler.init(this)
        // 路由
        Router.init(this)
    }
    
    private suspend fun initNonCriticalComponents() {
        // 数据分析
        Analytics.init(this)
        // 推送服务
        PushService.init(this)
        // 图片加载
        ImageLoader.init(this)
    }
}

// 2. 启动任务调度器
class StartupTaskDispatcher {
    private val tasks = mutableListOf<StartupTask>()
    
    fun addTask(task: StartupTask) = apply {
        tasks.add(task)
    }
    
    suspend fun execute() = coroutineScope {
        // 构建依赖图
        val taskGraph = buildDependencyGraph(tasks)
        
        // 并行执行无依赖的任务
        taskGraph.getRootTasks().map { task ->
            async {
                task.execute()
                // 执行依赖此任务的其他任务
                executeDependentTasks(task, taskGraph)
            }
        }.awaitAll()
    }
}

// 3. 使用 ViewStub 延迟加载
<ViewStub
    android:id="@+id/stub_heavy_view"
    android:layout="@layout/heavy_view_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />

// 需要时才加载
binding.stubHeavyView.setOnInflateListener { _, inflated ->
    // 视图加载完成
}
binding.stubHeavyView.inflate()
```

### 2. 列表性能优化
```kotlin
// 1. 使用 DiffUtil
class StoryDiffCallback(
    private val oldList: List<Story>,
    private val newList: List<Story>
) : DiffUtil.Callback() {
    
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    
    override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList[oldPos].id == newList[newPos].id
    }
    
    override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList[oldPos] == newList[newPos]
    }
    
    override fun getChangePayload(oldPos: Int, newPos: Int): Any? {
        val old = oldList[oldPos]
        val new = newList[newPos]
        
        return buildList {
            if (old.title != new.title) add("title")
            if (old.content != new.content) add("content")
        }
    }
}

// 2. RecyclerView 优化
class OptimizedRecyclerView : RecyclerView {
    init {
        // 固定尺寸
        setHasFixedSize(true)
        
        // 预取优化
        layoutManager = LinearLayoutManager(context).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = 5
        }
        
        // 复用池优化
        recycledViewPool.setMaxRecycledViews(VIEW_TYPE_ITEM, 20)
        
        // 绘制优化
        setItemViewCacheSize(20)
        isDrawingCacheEnabled = true
        drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
    }
}

// 3. 图片加载优化
@Composable
fun OptimizedImage(
    url: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .size(Size.ORIGINAL) // 自动计算尺寸
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
```

### 3. 内存优化
```kotlin
// 1. 内存泄漏防护
class SafeViewModel : ViewModel() {
    private val compositeDisposable = CompositeDisposable()
    
    // 使用 viewModelScope 自动管理协程
    fun loadData() {
        viewModelScope.launch {
            // 自动取消
        }
    }
    
    // RxJava 需要手动管理
    fun loadDataRx() {
        repository.getData()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { data ->
                // 处理数据
            }
            .addTo(compositeDisposable)
    }
    
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}

// 2. 大对象优化
class ImageCache {
    // 使用 LruCache 管理内存
    private val memoryCache = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    ) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }
    
    // 软引用缓存
    private val softCache = mutableMapOf<String, SoftReference<Bitmap>>()
    
    fun get(key: String): Bitmap? {
        // 先从强引用缓存获取
        memoryCache.get(key)?.let { return it }
        
        // 再从软引用缓存获取
        softCache[key]?.get()?.let { bitmap ->
            // 提升到强引用缓存
            memoryCache.put(key, bitmap)
            return bitmap
        }
        
        return null
    }
}

// 3. 对象池复用
object ViewHolderPool {
    private val pool = Pools.SimplePool<ViewHolder>(10)
    
    fun acquire(): ViewHolder {
        return pool.acquire() ?: ViewHolder()
    }
    
    fun release(holder: ViewHolder) {
        holder.reset()
        pool.release(holder)
    }
}
```

### 4. 网络优化
```kotlin
// 1. 请求合并
class BatchRequestManager {
    private val pendingRequests = mutableListOf<Request>()
    private val batchJob: Job
    
    init {
        batchJob = GlobalScope.launch {
            while (isActive) {
                delay(100) // 100ms 收集窗口
                if (pendingRequests.isNotEmpty()) {
                    executeBatch(pendingRequests.toList())
                    pendingRequests.clear()
                }
            }
        }
    }
    
    fun addRequest(request: Request) {
        pendingRequests.add(request)
    }
    
    private suspend fun executeBatch(requests: List<Request>) {
        // 合并请求
        val batchRequest = BatchRequest(requests)
        val response = apiService.batchExecute(batchRequest)
        
        // 分发结果
        response.results.forEachIndexed { index, result ->
            requests[index].callback.onResult(result)
        }
    }
}

// 2. 缓存策略
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val cacheSize = 50 * 1024 * 1024L // 50MB
        val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)
        
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(CacheInterceptor())
            .addNetworkInterceptor(NetworkCacheInterceptor())
            .build()
    }
}

class CacheInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        
        // 无网络时使用缓存
        if (!isNetworkAvailable()) {
            request = request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build()
        }
        
        return chain.proceed(request)
    }
}

// 3. 数据压缩
class CompressionInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header("Accept-Encoding", "gzip")
            .build()
            
        return chain.proceed(request)
    }
}
```

请提供具体的优化实现方案。
```

### 3. 复杂功能实现

```markdown
# 复杂功能实现 - 实时协作白板

## 功能需求
- **核心功能**：多人实时协作画板
- **技术要求**：低延迟、冲突处理、离线支持
- **用户规模**：支持10人同时协作
- **性能要求**：延迟<100ms，流畅度60fps

## 技术方案设计

### 1. 架构设计
```kotlin
// 整体架构
sealed class Architecture {
    object Presentation {
        // Canvas绘制层
        // 手势处理层
        // 工具栏UI
    }
    
    object Domain {
        // 绘制命令处理
        // 冲突解决算法
        // 状态同步逻辑
    }
    
    object Data {
        // WebSocket通信
        // 本地存储
        // 命令队列
    }
}
```

### 2. 核心实现

#### 绘制引擎
```kotlin
class DrawingEngine {
    private val paths = mutableListOf<DrawPath>()
    private val redoStack = mutableListOf<DrawPath>()
    
    // 绘制路径数据结构
    data class DrawPath(
        val id: String = UUID.randomUUID().toString(),
        val userId: String,
        val points: MutableList<PointF> = mutableListOf(),
        val paint: Paint,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    // 自定义 View 实现
    class CollaborativeCanvasView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
    ) : View(context, attrs) {
        
        private var currentPath: DrawPath? = null
        private val drawPaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = 5f
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    currentPath = DrawPath(
                        userId = getCurrentUserId(),
                        paint = Paint(drawPaint)
                    ).apply {
                        points.add(PointF(x, y))
                    }
                    paths.add(currentPath!!)
                    
                    // 发送开始绘制命令
                    sendDrawCommand(DrawCommand.Start(currentPath!!))
                }
                
                MotionEvent.ACTION_MOVE -> {
                    currentPath?.points?.add(PointF(x, y))
                    
                    // 批量发送点数据
                    if (currentPath!!.points.size % 5 == 0) {
                        sendDrawCommand(DrawCommand.AddPoints(
                            pathId = currentPath!!.id,
                            points = currentPath!!.points.takeLast(5)
                        ))
                    }
                    
                    invalidate()
                }
                
                MotionEvent.ACTION_UP -> {
                    currentPath?.let {
                        sendDrawCommand(DrawCommand.End(it.id))
                    }
                    currentPath = null
                }
            }
            return true
        }
        
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            
            // 绘制所有路径
            paths.forEach { path ->
                if (path.points.size > 1) {
                    val pathToDraw = Path().apply {
                        moveTo(path.points[0].x, path.points[0].y)
                        for (i in 1 until path.points.size) {
                            lineTo(path.points[i].x, path.points[i].y)
                        }
                    }
                    canvas.drawPath(pathToDraw, path.paint)
                }
            }
        }
    }
}

#### 实时同步
```kotlin
// WebSocket 通信
class CollaborationManager(
    private val webSocketClient: WebSocketClient,
    private val userId: String
) {
    private val commandQueue = Channel<DrawCommand>(Channel.UNLIMITED)
    private val localCommandBuffer = mutableListOf<DrawCommand>()
    
    init {
        // 处理发送队列
        GlobalScope.launch {
            commandQueue.consumeAsFlow()
                .buffer()
                .collect { command ->
                    try {
                        webSocketClient.send(command.toJson())
                        localCommandBuffer.add(command)
                    } catch (e: Exception) {
                        // 离线时缓存命令
                        localCommandBuffer.add(command)
                    }
                }
        }
        
        // 处理接收消息
        webSocketClient.onMessage { message ->
            val command = DrawCommand.fromJson(message)
            if (command.userId != userId) {
                applyRemoteCommand(command)
            }
        }
        
        // 断线重连
        webSocketClient.onDisconnect {
            reconnectWithExponentialBackoff()
        }
    }
    
    // 冲突解决 - 基于时间戳的最后写入者胜出
    private fun resolveConflict(
        local: DrawCommand,
        remote: DrawCommand
    ): DrawCommand {
        return if (local.timestamp > remote.timestamp) local else remote
    }
    
    // 操作转换 (OT) 算法简化版
    private fun transformCommand(
        command: DrawCommand,
        against: List<DrawCommand>
    ): DrawCommand {
        var transformed = command
        against.forEach { other ->
            if (other.timestamp < command.timestamp) {
                transformed = transform(transformed, other)
            }
        }
        return transformed
    }
}

// 命令模式
sealed class DrawCommand {
    abstract val pathId: String
    abstract val userId: String
    abstract val timestamp: Long
    
    data class Start(
        val path: DrawPath
    ) : DrawCommand() {
        override val pathId = path.id
        override val userId = path.userId
        override val timestamp = path.timestamp
    }
    
    data class AddPoints(
        override val pathId: String,
        override val userId: String,
        override val timestamp: Long,
        val points: List<PointF>
    ) : DrawCommand()
    
    data class End(
        override val pathId: String,
        override val userId: String,
        override val timestamp: Long
    ) : DrawCommand()
    
    data class Undo(
        override val userId: String,
        override val timestamp: Long
    ) : DrawCommand() {
        override val pathId = ""
    }
}
```

#### 性能优化
```kotlin
// 1. 渲染优化 - 分层绘制
class OptimizedCanvasView : View {
    private val staticBitmap: Bitmap? = null
    private val staticCanvas: Canvas? = null
    private var needsStaticRedraw = true
    
    override fun onDraw(canvas: Canvas) {
        // 绘制静态层（已完成的路径）
        if (needsStaticRedraw) {
            redrawStaticLayer()
            needsStaticRedraw = false
        }
        staticBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        
        // 只绘制动态层（当前绘制的路径）
        currentPath?.let { drawPath(canvas, it) }
    }
}

// 2. 数据压缩 - 道格拉斯-普克算法
fun simplifyPath(
    points: List<PointF>,
    tolerance: Float = 2.0f
): List<PointF> {
    if (points.size < 3) return points
    
    // 找到最远的点
    var maxDistance = 0f
    var maxIndex = 0
    
    val start = points.first()
    val end = points.last()
    
    for (i in 1 until points.size - 1) {
        val distance = perpendicularDistance(points[i], start, end)
        if (distance > maxDistance) {
            maxDistance = distance
            maxIndex = i
        }
    }
    
    // 递归简化
    return if (maxDistance > tolerance) {
        val left = simplifyPath(points.subList(0, maxIndex + 1), tolerance)
        val right = simplifyPath(points.subList(maxIndex, points.size), tolerance)
        left.dropLast(1) + right
    } else {
        listOf(start, end)
    }
}

// 3. 离线支持
class OfflineCommandStore(context: Context) {
    private val prefs = context.getSharedPreferences("commands", MODE_PRIVATE)
    private val database = Room.databaseBuilder(
        context,
        CommandDatabase::class.java,
        "commands"
    ).build()
    
    suspend fun saveCommand(command: DrawCommand) {
        database.commandDao().insert(command.toEntity())
    }
    
    suspend fun getPendingCommands(): List<DrawCommand> {
        return database.commandDao()
            .getPendingCommands()
            .map { it.toCommand() }
    }
    
    suspend fun markAsSynced(commandIds: List<String>) {
        database.commandDao().markAsSynced(commandIds)
    }
}
```

### 3. 测试实现
```kotlin
// 单元测试
class DrawingEngineTest {
    
    @Test
    fun `path simplification should reduce points`() {
        val points = listOf(
            PointF(0f, 0f),
            PointF(1f, 0.1f),
            PointF(2f, -0.1f),
            PointF(3f, 5f),
            PointF(4f, 6f),
            PointF(5f, 7f)
        )
        
        val simplified = simplifyPath(points, 1.0f)
        
        assertTrue(simplified.size < points.size)
        assertEquals(points.first(), simplified.first())
        assertEquals(points.last(), simplified.last())
    }
}

// UI 测试
class CanvasUITest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun drawing_gesture_creates_path() {
        composeTestRule.setContent {
            CollaborativeCanvas()
        }
        
        // 执行绘制手势
        composeTestRule.onNodeWithTag("canvas")
            .performTouchInput {
                down(center)
                moveBy(Offset(100f, 100f))
                up()
            }
        
        // 验证路径创建
        composeTestRule.onNodeWithTag("path_count")
            .assertTextEquals("1")
    }
}
```

请帮我完善实现细节和优化方案。
```

## 进阶模板

### 1. 代码重构方案

```markdown
# 代码重构实施方案

## 重构背景
- **代码现状**：[技术债务描述]
- **主要问题**：
  - 代码重复率高
  - 模块耦合严重
  - 测试困难
  - 扩展性差
- **重构目标**：[预期改进]

## 重构策略

### 1. 分层重构
#### Before - 混乱的代码结构
```kotlin
class UserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 直接在Activity中处理所有逻辑
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            
            // 网络请求
            Thread {
                try {
                    val url = URL("https://api.example.com/login")
                    val connection = url.openConnection() as HttpURLConnection
                    // ... 省略网络请求代码
                    
                    // 数据库操作
                    val db = openOrCreateDatabase("user.db", MODE_PRIVATE, null)
                    db.execSQL("INSERT INTO users...")
                    
                    runOnUiThread {
                        // UI更新
                        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }
}
```

#### After - Clean Architecture
```kotlin
// Presentation Layer
@AndroidEntryPoint
class UserActivity : AppCompatActivity() {
    private val viewModel: UserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.etUsername.text.toString(),
                binding.etPassword.text.toString()
            )
        }
        
        // 观察状态
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading()
                    is UiState.Success -> navigateToMain()
                    is UiState.Error -> showError(state.message)
                }
            }
        }
    }
}

// ViewModel
@HiltViewModel
class UserViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            loginUseCase(username, password)
                .onSuccess { user ->
                    _uiState.value = UiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "登录失败")
                }
        }
    }
}

// Domain Layer
class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String
    ): Result<User> {
        // 业务规则验证
        if (username.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("用户名或密码无效"))
        }
        
        return userRepository.login(username, password)
    }
}

// Data Layer
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {
    
    override suspend fun login(
        username: String,
        password: String
    ): Result<User> = withContext(dispatcher) {
        try {
            val response = apiService.login(LoginRequest(username, password))
            val user = response.toUser()
            
            // 保存到本地
            userDao.insertUser(user.toEntity())
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 2. 模块化重构
```kotlin
// 模块化结构
/*
app/
feature-auth/
  ├── src/main/java/com/example/auth/
  │   ├── presentation/
  │   ├── domain/
  │   └── data/
  └── build.gradle.kts

feature-home/
  ├── src/main/java/com/example/home/
  └── build.gradle.kts

core-common/
  ├── src/main/java/com/example/common/
  └── build.gradle.kts
*/

// 模块间通信 - 使用接口解耦
// core-common 模块
interface AuthNavigator {
    fun navigateToLogin()
    fun navigateToRegister()
}

interface AuthService {
    suspend fun isLoggedIn(): Boolean
    suspend fun getCurrentUser(): User?
    suspend fun logout()
}

// feature-auth 模块实现
@Singleton
class AuthServiceImpl @Inject constructor(
    private val userRepository: UserRepository
) : AuthService {
    override suspend fun isLoggedIn(): Boolean {
        return userRepository.getCurrentUser() != null
    }
}

// app 模块注入
@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {
    
    @Binds
    abstract fun bindAuthNavigator(
        impl: AuthNavigatorImpl
    ): AuthNavigator
}
```

### 3. 测试改进
```kotlin
// Before - 难以测试的代码
class DataManager(context: Context) {
    private val prefs = context.getSharedPreferences("data", MODE_PRIVATE)
    private val database = DatabaseHelper(context)
    
    fun saveUserData(user: User) {
        prefs.edit().putString("user_id", user.id).apply()
        database.insertUser(user)
        Analytics.track("user_saved")
    }
}

// After - 可测试的代码
class DataManager @Inject constructor(
    private val prefsManager: PreferencesManager,
    private val userDao: UserDao,
    private val analytics: Analytics
) {
    suspend fun saveUserData(user: User) {
        prefsManager.saveUserId(user.id)
        userDao.insert(user)
        analytics.track(AnalyticsEvent.UserSaved(user.id))
    }
}

// 测试
class DataManagerTest {
    @Mock lateinit var prefsManager: PreferencesManager
    @Mock lateinit var userDao: UserDao
    @Mock lateinit var analytics: Analytics
    
    private lateinit var dataManager: DataManager
    
    @Before
    fun setup() {
        dataManager = DataManager(prefsManager, userDao, analytics)
    }
    
    @Test
    fun `saveUserData should save to all sources`() = runTest {
        val user = User("123", "Test User")
        
        dataManager.saveUserData(user)
        
        verify(prefsManager).saveUserId("123")
        verify(userDao).insert(user)
        verify(analytics).track(AnalyticsEvent.UserSaved("123"))
    }
}
```

请提供具体的重构实施计划。
```

### 2. 技术难题解决

```markdown
# 技术难题解决方案

## 问题描述
- **问题现象**：[具体表现]
- **影响范围**：[受影响的功能]
- **复现步骤**：[如何复现]
- **已尝试方案**：[已经尝试过的解决方法]

## 需要解决的技术难题

### 1. 内存泄漏问题
```kotlin
// 问题代码
class LeakyActivity : AppCompatActivity() {
    companion object {
        // 静态引用导致泄漏
        var instance: LeakyActivity? = null
    }
    
    private val handler = Handler(Looper.getMainLooper())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        
        // Handler 导致泄漏
        handler.postDelayed({
            updateUI()
        }, 30000)
        
        // 匿名内部类导致泄漏
        NetworkManager.getInstance().request(object : Callback {
            override fun onSuccess(data: String) {
                textView.text = data
            }
        })
    }
}

// 解决方案
class FixedActivity : AppCompatActivity() {
    // 使用 WeakReference
    companion object {
        private var instanceRef: WeakReference<FixedActivity>? = null
        
        fun getInstance(): FixedActivity? = instanceRef?.get()
    }
    
    // 使用 Lifecycle 感知的协程
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instanceRef = WeakReference(this)
        
        // 自动取消的协程
        lifecycleScope.launch {
            delay(30000)
            updateUI()
        }
        
        // 使用 lifecycleScope
        lifecycleScope.launch {
            try {
                val data = NetworkManager.getInstance().requestSuspend()
                textView.text = data
            } catch (e: Exception) {
                // 错误处理
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instanceRef = null
    }
}

// 使用 LeakCanary 检测
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            // LeakCanary 自动初始化
        }
    }
}
```

### 2. ANR 问题解决
```kotlin
// 问题：主线程执行耗时操作
class ANRActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 导致 ANR 的代码
        button.setOnClickListener {
            // 主线程读取大文件
            val content = File("/sdcard/large_file.txt").readText()
            
            // 主线程数据库查询
            val cursor = database.rawQuery("SELECT * FROM large_table", null)
            while (cursor.moveToNext()) {
                // 处理数据
            }
        }
    }
}

// 解决方案
class OptimizedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        button.setOnClickListener {
            // 使用协程处理耗时操作
            lifecycleScope.launch {
                val content = withContext(Dispatchers.IO) {
                    File("/sdcard/large_file.txt").readText()
                }
                
                // 更新 UI
                textView.text = content
            }
            
            // 使用 Room 的异步查询
            viewModel.largeDateLiveData.observe(this) { data ->
                // 处理数据
            }
        }
    }
}

// 监控和预防
class ANRWatchdog : Thread {
    private val handler = Handler(Looper.getMainLooper())
    private var tick = 0
    
    override fun run() {
        while (!isInterrupted) {
            val lastTick = tick
            handler.post { tick++ }
            
            Thread.sleep(5000) // 5秒检查一次
            
            if (tick == lastTick) {
                // 主线程被阻塞
                ANRError().printStackTrace()
                // 上报错误
            }
        }
    }
}
```

### 3. 复杂的自定义View
```kotlin
// 复杂的图表View实现
class ComplexChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    // 性能优化：避免在 onDraw 中创建对象
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(12f)
    }
    
    // 数据
    private var dataPoints = listOf<DataPoint>()
    private var animator: ValueAnimator? = null
    
    // 缓存计算结果
    private var calculatedPoints = listOf<PointF>()
    private var needsRecalculation = true
    
    fun setData(points: List<DataPoint>) {
        dataPoints = points
        needsRecalculation = true
        animateChart()
    }
    
    private fun animateChart() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                animationProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        needsRecalculation = true
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (needsRecalculation) {
            calculatePoints()
            needsRecalculation = false
        }
        
        // 绘制背景网格
        drawGrid(canvas)
        
        // 绘制数据线
        drawDataLine(canvas)
        
        // 绘制数据点
        drawDataPoints(canvas)
        
        // 绘制标签
        drawLabels(canvas)
    }
    
    private fun calculatePoints() {
        if (dataPoints.isEmpty()) return
        
        val width = width - paddingLeft - paddingRight
        val height = height - paddingTop - paddingBottom
        
        val xStep = width.toFloat() / (dataPoints.size - 1)
        val maxValue = dataPoints.maxOf { it.value }
        val minValue = dataPoints.minOf { it.value }
        val valueRange = maxValue - minValue
        
        calculatedPoints = dataPoints.mapIndexed { index, point ->
            val x = paddingLeft + index * xStep
            val y = if (valueRange > 0) {
                paddingTop + height * (1 - (point.value - minValue) / valueRange)
            } else {
                paddingTop + height / 2f
            }
            PointF(x, y * animationProgress + height * (1 - animationProgress))
        }
    }
    
    // 使用硬件加速
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }
}

// 数据类
data class DataPoint(
    val label: String,
    val value: Float,
    val color: Int = Color.BLUE
)
```

请提供解决方案和最佳实践。
```

### 3. CI/CD 集成

```markdown
# Android CI/CD 集成实现

## 项目需求
- **代码管理**：GitHub/GitLab
- **构建要求**：自动化构建、测试、发布
- **环境管理**：开发、测试、生产环境
- **发布渠道**：Google Play、应用宝等

## CI/CD 实现方案

### 1. GitHub Actions 配置
```yaml
# .github/workflows/android.yml
name: Android CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  GRADLE_OPTS: "-Dorg.gradle.jvmargs=-Xmx4g -Dorg.gradle.daemon=false"

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Run unit tests
      run: ./gradlew test
    
    - name: Upload test results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: test-results
        path: '**/build/reports/tests/'
    
    - name: Run lint
      run: ./gradlew lint
    
    - name: Upload lint results
      uses: actions/upload-artifact@v3
      with:
        name: lint-results
        path: '**/build/reports/lint-results-*.html'

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Decode keystore
      env:
        ENCODED_KEYSTORE: ${{ secrets.KEYSTORE_BASE64 }}
      run: |
        echo $ENCODED_KEYSTORE | base64 -d > keystore.jks
    
    - name: Build release APK
      env:
        KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      run: |
        ./gradlew assembleRelease \
          -Pandroid.injected.signing.store.file=keystore.jks \
          -Pandroid.injected.signing.store.password=$KEYSTORE_PASSWORD \
          -Pandroid.injected.signing.key.alias=$KEY_ALIAS \
          -Pandroid.injected.signing.key.password=$KEY_PASSWORD
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-release
        path: app/build/outputs/apk/release/app-release.apk
    
    - name: Create Release
      if: github.ref == 'refs/heads/main'
      uses: actions/create-release@v1
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
      with:
        tag_name: v${{ github.run_number }}
        release_name: Release ${{ github.run_number }}
        draft: false
        prerelease: false

  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
    - uses: actions/checkout@v3
    
    - name: Download APK
      uses: actions/download-artifact@v3
      with:
        name: app-release
    
    - name: Upload to Google Play
      uses: r0adkll/upload-google-play@v1
      with:
        serviceAccountJsonPlainText: ${{ secrets.PLAY_SERVICE_ACCOUNT_JSON }}
        packageName: com.example.app
        releaseFiles: app-release.apk
        track: internal
        status: draft
```

### 2. Fastlane 集成
```ruby
# fastlane/Fastfile
default_platform(:android)

platform :android do
  desc "Runs all the tests"
  lane :test do
    gradle(task: "test")
  end

  desc "Build a debug APK"
  lane :debug do
    gradle(
      task: "clean assembleDebug",
      print_command: false
    )
  end

  desc "Build a release APK and upload to Play Store"
  lane :release do
    # 确保 git 状态干净
    ensure_git_status_clean
    
    # 增加版本号
    gradle(task: "incrementVersionCode")
    
    # 构建 Release APK
    gradle(
      task: "clean assembleRelease",
      properties: {
        "android.injected.signing.store.file" => ENV["KEYSTORE_FILE"],
        "android.injected.signing.store.password" => ENV["KEYSTORE_PASSWORD"],
        "android.injected.signing.key.alias" => ENV["KEY_ALIAS"],
        "android.injected.signing.key.password" => ENV["KEY_PASSWORD"]
      }
    )
    
    # 上传到 Play Store
    upload_to_play_store(
      track: 'internal',
      release_status: 'draft',
      skip_upload_metadata: true,
      skip_upload_images: true,
      skip_upload_screenshots: true
    )
    
    # 提交版本变更
    git_commit(
      path: ["app/build.gradle"],
      message: "Version Bump"
    )
    
    # 添加 Git 标签
    add_git_tag(
      tag: "v#{get_version_name}"
    )
    
    # 推送到远程
    push_to_git_remote
  end

  desc "Deploy a new beta version to Firebase App Distribution"
  lane :beta do
    gradle(task: "clean assembleRelease")
    
    firebase_app_distribution(
      app: ENV["FIREBASE_APP_ID"],
      testers: ENV["FIREBASE_TESTERS"],
      release_notes: "Beta release",
      firebase_cli_path: "/usr/local/bin/firebase",
      apk_path: "app/build/outputs/apk/release/app-release.apk"
    )
    
    # 发送通知
    slack(
      message: "Beta 版本已发布到 Firebase App Distribution",
      success: true,
      slack_url: ENV["SLACK_WEBHOOK"]
    )
  end
end

# fastlane/Appfile
json_key_file("path/to/play-service-account.json")
package_name("com.example.app")
```

### 3. 构建脚本优化
```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        versionCode = getVersionCode()
        versionName = getVersionName()
    }
    
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        
        create("staging") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
        }
    }
    
    // 多渠道打包
    flavorDimensions.add("channel")
    productFlavors {
        create("googleplay") {
            dimension = "channel"
            manifestPlaceholders["CHANNEL_NAME"] = "googleplay"
        }
        create("huawei") {
            dimension = "channel"
            manifestPlaceholders["CHANNEL_NAME"] = "huawei"
        }
    }
}

// 版本管理
fun getVersionCode(): Int {
    val code = project.properties["VERSION_CODE"]?.toString()?.toInt() ?: 1
    return if (System.getenv("CI") == "true") {
        code + (System.getenv("GITHUB_RUN_NUMBER")?.toInt() ?: 0)
    } else {
        code
    }
}

fun getVersionName(): String {
    return project.properties["VERSION_NAME"]?.toString() ?: "1.0.0"
}

// 自动增加版本号
tasks.register("incrementVersionCode") {
    doLast {
        val propertiesFile = file("gradle.properties")
        val properties = Properties().apply {
            load(propertiesFile.inputStream())
        }
        
        val oldCode = properties["VERSION_CODE"]?.toString()?.toInt() ?: 1
        properties["VERSION_CODE"] = (oldCode + 1).toString()
        
        properties.store(propertiesFile.outputStream(), null)
    }
}
```

### 4. 代码质量检查
```kotlin
// quality.gradle.kts
apply(plugin = "checkstyle")
apply(plugin = "pmd")

checkstyle {
    toolVersion = "10.12.0"
    configFile = file("${project.rootDir}/config/checkstyle/checkstyle.xml")
}

pmd {
    toolVersion = "6.55.0"
    ruleSets = listOf(
        "category/java/bestpractices.xml",
        "category/java/errorprone.xml"
    )
}

tasks.register("codeQuality") {
    dependsOn("checkstyle", "pmd", "lint", "detekt")
    
    doLast {
        println("Code quality checks completed")
    }
}

// SonarQube 集成
sonarqube {
    properties {
        property("sonar.projectKey", "android-app")
        property("sonar.organization", "my-org")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.login", System.getenv("SONAR_TOKEN"))
    }
}
```

请提供完整的CI/CD实施方案。
```

## 最佳实践总结

### 1. 代码质量
- **遵循规范**：Kotlin编码规范、命名约定
- **代码复用**：抽取公共组件、工具类
- **注释完善**：关键逻辑必须有中文注释
- **错误处理**：全面的异常捕获和处理

### 2. 架构设计
- **分层清晰**：严格遵循Clean Architecture
- **依赖注入**：使用Hilt管理依赖
- **模块化**：功能模块独立，降低耦合
- **可测试性**：设计时考虑测试

### 3. 性能优化
- **启动优化**：延迟加载、并行初始化
- **内存管理**：避免内存泄漏、对象复用
- **UI流畅**：60fps、避免主线程阻塞
- **网络优化**：缓存、压缩、批量请求

### 4. 团队协作
- **代码评审**：每个PR必须review
- **文档完善**：README、API文档、架构图
- **知识分享**：定期技术分享、最佳实践
- **持续改进**：复盘、优化、创新

## 学习资源

### 1. 官方文档
- [Android Developers](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

### 2. 社区资源
- [Android Weekly](https://androidweekly.net/)
- [Kotlin Weekly](http://kotlinweekly.net/)
- [ProAndroidDev](https://proandroiddev.com/)

### 3. 开源项目
- [architecture-samples](https://github.com/android/architecture-samples)
- [compose-samples](https://github.com/android/compose-samples)
- [Now in Android](https://github.com/android/nowinandroid)

### 4. 工具推荐
- **开发工具**：Android Studio、VS Code
- **调试工具**：Stetho、Flipper、Chuck
- **性能工具**：LeakCanary、BlockCanary
- **网络工具**：Charles、Postman

---

**提示**：
- 始终关注用户体验
- 代码是写给人看的
- 测试是质量的保证
- 学习是持续的过程