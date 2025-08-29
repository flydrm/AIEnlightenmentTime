# 代码质量验证SOP

## 目的
确保代码质量达到生产标准，无技术债务，可维护性高。

## 验证清单

### 1. 基础代码质量

#### 1.1 TODO/FIXME检查
```bash
# 命令
grep -r "TODO\|FIXME\|XXX\|HACK" app/src/main --include="*.kt"

# 标准
- 生产代码中不应有TODO标记
- 测试代码中的TODO需有明确计划
```

#### 1.2 空实现检查
```bash
# 搜索空函数体
grep -r "fun.*{[\s]*}" app/src/main --include="*.kt"

# 注意事项
- 接口方法不需要实现
- 某些生命周期方法可以为空（如onCleared）
```

#### 1.3 Mock/Stub检查
```bash
# 搜索模拟代码
grep -r "mock\|stub\|fake\|dummy" app/src/main --include="*.kt" -i

# 例外
- MockK等测试框架的导入
- 数据类中的fake前缀
```

### 2. 错误处理验证

#### 2.1 网络请求错误处理
```kotlin
// ✅ 良好示例
suspend fun fetchData(): Result<Data> {
    return try {
        val response = api.getData()
        Result.success(response)
    } catch (e: IOException) {
        // 网络错误
        Result.failure(e)
    } catch (e: HttpException) {
        // HTTP错误
        Result.failure(e)
    }
}

// ❌ 错误示例
suspend fun fetchData(): Data {
    return api.getData() // 无错误处理
}
```

#### 2.2 协程异常处理
```kotlin
// ✅ 使用SupervisorJob避免异常传播
private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

// ✅ 使用CoroutineExceptionHandler
private val errorHandler = CoroutineExceptionHandler { _, exception ->
    Log.e(TAG, "Coroutine exception", exception)
}
```

### 3. 资源管理

#### 3.1 内存泄漏预防
```kotlin
// ViewModel中清理资源
override fun onCleared() {
    super.onCleared()
    // 取消所有协程
    viewModelScope.cancel()
    // 清理其他资源
    disposables.clear()
}
```

#### 3.2 文件操作
```kotlin
// 使用use扩展自动关闭资源
file.inputStream().use { stream ->
    // 处理流
}
```

### 4. 代码规范

#### 4.1 命名规范
- 类名：PascalCase
- 函数名：camelCase
- 常量：UPPER_SNAKE_CASE
- 包名：lowercase

#### 4.2 代码格式
```bash
# 使用ktlint检查格式
./gradlew ktlintCheck

# 自动修复格式问题
./gradlew ktlintFormat
```

### 5. 架构规范

#### 5.1 Clean Architecture层级
```
domain/  # 不依赖Android框架
├── model/      # 纯数据类
├── repository/ # 接口定义
└── usecase/    # 业务逻辑

data/    # 实现细节
├── local/      # 本地存储
├── remote/     # 网络请求
└── repository/ # 接口实现

presentation/   # UI层
├── screen/     # Composable
├── viewmodel/  # 状态管理
└── theme/      # UI主题
```

#### 5.2 依赖方向
- presentation → domain ← data
- 不允许反向依赖

### 6. 注释规范 ⭐ 重要

#### 6.1 加权验证体系
- **核心类高权重**：Domain层UseCase（1.5）、Repository接口（1.5）
- **测试类低权重**：所有测试类（0.5）
- **其他类标准权重**：工具类、常量类（1.0）

#### 6.2 分层中文注释要求
| 层级 | 中文注释比例要求 | 说明 |
|------|------------------|------|
| Domain | ≥80% | 业务逻辑必须中文 |
| Presentation | ≥70% | UI交互用中文说明 |
| Data | ≥60% | 数据策略用中文 |
| Test | ≥40% | 测试意图用中文 |

#### 6.3 强制注释场景
1. **所有类/接口**：必须有功能说明
2. **复杂方法**：行数>20或控制流>3
3. **UI交互**：点击、手势、状态变化
4. **关键算法**：评分计算、推荐逻辑

#### 6.4 专用验证工具
```bash
# 运行加权注释质量检查
python3 docs/sop/validate/scripts/weighted-comment-check.py

# 目标：总分≥95分
# A+ (95-100): 可发布
# A  (90-94): 优秀
# B  (85-89): 良好
# <85: 需改进
```

## 验证脚本

### 完整质量检查脚本
```bash
#!/bin/bash
# quality-check.sh

echo "🔍 代码质量检查"

# 1. TODO检查
echo -n "检查TODO标记... "
if grep -r "TODO\|FIXME" app/src/main --include="*.kt" -q; then
    echo "❌ 发现TODO"
    exit 1
else
    echo "✅ 通过"
fi

# 2. 空实现检查
echo -n "检查空实现... "
if grep -r "fun.*{[\s]*}" app/src/main --include="*.kt" | grep -v "interface" -q; then
    echo "❌ 发现空实现"
    exit 1
else
    echo "✅ 通过"
fi

# 3. 硬编码检查
echo -n "检查硬编码值... "
if grep -r "127.0.0.1\|localhost" app/src/main --include="*.kt" -q; then
    echo "❌ 发现硬编码"
    exit 1
else
    echo "✅ 通过"
fi

echo "✅ 代码质量检查通过！"
```

## 常见问题

### Q1: 接口被报告为空实现
**A**: 接口方法不需要实现体，验证脚本应排除interface文件。

### Q2: 生命周期方法为空
**A**: onCreate、onCleared等可以为空，这是正常的。

### Q3: 数据类的equals/hashCode
**A**: Kotlin自动生成，不需要手动实现。

## 检查频率

- **每次提交**：运行基础检查
- **每日构建**：运行完整检查
- **发布前**：运行深度检查

---

*SOP版本：1.0*  
*适用于Kotlin + Android项目*