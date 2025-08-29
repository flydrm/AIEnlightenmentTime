#!/bin/bash

# 深度验证脚本 - 确保项目真正完成

echo "======================================"
echo "🔍 深度项目验证 v3.0"
echo "======================================"
echo ""

TOTAL_ISSUES=0

# 1. 检查所有导航目标是否有对应的Screen
echo "1. 检查导航完整性..."
NAV_ROUTES=$(grep -h "navigate(" app/src/main/java/**/*.kt 2>/dev/null | grep -oE 'Screen\.[A-Za-z]+\.route' | sort -u)
DEFINED_ROUTES=$(grep -h "object.*:.*Screen(" app/src/main/java/**/*.kt 2>/dev/null | grep -oE 'object [A-Za-z]+' | awk '{print "Screen."$2".route"}' | sort -u)

for route in $NAV_ROUTES; do
    if ! echo "$DEFINED_ROUTES" | grep -q "$route"; then
        echo "  ❌ 缺失路由定义: $route"
        ((TOTAL_ISSUES++))
    fi
done

# 2. 检查所有Screen是否在NavHost中注册
echo ""
echo "2. 检查Screen注册..."
SCREEN_OBJECTS=$(grep -h "object.*:.*Screen(" app/src/main/java/**/*.kt 2>/dev/null | grep -oE 'object [A-Za-z]+' | awk '{print $2}')

for screen in $SCREEN_OBJECTS; do
    if ! grep -q "composable(Screen.$screen.route)" app/src/main/java/**/EnlightenmentNavHost.kt 2>/dev/null; then
        echo "  ❌ Screen未注册: $screen"
        ((TOTAL_ISSUES++))
    fi
done

# 3. 检查所有Repository实现是否使用持久化
echo ""
echo "3. 检查数据持久化..."
REPO_IMPLS=$(find app/src/main/java -name "*RepositoryImpl.kt" 2>/dev/null)

for repo in $REPO_IMPLS; do
    if grep -q "mutableMapOf\|mutableListOf\|mutableSetOf" "$repo" 2>/dev/null; then
        if ! grep -q "dataStore\|dao\|database\|sharedPreferences" "$repo" 2>/dev/null; then
            echo "  ⚠️  可能使用内存存储: $(basename $repo)"
            ((TOTAL_ISSUES++))
        fi
    fi
done

# 4. 检查所有ViewModel是否有空实现
echo ""
echo "4. 检查ViewModel实现..."
VM_FILES=$(find app/src/main/java -name "*ViewModel.kt" ! -name "*Test.kt" 2>/dev/null)

for vm in $VM_FILES; do
    # 检查空函数体
    if grep -E "fun\s+\w+\s*\([^)]*\)\s*\{\s*\}" "$vm" 2>/dev/null | grep -v "data class\|interface" > /dev/null; then
        echo "  ❌ 发现空函数: $(basename $vm)"
        ((TOTAL_ISSUES++))
    fi
done

# 5. 检查API端点配置
echo ""
echo "5. 检查API配置..."
if grep -r "localhost\|127.0.0.1" app/build.gradle.kts app/src/main/res/xml/*.xml 2>/dev/null | grep -v "//\|#" > /dev/null; then
    echo "  ❌ 发现本地API地址"
    ((TOTAL_ISSUES++))
fi

# 6. 检查关键功能的端到端实现
echo ""
echo "6. 检查核心功能链路..."

# 检查故事生成链路
STORY_CHAIN=(
    "GenerateStoryUseCase"
    "StoryRepository" 
    "StoryRepositoryImpl"
    "AIApiService"
    "StoryViewModel"
    "StoryScreen"
)

for component in "${STORY_CHAIN[@]}"; do
    if ! find app/src/main/java -name "*.kt" -exec grep -l "$component" {} \; 2>/dev/null | grep -v Test > /dev/null; then
        echo "  ❌ 故事功能链缺失: $component"
        ((TOTAL_ISSUES++))
    fi
done

# 7. 检查测试覆盖
echo ""
echo "7. 检查测试覆盖..."
MAIN_FILES=$(find app/src/main/java -name "*.kt" ! -path "*/di/*" ! -name "*Entity.kt" ! -name "*Dao.kt" | wc -l)
TEST_FILES=$(find app/src/test app/src/androidTest -name "*Test.kt" 2>/dev/null | wc -l)

if [ $TEST_FILES -lt $((MAIN_FILES / 4)) ]; then
    echo "  ⚠️  测试覆盖率可能不足: $TEST_FILES 测试文件 vs $MAIN_FILES 源文件"
fi

# 8. 检查关键配置
echo ""
echo "8. 检查关键配置..."

# 检查网络安全配置
if ! grep -q "cleartextTrafficPermitted=\"false\"" app/src/main/res/xml/network_security_config.xml 2>/dev/null; then
    echo "  ⚠️  网络安全配置可能不完整"
fi

# 检查ProGuard配置
if ! [ -f app/proguard-rules.pro ]; then
    echo "  ❌ 缺少ProGuard配置"
    ((TOTAL_ISSUES++))
fi

# 总结
echo ""
echo "======================================"
if [ $TOTAL_ISSUES -eq 0 ]; then
    echo "✅ 深度验证通过！项目完整性良好。"
    exit 0
else
    echo "❌ 发现 $TOTAL_ISSUES 个潜在问题需要关注"
    exit 1
fi