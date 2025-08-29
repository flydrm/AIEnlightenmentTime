#!/bin/bash
# quick-start.sh - 快速启动项目脚本

echo "🚀 AI启蒙时光 - 快速启动脚本"
echo "================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查环境
check_environment() {
    echo "1️⃣ 检查开发环境..."
    
    # 检查Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
        echo -e "  ✅ Java版本: $JAVA_VERSION"
    else
        echo -e "  ${RED}❌ 未找到Java，请安装JDK 11或17${NC}"
        exit 1
    fi
    
    # 检查Android SDK
    if [ -z "$ANDROID_HOME" ]; then
        echo -e "  ${YELLOW}⚠️ ANDROID_HOME未设置${NC}"
        echo "  请在~/.bashrc或~/.zshrc中添加："
        echo "  export ANDROID_HOME=~/Android/Sdk"
        echo "  export PATH=\$PATH:\$ANDROID_HOME/tools:\$ANDROID_HOME/platform-tools"
    else
        echo -e "  ✅ Android SDK: $ANDROID_HOME"
    fi
    
    echo ""
}

# 配置项目
setup_project() {
    echo "2️⃣ 配置项目..."
    
    # 检查local.properties
    if [ ! -f "local.properties" ]; then
        echo -e "  ${YELLOW}创建local.properties...${NC}"
        cat > local.properties << EOF
sdk.dir=$ANDROID_HOME
# API密钥配置（请替换为实际密钥）
GEMINI_API_KEY=your_gemini_api_key_here
GPT_API_KEY=your_gpt_api_key_here
EOF
        echo -e "  ${GREEN}✅ local.properties已创建${NC}"
        echo -e "  ${YELLOW}⚠️ 请编辑local.properties添加API密钥${NC}"
    else
        echo -e "  ✅ local.properties已存在"
    fi
    
    # 检查gradle.properties
    if ! grep -q "org.gradle.jvmargs" gradle.properties 2>/dev/null; then
        echo -e "  ${YELLOW}优化gradle.properties...${NC}"
        cat >> gradle.properties << EOF

# 性能优化配置
org.gradle.jvmargs=-Xmx4096m -XX:+UseParallelGC
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true

# 使用国内镜像
ALIYUN_MAVEN_URL=https://maven.aliyun.com/repository/public
EOF
        echo -e "  ${GREEN}✅ gradle.properties已优化${NC}"
    fi
    
    echo ""
}

# 安装依赖
install_dependencies() {
    echo "3️⃣ 安装项目依赖..."
    
    # 设置Gradle权限
    if [ -f "gradlew" ]; then
        chmod +x gradlew
        echo -e "  ✅ Gradle wrapper权限已设置"
    fi
    
    # 清理并构建
    echo -e "  ${BLUE}开始构建项目...${NC}"
    ./gradlew clean build --no-daemon
    
    if [ $? -eq 0 ]; then
        echo -e "  ${GREEN}✅ 项目构建成功！${NC}"
    else
        echo -e "  ${RED}❌ 项目构建失败，请检查错误信息${NC}"
        exit 1
    fi
    
    echo ""
}

# 运行检查
run_checks() {
    echo "4️⃣ 运行代码检查..."
    
    # 代码格式检查
    echo -e "  ${BLUE}检查代码格式...${NC}"
    ./gradlew ktlintCheck
    
    # 静态代码分析
    echo -e "  ${BLUE}运行静态分析...${NC}"
    ./gradlew lint
    
    # 单元测试
    echo -e "  ${BLUE}运行单元测试...${NC}"
    ./gradlew test
    
    # 注释检查
    if [ -f "docs/sop/project/scripts/check-comments.sh" ]; then
        echo -e "  ${BLUE}检查代码注释...${NC}"
        bash docs/sop/project/scripts/check-comments.sh
    fi
    
    echo ""
}

# 生成快捷命令
create_shortcuts() {
    echo "5️⃣ 创建快捷命令..."
    
    cat > ai-dev.sh << 'EOF'
#!/bin/bash
# AI启蒙时光开发工具

case "$1" in
    run)
        echo "🚀 运行应用..."
        ./gradlew installDebug && adb shell am start -n com.enlightenment.ai/.presentation.MainActivity
        ;;
    test)
        echo "🧪 运行测试..."
        ./gradlew test
        ;;
    lint)
        echo "🔍 代码检查..."
        ./gradlew ktlintCheck && ./gradlew lint
        ;;
    format)
        echo "✨ 格式化代码..."
        ./gradlew ktlintFormat
        ;;
    clean)
        echo "🧹 清理项目..."
        ./gradlew clean
        ;;
    build)
        echo "🔨 构建项目..."
        ./gradlew assembleDebug
        ;;
    release)
        echo "📦 构建发布版..."
        ./gradlew assembleRelease
        ;;
    *)
        echo "用法: ./ai-dev.sh {run|test|lint|format|clean|build|release}"
        exit 1
        ;;
esac
EOF
    
    chmod +x ai-dev.sh
    echo -e "  ${GREEN}✅ 快捷命令已创建: ./ai-dev.sh${NC}"
    echo ""
}

# 显示下一步
show_next_steps() {
    echo "✅ 项目配置完成！"
    echo ""
    echo "📝 下一步操作："
    echo "1. 编辑 local.properties 添加API密钥"
    echo "2. 在Android Studio中打开项目"
    echo "3. 同步Gradle依赖"
    echo "4. 运行应用: ./ai-dev.sh run"
    echo ""
    echo "🔧 常用命令："
    echo "  ./ai-dev.sh run     - 运行应用"
    echo "  ./ai-dev.sh test    - 运行测试"
    echo "  ./ai-dev.sh lint    - 代码检查"
    echo "  ./ai-dev.sh format  - 格式化代码"
    echo ""
    echo "📚 查看完整文档: docs/sop/project/"
    echo ""
}

# 主流程
main() {
    check_environment
    setup_project
    install_dependencies
    run_checks
    create_shortcuts
    show_next_steps
}

# 执行主流程
main