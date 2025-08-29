#!/bin/bash

# AI启蒙时光项目质量检查脚本
# 用于系统化检查项目完整性

echo "========================================="
echo "AI启蒙时光 - 项目质量检查工具 v1.0"
echo "========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 计数器
TOTAL_ISSUES=0

# 1. TODO/FIXME扫描
echo "1. 检查TODO/FIXME标记..."
TODO_COUNT=$(grep -r "TODO\|FIXME\|XXX\|HACK\|TBD\|WIP\|PENDING" --include="*.kt" --include="*.java" --include="*.xml" app/src/ 2>/dev/null | wc -l)
if [ $TODO_COUNT -gt 0 ]; then
    echo -e "${RED}✗ 发现 $TODO_COUNT 个TODO/FIXME标记${NC}"
    echo "详细位置："
    grep -r "TODO\|FIXME\|XXX\|HACK\|TBD\|WIP\|PENDING" --include="*.kt" --include="*.java" --include="*.xml" app/src/ 2>/dev/null | head -10
    TOTAL_ISSUES=$((TOTAL_ISSUES + TODO_COUNT))
else
    echo -e "${GREEN}✓ 未发现TODO/FIXME标记${NC}"
fi
echo ""

# 2. Mock/Stub扫描
echo "2. 检查Mock/Stub代码..."
MOCK_COUNT=$(grep -r "mock\|Mock\|fake\|Fake\|stub\|Stub\|dummy\|Dummy" --include="*.kt" -i app/src/main/java/ 2>/dev/null | grep -v "placeholder" | wc -l)
if [ $MOCK_COUNT -gt 0 ]; then
    echo -e "${YELLOW}⚠ 发现 $MOCK_COUNT 个可能的Mock/Stub引用${NC}"
    echo "详细位置："
    grep -r "mock\|Mock\|fake\|Fake\|stub\|Stub\|dummy\|Dummy" --include="*.kt" -i app/src/main/java/ 2>/dev/null | grep -v "placeholder" | head -10
    # Mock不一定是问题，所以用黄色警告
else
    echo -e "${GREEN}✓ 未发现Mock/Stub代码${NC}"
fi
echo ""

# 3. 占位符注释扫描
echo "3. 检查占位符注释..."
PLACEHOLDER_COUNT=$(grep -r "//.*In production\|//.*would be\|//.*should be\|//.*will be\|//.*to be implemented\|//.*real implementation\|//.*actual implementation" --include="*.kt" app/src/main/ 2>/dev/null | wc -l)
if [ $PLACEHOLDER_COUNT -gt 0 ]; then
    echo -e "${RED}✗ 发现 $PLACEHOLDER_COUNT 个占位符注释${NC}"
    echo "详细位置："
    grep -r "//.*In production\|//.*would be\|//.*should be\|//.*will be\|//.*to be implemented" --include="*.kt" app/src/main/ 2>/dev/null | head -10
    TOTAL_ISSUES=$((TOTAL_ISSUES + PLACEHOLDER_COUNT))
else
    echo -e "${GREEN}✓ 未发现占位符注释${NC}"
fi
echo ""

# 4. 硬编码值扫描
echo "4. 检查硬编码值..."
HARDCODE_COUNT=$(grep -r "localhost\|127.0.0.1\|example.com\|test.com\|demo\|hardcoded" --include="*.kt" app/src/main/java/ 2>/dev/null | grep -v "BuildConfig\|test" | wc -l)
if [ $HARDCODE_COUNT -gt 0 ]; then
    echo -e "${YELLOW}⚠ 发现 $HARDCODE_COUNT 个可能的硬编码值${NC}"
    echo "详细位置："
    grep -r "localhost\|127.0.0.1\|example.com\|test.com" --include="*.kt" app/src/main/java/ 2>/dev/null | head -5
else
    echo -e "${GREEN}✓ 未发现硬编码值${NC}"
fi
echo ""

# 5. 空实现扫描
echo "5. 检查空实现..."
EMPTY_COUNT=$(grep -r "fun.*{[\s]*}" --include="*.kt" app/src/main/java/ 2>/dev/null | wc -l)
if [ $EMPTY_COUNT -gt 0 ]; then
    echo -e "${RED}✗ 发现 $EMPTY_COUNT 个空方法实现${NC}"
    echo "详细位置："
    grep -r "fun.*{[\s]*}" --include="*.kt" app/src/main/java/ 2>/dev/null | head -5
    TOTAL_ISSUES=$((TOTAL_ISSUES + EMPTY_COUNT))
else
    echo -e "${GREEN}✓ 未发现空实现${NC}"
fi
echo ""

# 6. 检查未实现的接口方法
echo "6. 检查可能未实现的功能..."
NOT_IMPLEMENTED_COUNT=$(grep -r "NotImplementedError\|TODO()\|error(\"Not implemented\"\)" --include="*.kt" app/src/ 2>/dev/null | wc -l)
if [ $NOT_IMPLEMENTED_COUNT -gt 0 ]; then
    echo -e "${RED}✗ 发现 $NOT_IMPLEMENTED_COUNT 个未实现的功能${NC}"
    TOTAL_ISSUES=$((TOTAL_ISSUES + NOT_IMPLEMENTED_COUNT))
else
    echo -e "${GREEN}✓ 未发现明显未实现的功能${NC}"
fi
echo ""

# 7. 检查注释中的临时代码标记
echo "7. 检查临时代码标记..."
TEMP_COUNT=$(grep -r "//.*temp\|//.*temporary\|//.*TEMP\|/\*.*temp.*\*/" --include="*.kt" app/src/ 2>/dev/null | wc -l)
if [ $TEMP_COUNT -gt 0 ]; then
    echo -e "${YELLOW}⚠ 发现 $TEMP_COUNT 个临时代码标记${NC}"
else
    echo -e "${GREEN}✓ 未发现临时代码标记${NC}"
fi
echo ""

# 总结
echo "========================================="
echo "检查完成！"
echo ""
if [ $TOTAL_ISSUES -gt 0 ]; then
    echo -e "${RED}总共发现 $TOTAL_ISSUES 个必须修复的问题${NC}"
    echo "请逐项修复后再次运行此脚本。"
    exit 1
else
    echo -e "${GREEN}✓ 恭喜！未发现必须修复的问题。${NC}"
    echo "项目质量检查通过。"
    exit 0
fi