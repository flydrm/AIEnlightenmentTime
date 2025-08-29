# AI启蒙时光 - 架构设计文档

## 1. 架构概述

AI启蒙时光采用 **Clean Architecture + MVVM** 架构模式，确保代码的可维护性、可测试性和可扩展性。

```
┌─────────────────────────────────────────────────┐
│                 Presentation Layer               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────┐ │
│  │   Screens   │  │ ViewModels  │  │  Theme  │ │
│  │  (Compose)  │  │   (State)   │  │         │ │
│  └─────────────┘  └─────────────┘  └─────────┘ │
└─────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────┐
│                  Domain Layer                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────┐ │
│  │   Models    │  │  Use Cases  │  │  Repos  │ │
│  │  (Entities) │  │   (Logic)   │  │ (Intf)  │ │
│  └─────────────┘  └─────────────┘  └─────────┘ │
└─────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────┐
│                   Data Layer                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────┐ │
│  │   Remote    │  │    Local    │  │  Repos  │ │
│  │  (Network)  │  │ (Database)  │  │ (Impl)  │ │
│  └─────────────┘  └─────────────┘  └─────────┘ │
└─────────────────────────────────────────────────┘
```

## 2. 技术栈

### 核心框架
- **Kotlin**: 编程语言
- **Jetpack Compose**: UI框架
- **Hilt**: 依赖注入
- **Coroutines + Flow**: 异步编程

### 网络层
- **Retrofit**: HTTP客户端
- **OkHttp**: 网络库
- **Gson**: JSON解析

### 本地存储
- **Room**: 数据库
- **DataStore**: 键值存储

### AI集成
- **GEMINI-2.5-PRO**: 主AI模型
- **GPT-5-PRO**: 备用AI模型
- **Qwen3-Embedding-8B**: 嵌入模型
- **BAAI/bge-reranker-v2-m3**: 重排模型
- **grok-4-imageGen**: 图像生成

## 3. 模块设计

### 3.1 Presentation Layer（表现层）

负责UI展示和用户交互：

```kotlin
// Screen示例
@Composable
fun StoryScreen(
    viewModel: StoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // UI实现
}

// ViewModel示例
@HiltViewModel
class StoryViewModel @Inject constructor(
    private val generateStoryUseCase: GenerateStoryUseCase
) : ViewModel() {
    // 状态管理
}
```

### 3.2 Domain Layer（领域层）

包含业务逻辑，独立于任何框架：

```kotlin
// UseCase示例
class GenerateStoryUseCase @Inject constructor(
    private val repository: StoryRepository
) {
    suspend operator fun invoke(topic: String): Result<Story>
}

// Repository接口
interface StoryRepository {
    suspend fun generateStory(topic: String): Result<Story>
}
```

### 3.3 Data Layer（数据层）

负责数据获取和存储：

```kotlin
// Repository实现
class StoryRepositoryImpl @Inject constructor(
    private val api: AIApiService,
    private val dao: StoryDao
) : StoryRepository {
    // 实现数据获取逻辑
}
```

## 4. 依赖注入设计

使用Hilt进行依赖管理：

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindStoryRepository(
        impl: StoryRepositoryImpl
    ): StoryRepository
}
```

## 5. 数据流

1. **用户操作** → Screen
2. **事件处理** → ViewModel
3. **业务逻辑** → UseCase
4. **数据请求** → Repository
5. **网络/本地** → DataSource
6. **状态更新** → StateFlow
7. **UI更新** → Compose Recomposition

## 6. 错误处理策略

### 6.1 网络错误
- 主模型失败 → 尝试备用模型
- 备用模型失败 → 使用本地缓存
- 无缓存 → 显示友好提示

### 6.2 降级策略
```kotlin
sealed class ModelPriority {
    object Primary : ModelPriority()   // GEMINI-2.5-PRO
    object Fallback : ModelPriority()  // GPT-5-PRO
    object Cache : ModelPriority()     // 本地缓存
}
```

## 7. 性能优化

### 7.1 UI性能
- 使用`LazyColumn`优化列表
- `remember`和`derivedStateOf`减少重组
- 图片使用Coil异步加载

### 7.2 网络优化
- OkHttp缓存策略
- 预加载常用数据
- 请求合并和批处理

### 7.3 内存优化
- ViewModel正确清理
- 图片缓存大小限制
- 数据库查询优化

## 8. 安全设计

### 8.1 API密钥管理
- 使用BuildConfig存储
- 从gradle.properties读取
- 不硬编码在代码中

### 8.2 数据加密
- 敏感数据使用EncryptedSharedPreferences
- 网络传输使用HTTPS
- 本地存储加密

### 8.3 权限管理
- 最小权限原则
- 运行时权限请求
- 权限使用说明

## 9. 测试策略

### 9.1 单元测试
- UseCase逻辑测试
- ViewModel状态测试
- Repository数据测试

### 9.2 UI测试
- Compose UI测试
- 用户流程测试
- 屏幕截图测试

### 9.3 集成测试
- API集成测试
- 数据库集成测试
- 端到端测试

## 10. 部署架构

### 10.1 构建配置
- Debug/Release变体
- ProGuard混淆
- 资源优化

### 10.2 CI/CD
- GitHub Actions自动构建
- 自动化测试
- 版本管理

## 11. 未来扩展

### 11.1 功能扩展点
- 新增AI模型支持
- 多语言支持
- 离线模式增强

### 11.2 技术升级路径
- Compose Multiplatform
- Kotlin Multiplatform
- 服务端渲染

---

*架构设计文档 - 2024年12月*