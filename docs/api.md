# AI启蒙时光 - API文档

## 1. API概述

本文档描述了AI启蒙时光应用的API接口规范。

### 基础信息
- **Base URL**: `https://ai-edu-api.cloud.tencent.com/v1/`
- **协议**: HTTPS
- **数据格式**: JSON
- **认证方式**: Bearer Token

## 2. 认证

所有API请求需要在Header中包含认证信息：

```http
Authorization: Bearer {API_KEY}
```

支持的API Keys:
- `GEMINI_API_KEY`: 用于GEMINI模型
- `GPT_API_KEY`: 用于GPT模型

## 3. API接口

### 3.1 故事生成

生成个性化的儿童故事。

**接口**: `POST /story/generate`

**请求头**:
```http
Content-Type: application/json
Authorization: Bearer {GEMINI_API_KEY}
```

**请求体**:
```json
{
  "topic": "恐龙",
  "childAge": 4,
  "duration": 5,
  "educationalFocus": ["认知", "想象力"]
}
```

**响应**:
```json
{
  "id": "story_123",
  "title": "小恐龙的冒险",
  "content": "从前，有一只小恐龙...",
  "imageUrl": "https://example.com/story_123.jpg",
  "duration": 5,
  "questions": [
    {
      "id": "q1",
      "text": "小恐龙最喜欢吃什么？",
      "options": ["树叶", "肉", "水果", "草"],
      "correctAnswerIndex": 0,
      "explanation": "小恐龙是食草恐龙，最喜欢吃树叶。"
    }
  ]
}
```

### 3.2 智能对话

与AI进行对话交互。

**接口**: `POST /dialogue/chat`

**请求头**:
```http
Content-Type: application/json
Authorization: Bearer {GPT_API_KEY}
```

**请求体**:
```json
{
  "message": "太阳为什么是热的？",
  "conversationId": "conv_456",
  "childAge": 5,
  "context": []
}
```

**响应**:
```json
{
  "reply": "太阳就像一个超级大的火球！它里面有很多很多的能量...",
  "conversationId": "conv_456",
  "suggestions": ["太阳有多大？", "为什么会有白天和黑夜？"]
}
```

### 3.3 图像识别

识别图片中的物体并生成教育内容。

**接口**: `POST /image/recognize`

**请求头**:
```http
Authorization: Bearer {GEMINI_API_KEY}
```

**请求体**: multipart/form-data
- `image`: 图片文件

**响应**:
```json
{
  "object": "猫",
  "confidence": 0.95,
  "educationalContent": "这是一只可爱的小猫咪！猫咪是人类的好朋友...",
  "relatedTopics": ["宠物", "动物", "哺乳动物"]
}
```

## 4. 错误处理

### 错误响应格式
```json
{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "请求参数错误",
    "details": "topic字段不能为空"
  }
}
```

### 错误码说明

| 错误码 | HTTP状态码 | 说明 |
|--------|-----------|------|
| INVALID_REQUEST | 400 | 请求参数错误 |
| UNAUTHORIZED | 401 | 未授权 |
| FORBIDDEN | 403 | 禁止访问 |
| NOT_FOUND | 404 | 资源不存在 |
| RATE_LIMITED | 429 | 请求频率超限 |
| INTERNAL_ERROR | 500 | 服务器内部错误 |
| SERVICE_UNAVAILABLE | 503 | 服务暂时不可用 |

## 5. 限流策略

- 每个API Key限制：100请求/分钟
- 超出限制返回429错误
- 建议使用指数退避重试

## 6. 最佳实践

### 6.1 错误重试
```kotlin
suspend fun <T> retryWithBackoff(
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong()
                .coerceAtMost(maxDelay)
        }
    }
    return block()
}
```

### 6.2 请求优化
- 使用HTTP缓存减少重复请求
- 批量请求合并
- 预加载常用数据

### 6.3 安全建议
- 不要在客户端硬编码API Key
- 使用HTTPS传输
- 验证响应数据完整性

## 7. SDK使用示例

### Kotlin/Android
```kotlin
// 初始化
val apiService = Retrofit.Builder()
    .baseUrl("https://ai-edu-api.cloud.tencent.com/v1/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(AIApiService::class.java)

// 调用API
suspend fun generateStory(topic: String): Story {
    val response = apiService.generateStory(
        apiKey = "Bearer ${BuildConfig.GEMINI_API_KEY}",
        request = StoryGenerateRequest(
            topic = topic,
            childAge = 4,
            duration = 5
        )
    )
    return response.toDomainModel()
}
```

## 8. 更新日志

### v1.0.0 (2024-12-30)
- 初始版本发布
- 支持故事生成、智能对话、图像识别

---

*API文档 - 2024年12月*