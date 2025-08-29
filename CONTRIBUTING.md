# 贡献指南

感谢您对AI启蒙时光项目的关注！我们欢迎所有形式的贡献。

## 📋 贡献方式

### 1. 报告问题
- 使用GitHub Issues报告bug
- 提供详细的复现步骤
- 包含设备信息和日志

### 2. 功能建议
- 在Issues中提出新功能想法
- 说明使用场景和价值
- 参与功能讨论

### 3. 代码贡献
- Fork项目
- 创建功能分支
- 提交Pull Request

## 🚀 开发流程

### 1. 环境准备

```bash
# 克隆项目
git clone https://github.com/your-org/ai-enlightenment.git
cd ai-enlightenment

# 创建分支
git checkout -b feature/your-feature-name
```

### 2. 开发规范

#### 代码风格
- 遵循Kotlin官方代码规范
- 使用有意义的变量名
- 添加必要的注释

#### 提交规范
```
<type>(<scope>): <subject>

<body>

<footer>
```

类型(type):
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式
- refactor: 重构
- test: 测试
- chore: 构建/辅助工具

示例：
```
feat(story): 添加故事收藏功能

- 用户可以收藏喜欢的故事
- 收藏列表支持离线访问
- 添加收藏动画效果

Closes #123
```

### 3. 测试要求

- 新功能必须包含单元测试
- 保持测试覆盖率80%以上
- 运行所有测试确保通过

```bash
# 运行单元测试
./gradlew test

# 运行UI测试
./gradlew connectedAndroidTest
```

### 4. Pull Request

#### PR检查清单
- [ ] 代码符合规范
- [ ] 添加了测试
- [ ] 更新了文档
- [ ] 本地测试通过
- [ ] 解决了所有冲突

#### PR描述模板
```markdown
## 变更说明
简要说明此PR的目的

## 变更类型
- [ ] Bug修复
- [ ] 新功能
- [ ] 性能优化
- [ ] 代码重构

## 测试
- [ ] 单元测试
- [ ] UI测试
- [ ] 手动测试

## 截图（如适用）
添加UI变更的截图
```

## 📁 项目结构

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/enlightenment/ai/
│   │   │   ├── data/        # 数据层
│   │   │   ├── domain/      # 领域层
│   │   │   ├── presentation/# 表现层
│   │   │   └── di/          # 依赖注入
│   │   └── res/             # 资源文件
│   ├── test/                # 单元测试
│   └── androidTest/         # UI测试
└── build.gradle.kts         # 构建配置
```

## 🛠️ 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **架构**: Clean Architecture + MVVM
- **依赖注入**: Hilt
- **网络**: Retrofit + OkHttp
- **数据库**: Room
- **异步**: Coroutines + Flow

## 📝 代码示例

### ViewModel示例
```kotlin
@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val useCase: ExampleUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExampleUiState())
    val uiState: StateFlow<ExampleUiState> = _uiState.asStateFlow()
    
    fun performAction() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            useCase.execute()
                .onSuccess { data ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            data = data
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
```

### Composable示例
```kotlin
@Composable
fun ExampleScreen(
    viewModel: ExampleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            uiState.error != null -> {
                ErrorMessage(uiState.error)
            }
            else -> {
                Content(uiState.data)
            }
        }
    }
}
```

## 🤝 行为准则

- 尊重所有贡献者
- 建设性的批评和讨论
- 关注项目目标
- 帮助新贡献者

## 📮 联系方式

- 项目维护者: [@maintainer](https://github.com/maintainer)
- 邮件: ai-enlightenment@example.com
- 讨论区: [GitHub Discussions](https://github.com/your-org/ai-enlightenment/discussions)

## 📄 许可证

通过贡献代码，您同意您的贡献将按照项目的MIT许可证进行授权。

---

感谢您的贡献！让我们一起为孩子们创造更好的AI教育体验！ 🌟