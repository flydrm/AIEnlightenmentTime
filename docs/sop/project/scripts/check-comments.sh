#!/bin/bash
# check-comments.sh - 代码注释质量检查脚本

echo "🔍 AI启蒙时光项目 - 代码注释质量检查"
echo "========================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 计数器
TOTAL_FILES=0
FILES_WITH_COMMENT=0
COMPLEX_METHODS=0
METHODS_WITH_COMMENT=0
UI_COMPONENTS=0
UI_WITH_COMMENT=0
MISSING_COMMENTS=""

# 检查源码目录是否存在
if [ ! -d "app/src/main" ]; then
    echo -e "${RED}❌ 错误：找不到源码目录 app/src/main${NC}"
    echo "请在项目根目录运行此脚本"
    exit 1
fi

echo "📊 开始分析代码注释覆盖率..."
echo ""

# 1. 检查类和接口的注释
echo "1️⃣ 检查类/接口注释..."
while IFS= read -r file; do
    TOTAL_FILES=$((TOTAL_FILES + 1))
    
    # 检查是否有类级别的注释（改进版：支持缩进的类定义）
    if grep -B10 "^\s*\(class\|interface\|object\|enum\|data class\|sealed class\)" "$file" | grep -q "^\s*\*/"; then
        FILES_WITH_COMMENT=$((FILES_WITH_COMMENT + 1))
    else
        # 获取类名
        CLASS_NAME=$(grep -E "^\s*(class|interface|object|enum|data class|sealed class)" "$file" | head -1 | awk '{print $2}')
        if [ ! -z "$CLASS_NAME" ]; then
            MISSING_COMMENTS="${MISSING_COMMENTS}\n  - $file ($CLASS_NAME)"
        fi
    fi
done < <(find app/src/main -name "*.kt" -type f)

CLASS_COVERAGE=$((FILES_WITH_COMMENT * 100 / TOTAL_FILES))
echo "  ✅ 类注释覆盖率: $FILES_WITH_COMMENT/$TOTAL_FILES ($CLASS_COVERAGE%)"

