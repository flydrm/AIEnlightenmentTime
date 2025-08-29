package com.enlightenment.ai.domain.repository

import com.enlightenment.ai.domain.model.ChildProfile
import kotlinx.coroutines.flow.Flow

/**
 * 领域层 - 儿童档案仓库接口
 * 
 * 职责说明：
 * 管理儿童用户的个人档案和学习数据。
 * 提供档案的创建、读取、更新和观察功能。
 * 
 * 核心功能：
 * 1. 档案数据持久化
 * 2. 兴趣爱好管理
 * 3. 学习进度追踪
 * 4. 响应式数据观察
 * 
 * 设计理念：
 * - 单一用户：每个设备只支持一个儿童档案
 * - 隐私优先：所有数据本地存储
 * - 实时更新：使用Flow提供响应式数据
 * 
 * 数据安全：
 * - 不上传个人信息到服务器
 * - 支持档案导出和删除
 * - 家长可控制数据访问
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
interface ProfileRepository {
    /**
     * 获取当前儿童档案
     * 
     * @return 存在返回档案对象，不存在返回null
     */
    suspend fun getProfile(): ChildProfile?
    
    /**
     * 保存或更新档案
     * 
     * 首次保存创建新档案，已存在则更新。
     * 
     * @param profile 要保存的档案信息
     */
    suspend fun saveProfile(profile: ChildProfile)
    
    /**
     * 观察档案变化
     * 
     * 返回响应式数据流，档案更新时自动通知。
     * 用于UI实时展示最新信息。
     * 
     * @return 档案数据流，可能为null
     */
    fun observeProfile(): Flow<ChildProfile?>
    
    /**
     * 更新兴趣爱好
     * 
     * 单独更新兴趣列表，用于快速调整推荐内容。
     * 
     * @param interests 新的兴趣列表
     */
    suspend fun updateInterests(interests: List<String>)
    
    /**
     * 更新学习进度
     * 
     * 记录特定主题的学习成绩，用于个性化推荐。
     * 
     * @param topic 学习主题
     * @param score 得分（0-1）
     */
    suspend fun updateLearningProgress(topic: String, score: Float)
}