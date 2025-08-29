package com.enlightenment.ai.domain.repository

import com.enlightenment.ai.domain.model.Story
import kotlinx.coroutines.flow.Flow

/**
 * 领域层 - 故事仓库接口
 * 
 * 职责说明：
 * 定义故事相关的业务操作契约，不关心具体实现细节。
 * 遵循依赖倒置原则，让数据层依赖于领域层的抽象。
 * 
 * 设计原则：
 * 1. 接口隔离：只定义必要的操作方法
 * 2. 单一职责：专注于故事的生成和管理
 * 3. 技术无关：不涉及具体存储或网络实现
 * 
 * 实现要求：
 * - 必须处理网络异常，提供降级方案
 * - 必须实现缓存机制，支持离线使用
 * - 必须进行内容过滤，确保儿童友好
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
/**
 * StoryRepository - Story仓库
 * 
 * 仓库接口定义，抽象数据访问操作
 * 
 * 接口设计：
 * - 定义数据访问契约
 * - 隐藏数据源实现细节
 * - 支持依赖注入
 * 
 * 实现要求：
 * - 线程安全
 * - 错误处理
 * - 资源管理
 * 
 * @since 1.0.0
 */
interface StoryRepository {
    /**
     * 生成AI故事
     * 
     * 功能说明：
     * 调用AI服务生成个性化的儿童故事。
     * 根据年龄和兴趣定制内容，确保适合目标儿童。
     * 
     * 实现要求：
     * 1. 调用AI服务前检查网络状态
     * 2. 设置合理的超时时间（建议30秒）
     * 3. 失败时尝试返回缓存内容
     * 4. 成功后自动保存到本地缓存
     * 
     * @param childAge 儿童年龄（3-6岁），用于调整故事难度
     * @param interests 兴趣列表，如["恐龙", "太空", "公主"]
     * @param theme 可选主题，如"冒险"、"友谊"、"勇气"
     * @return 成功返回Story对象，失败返回具体错误
     */
    suspend fun generateStory(
        childAge: Int,
        interests: List<String>,
        theme: String? = null
    ): Result<Story>
    
    /**
     * 根据ID获取故事
     * 
     * 用于从历史记录或缓存中获取已有故事。
     * 
     * @param id 故事唯一标识
     * @return 找到返回Story对象，否则返回null
     */
    suspend fun getStoryById(id: String): Story?
    
    /**
     * 保存故事到本地
     * 
     * 将生成或收藏的故事保存到本地数据库。
     * 用于离线访问和历史记录。
     * 
     * @param story 要保存的故事对象
     */
    suspend fun saveStory(story: Story)
    
    /**
     * 获取最近故事流
     * 
     * 返回一个响应式的故事列表流。
     * 当有新故事添加时自动更新。
     * 
     * @return 最近故事列表的Flow，按时间倒序
     */
    fun getRecentStories(): Flow<List<Story>>
    
    /**
     * 获取缓存故事列表
     * 
     * 一次性获取所有缓存的故事。
     * 主要用于离线模式下的内容展示。
     * 
     * @return 缓存的故事列表
     */
    suspend fun getCachedStories(): List<Story>
}