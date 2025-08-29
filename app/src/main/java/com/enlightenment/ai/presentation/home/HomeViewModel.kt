package com.enlightenment.ai.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 首页ViewModel
 * 
 * 职责说明：
 * 管理首页的UI状态，包括个性化问候语和小熊猫动画状态。
 * 根据儿童档案信息提供个性化的用户体验。
 * 
 * 核心功能：
 * 1. 加载儿童档案信息
 * 2. 生成个性化问候语
 * 3. 控制小熊猫表情动画
 * 4. 管理页面加载状态
 * 
 * 个性化策略：
 * - 有档案：使用儿童姓名打招呼
 * - 无档案：使用通用问候语
 * - 根据时间段调整问候内容（可扩展）
 * 
 * @property profileRepository 档案仓库，获取儿童信息
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@HiltViewModel
/**
 * HomeViewModel - Home视图模型
 * 
 * 功能职责：
 * - 管理Home界面的业务逻辑
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
class HomeViewModel @Inject constructor(  // 依赖注入
    private val profileRepository: ProfileRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        // 初始化时加载儿童档案
        loadProfile()
    }
    
    /**
     * 加载儿童档案信息
     * 
     * 功能流程：
     * 1. 异步获取档案数据
     * 2. 根据档案生成个性化问候
     * 3. 设置小熊猫表情为快乐
     * 
     * 问候语策略：
     * - 有姓名：「嗨，{姓名}！今天想学什么呢？」
     * - 无姓名：「嗨，小朋友！让我们开始今天的学习吧！」
     * 
     * 后续扩展：
     * - 根据时间段调整问候（早上好/下午好/晚上好）
     * - 根据学习记录推荐内容
     * - 节日特殊问候
     */
    private fun loadProfile() {
        viewModelScope.launch {  // 启动协程执行异步操作
            // 从仓库获取档案
            val profile = profileRepository.getProfile()
            
            // 更新UI状态
            _uiState.update { currentState ->  // 更新UI状态
                currentState.copy(
                    greeting = if (profile != null) {
                        "嗨，${profile.name}！今天想学什么呢？"
                    } else {
                        "嗨，小朋友！让我们开始今天的学习吧！"
                    },
                    pandaMood = "happy"  // 初始状态总是快乐的
                )
            }
        }
    }
}

/**
 * 首页UI状态
 * 
 * 包含首页所需的所有UI状态数据。
 * 使用data class确保状态的不可变性。
 * 
 * @property greeting 个性化问候语
 * @property pandaMood 小熊猫表情状态（happy/curious/excited等）
 * @property isLoading 页面加载状态，预留给未来使用
 */
/**
 * HomeUiState - 主页UI状态
 * 
 * 功能描述：
 * - 提供核心业务功能处理功能
 * - 支持灵活配置、易于扩展、高性能
 * 
 * 设计说明：
 * - 采用数据类设计
 * - 遵循项目统一的架构规范
 * 
 * @since 1.0.0
 */
data class HomeUiState(
    val greeting: String = "嗨，小朋友！",
    val pandaMood: String = "happy",
    val isLoading: Boolean = false
)