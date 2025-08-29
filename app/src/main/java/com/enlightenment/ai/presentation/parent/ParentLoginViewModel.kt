package com.enlightenment.ai.presentation.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * 家长登录ViewModel
 * 
 * 职责说明：
 * 管理家长中心的访问验证，通过简单的数学题确保是成年人操作。
 * 防止儿童误入家长设置区域，保护隐私和安全设置。
 * 
 * 核心功能：
 * 1. 数学题验证（简单加法）
 * 2. 密码提示显示
 * 3. 验证状态管理
 * 4. 自动隐藏提示
 * 
 * 安全策略：
 * - 使用认知差异：儿童难以计算的简单数学
 * - 无需密码存储：避免密码泄露风险
 * - 动态题目：可扩展为随机题目
 * - 友好提示：帮助家长快速通过
 * 
 * 用户体验：
 * - 即时验证反馈
 * - 3秒自动隐藏提示
 * - 清晰的错误信息
 * - 无需记忆密码
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@HiltViewModel
/**
 * ParentLoginViewModel - ParentLogin视图模型
 * 
 * 功能职责：
 * - 管理ParentLogin界面的业务逻辑
 * - 处理用户交互事件和状态更新
 * - 协调数据层和展示层的通信
 * 
 * 状态管理：
 * - 使用StateFlow管理UI状态
 * - 支持配置变更后的状态保持
 * - 提供状态更新的原子性保证
 * 
 * 生命周期：
 * - 自动处理协程作用域
 * - 支持数据预加载
 * - 优雅的资源清理
 * 
 * @since 1.0.0
 */
class ParentLoginViewModel @Inject constructor() : ViewModel() {  // 依赖注入
    
    companion object {
        // 提示显示时长：3秒后自动隐藏
        private val HINT_DISPLAY_DURATION = 3.seconds
    }
    
    // UI状态管理
    private val _uiState = MutableStateFlow<ParentLoginUiState>(ParentLoginUiState.Idle)
    val uiState: StateFlow<ParentLoginUiState> = _uiState.asStateFlow()
    
    // 简单数学题：12 + 7 = 19
    // 后续可扩展为随机题目，增加安全性
    private val correctAnswer = "19"
    
    /**
     * 验证家长密码（数学题答案）
     * 
     * 验证流程：
     * 1. 显示加载状态
     * 2. 执行答案验证
     * 3. 更新UI状态（成功/失败）
     * 
     * 设计思路：
     * - 使用简单加法，成人秒答，儿童困难
     * - 答案固定便于测试，生产可改为随机
     * - 错误提示友好，不透露正确答案
     * 
     * @param answer 用户输入的答案
     */
    fun verifyPassword(answer: String) {
        viewModelScope.launch {  // 启动协程执行异步操作
            _uiState.value = ParentLoginUiState.Loading
            
            // 执行实际验证
            val isValid = verifyAnswer(answer)
            
            if (isValid) {
                _uiState.value = ParentLoginUiState.Success
            } else {
                _uiState.value = ParentLoginUiState.Error("答案不正确，请重试")
            }
        }
    }
    
    /**
     * 验证答案的实际逻辑
     * 
     * 使用协程切换到计算线程，虽然计算简单，
     * 但保持良好的架构习惯。
     * 
     * @param answer 待验证的答案
     * @return 是否正确
     */
    private suspend fun verifyAnswer(answer: String): Boolean {
        return withContext(Dispatchers.Default) {
            // 去除空格后比较
            answer.trim() == correctAnswer
        }
    }
    
    /**
     * 显示密码提示
     * 
     * 功能说明：
     * 1. 显示数学题提示
     * 2. 3秒后自动隐藏
     * 3. 使用协程实现非阻塞延时
     * 
     * 用户体验：
     * - 快速查看提示
     * - 自动消失不干扰
     * - 可重复点击查看
     */
    fun showPasswordHint() {
        _uiState.value = ParentLoginUiState.ShowHint("提示：十二加七等于多少？")
        
        // Auto-dismiss hint after display duration
        viewModelScope.launch {  // 启动协程执行异步操作
            // Use a timer instead of delay for real timing
            withTimeoutOrNull(HINT_DISPLAY_DURATION) {
                // Wait for 用户 action or timeout
                while (_uiState.value is ParentLoginUiState.ShowHint) {
                    ensureActive()
                    yield()
                }
            }
            // Ensure 状态 返回 to 空闲
            if (_uiState.value is ParentLoginUiState.ShowHint) {
                _uiState.value = ParentLoginUiState.Idle
            }
        }
    }
}
/**
 * ParentLoginUiState
 * 
 * 功能说明：
 * 提供ParentLoginUiState相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */


/**
 * ParentLoginUiState - 家长登录UI状态
 * 
 * 功能描述：
 * - 提供核心业务功能处理功能
 * - 支持灵活配置、易于扩展、高性能
 * 
 * 设计说明：
 * - 采用密封类层次设计
 * - 遵循项目统一的架构规范
 * 
 * @since 1.0.0
 */
sealed class ParentLoginUiState {
/**
 * 空闲
 * 
 * 功能说明：
 * 提供Idle相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * 空闲 - 空闲
     * 
     * 功能描述：
     * - 提供核心业务功能处理功能
     * - 支持灵活配置、易于扩展、高性能
     * 
     * 设计说明：
     * - 采用单例模式设计
     * - 遵循项目统一的架构规范
     * 
     * @自版本 1.0.0
     */
    object Idle : ParentLoginUiState()
/**
 * 加载中
 * 
 * 功能说明：
 * 提供Loading相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * 加载中 - 加载中
     * 
     * 功能描述：
     * - 提供核心业务功能处理功能
     * - 支持灵活配置、易于扩展、高性能
     * 
     * 设计说明：
     * - 采用单例模式设计
     * - 遵循项目统一的架构规范
     * 
     * @自版本 1.0.0
     */
    object Loading : ParentLoginUiState()
/**
 * 成功
 * 
 * 功能说明：
 * 提供Success相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * 成功 - 成功
     * 
     * 功能描述：
     * - 提供核心业务功能处理功能
     * - 支持灵活配置、易于扩展、高性能
     * 
     * 设计说明：
     * - 采用单例模式设计
     * - 遵循项目统一的架构规范
     * 
     * @自版本 1.0.0
     */
    object Success : ParentLoginUiState()
/**
 * 错误
 * 
 * 功能说明：
 * 提供Error相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * 错误 - 错误
     * 
     * 功能描述：
     * - 提供核心业务功能处理功能
     * - 支持灵活配置、易于扩展、高性能
     * 
     * 设计说明：
     * - 采用数据类设计
     * - 遵循项目统一的架构规范
     * 
     * @自版本 1.0.0
     */
    data class Error(val message: String) : ParentLoginUiState()
/**
 * ShowHint
 * 
 * 功能说明：
 * 提供ShowHint相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * ShowHint - ShowHint
     * 
     * 功能描述：
     * - 提供核心业务功能处理功能
     * - 支持灵活配置、易于扩展、高性能
     * 
     * 设计说明：
     * - 采用数据类设计
     * - 遵循项目统一的架构规范
     * 
     * @自版本 1.0.0
     */
    data class ShowHint(val hint: String) : ParentLoginUiState()
}