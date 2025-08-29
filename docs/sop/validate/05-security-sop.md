# 安全验证SOP

## 目的
确保应用的数据安全、用户隐私保护和内容安全，特别是针对儿童用户。

## 安全验证清单

### 1. 数据安全

#### 1.1 敏感数据加密
```kotlin
// ✅ 使用EncryptedSharedPreferences
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

// ❌ 避免明文存储
val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
prefs.edit().putString("api_key", "sk-xxxxx").apply() // 不安全！
```

#### 1.2 API密钥保护
```kotlin
// ✅ 从BuildConfig读取
private val apiKey = BuildConfig.GEMINI_API_KEY

// ✅ gradle.properties配置
GEMINI_API_KEY=your_encrypted_key

// ❌ 硬编码密钥
private val apiKey = "sk-1234567890" // 绝对禁止！
```

### 2. 网络安全

#### 2.1 HTTPS强制
```xml
<!-- network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">ai-edu-api.cloud.tencent.com</domain>
    </domain-config>
</network-security-config>
```

#### 2.2 证书固定（可选）
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.example.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

### 3. 内容安全（儿童保护）

#### 3.1 内容过滤器
```kotlin
class ContentSafetyFilter {
    private val inappropriateKeywords = listOf(
        // 不适合儿童的关键词
        "violence", "adult", "scary", // 示例
    )
    
    fun isContentSafe(content: String): Boolean {
        val lowercaseContent = content.lowercase()
        return inappropriateKeywords.none { 
            lowercaseContent.contains(it) 
        }
    }
    
    fun filterContent(content: String): String {
        var filtered = content
        inappropriateKeywords.forEach { keyword ->
            filtered = filtered.replace(keyword, "***", ignoreCase = true)
        }
        return filtered
    }
}
```

#### 3.2 图片内容审核
```kotlin
// 在图片识别前进行安全检查
suspend fun recognizeImage(imageUri: String): Result<String> {
    // 1. 本地预检查
    if (!isImageSafe(imageUri)) {
        return Result.failure(Exception("图片内容不适合"))
    }
    
    // 2. API调用时的安全参数
    val response = api.recognizeImage(
        image = imageUri,
        safetyLevel = "strict_children"
    )
    
    // 3. 结果过滤
    return Result.success(
        contentFilter.filterContent(response.description)
    )
}
```

### 4. 权限管理

#### 4.1 最小权限原则
```xml
<!-- AndroidManifest.xml -->
<!-- ✅ 只申请必需权限 -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />

<!-- ❌ 避免不必要的权限 -->
<!-- <uses-permission android:name="android.permission.READ_CONTACTS" /> -->
```

#### 4.2 运行时权限处理
```kotlin
// 权限请求前解释用途
@Composable
fun CameraPermissionRequest() {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showEducationalDialog()
        }
    }
    
    Button(onClick = { 
        launcher.launch(Manifest.permission.CAMERA) 
    }) {
        Text("需要相机权限来识别物体")
    }
}
```

### 5. 用户隐私

#### 5.1 数据收集最小化
```kotlin
// ✅ 只收集必要数据
data class UserAnalytics(
    val sessionId: String = UUID.randomUUID().toString(),
    val appVersion: String,
    // 不收集个人信息
)

// ❌ 避免收集敏感信息
data class BadAnalytics(
    val deviceId: String,      // 避免
    val location: Location,    // 避免
    val contacts: List<String> // 绝对禁止
)
```

#### 5.2 家长控制
```kotlin
// 家长验证保护
class ParentGate {
    fun showVerification(onSuccess: () -> Unit) {
        // 数学问题验证
        val question = "3 + 5 = ?"
        val answer = 8
        
        // 验证逻辑
        if (userAnswer == answer) {
            onSuccess()
        }
    }
}
```

### 6. 代码混淆

#### 6.1 ProGuard配置
```proguard
# 保护敏感类
-keep class com.enlightenment.ai.core.security.** { *; }

# 混淆其他代码
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
```

## 安全测试工具

### 1. 静态分析
```bash
# 使用Android Lint
./gradlew lint

# 检查结果
app/build/reports/lint-results.html
```

### 2. 依赖漏洞扫描
```bash
# 使用dependency-check
./gradlew dependencyCheckAnalyze
```

### 3. 手动测试清单
- [ ] 尝试抓包查看API请求
- [ ] 检查本地存储文件
- [ ] 反编译APK查看代码
- [ ] 测试权限拒绝场景

## 安全事件响应

### 1. 安全更新流程
1. 发现漏洞 → 评估影响
2. 开发修复 → 测试验证
3. 发布更新 → 通知用户
4. 监控反馈 → 持续改进

### 2. 数据泄露应对
1. 立即停止数据收集
2. 评估影响范围
3. 通知受影响用户
4. 实施补救措施

## 合规要求

### 1. 儿童在线隐私保护法（COPPA）
- 不收集13岁以下儿童个人信息
- 获得家长同意
- 提供家长控制选项

### 2. GDPR合规
- 数据处理透明
- 用户有删除权
- 数据便携性

### 3. 中国相关法规
- 遵守网络安全法
- 儿童个人信息保护
- 数据本地化要求

## 安全检查脚本

```bash
#!/bin/bash
# security-check.sh

echo "🔒 安全检查"

# 1. 检查硬编码密钥
echo -n "检查硬编码密钥... "
if grep -r "sk-\|api_key.*=.*\"" app/src/main --include="*.kt" | grep -v "BuildConfig"; then
    echo "❌ 发现硬编码密钥"
    exit 1
else
    echo "✅ 通过"
fi

# 2. 检查HTTP使用
echo -n "检查不安全的HTTP... "
if grep -r "http://" app/src/main --include="*.kt" | grep -v "https://"; then
    echo "⚠️ 发现HTTP使用"
else
    echo "✅ 通过"
fi

# 3. 检查权限
echo -n "检查过度权限... "
dangerous_perms=$(grep -c "uses-permission" app/src/main/AndroidManifest.xml)
if [ $dangerous_perms -gt 5 ]; then
    echo "⚠️ 权限过多: $dangerous_perms"
else
    echo "✅ 通过"
fi

echo "✅ 安全检查完成！"
```

---

*SOP版本：1.0*  
*专注于儿童应用安全*