# 2. 检查复杂方法的注释（超过10行的方法）
echo ""
echo "2️⃣ 检查复杂方法注释..."
while IFS= read -r file; do
    # 使用awk查找超过10行的函数
    awk '
    /fun / && !/^[[:space:]]*\/\// {
        start = NR
        name = $0
        gsub(/^[[:space:]]*/, "", name)
    }
    /{/ && start {
        brace_count = 1
        method_start = start
        method_name = name
    }
    brace_count > 0 {
        gsub(/\/\/.*$/, "")  # 移除行注释
        gsub(/\/\*.*\*\//, "")  # 移除块注释
        brace_count += gsub(/{/, "{")
        brace_count -= gsub(/}/, "}")
        if (brace_count == 0) {
            if (NR - method_start > 10) {
                print FILENAME ":" method_start "-" NR ":" method_name
            }
            start = 0
        }
    }
    ' "$file"
done < <(find app/src/main -name "*.kt" -type f) | while IFS=: read -r file lines method; do
    COMPLEX_METHODS=$((COMPLEX_METHODS + 1))
    
    # 检查方法前是否有注释
    LINE_NUM=$(echo "$lines" | cut -d'-' -f1)
    if [ "$LINE_NUM" -gt 5 ]; then
        CHECK_LINE=$((LINE_NUM - 5))
    else
        CHECK_LINE=1
    fi
    
    if sed -n "${CHECK_LINE},${LINE_NUM}p" "$file" | grep -q "\*/"; then
        METHODS_WITH_COMMENT=$((METHODS_WITH_COMMENT + 1))
    else
        MISSING_COMMENTS="${MISSING_COMMENTS}\n  - $file:$lines (复杂方法需要注释)"
    fi
done

if [ $COMPLEX_METHODS -gt 0 ]; then
    METHOD_COVERAGE=$((METHODS_WITH_COMMENT * 100 / COMPLEX_METHODS))
    echo "  ✅ 复杂方法注释覆盖率: $METHODS_WITH_COMMENT/$COMPLEX_METHODS ($METHOD_COVERAGE%)"
else
    echo "  ℹ️ 未发现复杂方法"
fi

# 3. 检查UI组件的交互注释
echo ""
echo "3️⃣ 检查UI组件交互注释..."
while IFS= read -r file; do
    if grep -q "@Composable" "$file"; then
        UI_COMPONENTS=$((UI_COMPONENTS + 1))
        
        # 检查是否有交互说明
        if grep -B20 "@Composable" "$file" | grep -q "交互\|用户\|点击\|操作"; then
            UI_WITH_COMMENT=$((UI_WITH_COMMENT + 1))
        else
            MISSING_COMMENTS="${MISSING_COMMENTS}\n  - $file (UI组件缺少交互说明)"
        fi
    fi
done < <(find app/src/main -name "*.kt" -type f | grep -E "(Screen|Component|View)\.kt$")

if [ $UI_COMPONENTS -gt 0 ]; then
    UI_COVERAGE=$((UI_WITH_COMMENT * 100 / UI_COMPONENTS))
    echo "  ✅ UI组件注释覆盖率: $UI_WITH_COMMENT/$UI_COMPONENTS ($UI_COVERAGE%)"
else
    echo "  ℹ️ 未发现UI组件"
fi

# 4. 检查TODO和FIXME
echo ""
echo "4️⃣ 检查TODO/FIXME项..."
TODO_COUNT=$(grep -r "TODO\|FIXME\|XXX\|HACK" app/src/main --include="*.kt" | wc -l)
if [ $TODO_COUNT -gt 0 ]; then
    echo -e "  ${YELLOW}⚠️ 发现 $TODO_COUNT 个TODO/FIXME项${NC}"
    grep -r "TODO\|FIXME\|XXX\|HACK" app/src/main --include="*.kt" | head -5
    if [ $TODO_COUNT -gt 5 ]; then
        echo "  ... 还有 $((TODO_COUNT - 5)) 个"
    fi
else
    echo "  ✅ 没有发现TODO/FIXME项"
fi

# 5. 检查中文注释比例
echo ""
echo "5️⃣ 检查中文注释使用情况..."
TOTAL_COMMENTS=$(grep -r "^\s*\*\|^\s*\/\/" app/src/main --include="*.kt" | wc -l)
CHINESE_COMMENTS=$(grep -r "^\s*\*\|^\s*\/\/" app/src/main --include="*.kt" | grep -P "[\x{4e00}-\x{9fa5}]" | wc -l)
if [ $TOTAL_COMMENTS -gt 0 ]; then
    CHINESE_RATIO=$((CHINESE_COMMENTS * 100 / TOTAL_COMMENTS))
    echo "  ✅ 中文注释比例: $CHINESE_COMMENTS/$TOTAL_COMMENTS ($CHINESE_RATIO%)"
else
    echo "  ⚠️ 未发现注释"
fi

# 生成总体报告
echo ""
echo "📈 注释质量总体评分"
echo "===================="

# 计算总分
TOTAL_SCORE=0
WEIGHT_CLASS=40      # 类注释权重40%
WEIGHT_METHOD=30     # 方法注释权重30%
WEIGHT_UI=20         # UI注释权重20%
WEIGHT_CHINESE=10    # 中文注释权重10%

# 类注释得分
if [ $TOTAL_FILES -gt 0 ]; then
    CLASS_SCORE=$((CLASS_COVERAGE * WEIGHT_CLASS / 100))
    TOTAL_SCORE=$((TOTAL_SCORE + CLASS_SCORE))
    echo "类注释得分: $CLASS_SCORE/$WEIGHT_CLASS"
fi

# 方法注释得分
if [ $COMPLEX_METHODS -gt 0 ]; then
    METHOD_SCORE=$((METHOD_COVERAGE * WEIGHT_METHOD / 100))
    TOTAL_SCORE=$((TOTAL_SCORE + METHOD_SCORE))
    echo "方法注释得分: $METHOD_SCORE/$WEIGHT_METHOD"
else
    TOTAL_SCORE=$((TOTAL_SCORE + WEIGHT_METHOD))
    echo "方法注释得分: $WEIGHT_METHOD/$WEIGHT_METHOD (无复杂方法)"
fi

# UI注释得分
if [ $UI_COMPONENTS -gt 0 ]; then
    UI_SCORE=$((UI_COVERAGE * WEIGHT_UI / 100))
    TOTAL_SCORE=$((TOTAL_SCORE + UI_SCORE))
    echo "UI注释得分: $UI_SCORE/$WEIGHT_UI"
else
    TOTAL_SCORE=$((TOTAL_SCORE + WEIGHT_UI))
    echo "UI注释得分: $WEIGHT_UI/$WEIGHT_UI (无UI组件)"
fi

# 中文注释得分
if [ $TOTAL_COMMENTS -gt 0 ] && [ $CHINESE_RATIO -ge 60 ]; then
    CHINESE_SCORE=$WEIGHT_CHINESE
else
    CHINESE_SCORE=$((CHINESE_RATIO * WEIGHT_CHINESE / 100))
fi
TOTAL_SCORE=$((TOTAL_SCORE + CHINESE_SCORE))
echo "中文注释得分: $CHINESE_SCORE/$WEIGHT_CHINESE"

echo "-------------------"
echo -e "${GREEN}总分: $TOTAL_SCORE/100${NC}"

# 评级
if [ $TOTAL_SCORE -ge 90 ]; then
    echo -e "${GREEN}🏆 评级: A (优秀)${NC}"
    echo "代码注释质量非常好，继续保持！"
elif [ $TOTAL_SCORE -ge 80 ]; then
    echo -e "${GREEN}✅ 评级: B (良好)${NC}"
    echo "代码注释质量良好，还有提升空间。"
elif [ $TOTAL_SCORE -ge 60 ]; then
    echo -e "${YELLOW}⚠️ 评级: C (及格)${NC}"
    echo "代码注释基本达标，建议补充更多注释。"
else
    echo -e "${RED}❌ 评级: D (不及格)${NC}"
    echo "代码注释严重不足，请立即改进！"
fi

# 显示缺少注释的文件
if [ ! -z "$MISSING_COMMENTS" ]; then
    echo ""
    echo "📝 需要补充注释的位置："
    echo -e "$MISSING_COMMENTS" | head -20
    MISSING_COUNT=$(echo -e "$MISSING_COMMENTS" | wc -l)
    if [ $MISSING_COUNT -gt 20 ]; then
        echo "  ... 还有 $((MISSING_COUNT - 20)) 处"
    fi
fi

# 生成改进建议
echo ""
echo "💡 改进建议："
echo "============"

if [ $CLASS_COVERAGE -lt 80 ]; then
    echo "1. 为所有类添加功能说明注释"
fi

if [ $COMPLEX_METHODS -gt 0 ] && [ $METHOD_COVERAGE -lt 80 ]; then
    echo "2. 为复杂方法添加详细的流程注释"
fi

if [ $UI_COMPONENTS -gt 0 ] && [ $UI_COVERAGE -lt 80 ]; then
    echo "3. 为UI组件添加交互说明"
fi

if [ $CHINESE_RATIO -lt 60 ]; then
    echo "4. 增加中文注释，提高可读性"
fi

if [ $TODO_COUNT -gt 0 ]; then
    echo "5. 清理TODO/FIXME项"
fi

echo ""
echo "参考文档：docs/sop/project/09-comment-standards.md"

# 返回状态码
if [ $TOTAL_SCORE -lt 80 ]; then
    echo ""
    echo -e "${RED}❌ 注释质量未达标（要求80分以上）${NC}"
    exit 1
else
    echo ""
    echo -e "${GREEN}✅ 注释质量检查通过${NC}"
    exit 0
fi