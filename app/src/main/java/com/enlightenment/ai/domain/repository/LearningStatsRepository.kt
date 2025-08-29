package com.enlightenment.ai.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 领域层 - 学习统计仓库接口
 * 
 * 职责说明：
 * 管理儿童的学习数据统计，包括学习时长、完成故事数、连续天数等。
 * 为家长了解孩子学习情况和系统个性化推荐提供数据支持。
 * 
 * 核心功能：
 * 1. 记录学习行为数据
 * 2. 统计学习成果
 * 3. 追踪学习习惯
 * 4. 提供实时数据流
 * 
 * 数据价值：
 * - 激励机制：展示学习成就，激发学习动力
 * - 家长监督：让家长了解孩子学习情况
 * - 个性推荐：基于数据优化内容推荐
 * - 习惯培养：通过连续天数培养学习习惯
 * 
 * 隐私保护：
 * - 所有数据本地存储
 * - 不包含个人隐私信息
 * - 支持数据导出和清除
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
/**
 * LearningStatsRepository - LearningStats仓库
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
interface LearningStatsRepository {
    /**
     * 增加完成故事数
     * 
     * 在儿童完成一个故事（包括问答）后调用。
     * 自动更新今日故事数和总故事数。
     */
    suspend fun incrementStoriesCompleted()
    
    /**
     * 记录学习时长
     * 
     * 记录本次学习的时长，用于统计每日和总学习时间。
     * 
     * @param durationMinutes 学习时长（分钟）
     */
    suspend fun recordLearningSession(durationMinutes: Int)
    
    /**
     * 获取总完成故事数
     * 
     * @return 历史累计完成的故事总数
     */
    suspend fun getStoriesCompleted(): Int
    
    /**
     * 获取总学习天数
     * 
     * 统计有学习记录的天数，不要求连续。
     * 
     * @return 累计学习天数
     */
    suspend fun getTotalLearningDays(): Int
    
    /**
     * 获取最后学习日期
     * 
     * 用于计算连续学习天数和提醒功能。
     * 
     * @return 最后学习的时间戳，从未学习返回null
     */
    suspend fun getLastLearningDate(): Long?
    
    /**
     * 获取今日学习分钟数
     * 
     * 统计今天的累计学习时长。
     * 
     * @return 今日学习分钟数
     */
    suspend fun getTodayMinutes(): Int
    
    /**
     * 获取今日完成故事数
     * 
     * 统计今天完成的故事数量。
     * 
     * @return 今日完成故事数
     */
    suspend fun getTodayStories(): Int
    
    /**
     * 观察学习统计数据
     * 
     * 返回响应式数据流，实时更新学习统计。
     * 用于UI展示最新的学习数据。
     * 
     * @return 学习统计数据流
     */
    fun observeLearningStats(): Flow<LearningStats>
}

/**
 * 学习统计数据
 * 
 * 汇总的学习数据模型，用于展示和分析。
 * 包含各维度的学习指标。
 * 
 * @property totalStoriesCompleted 总完成故事数
 * @property totalLearningDays 总学习天数（非连续）
 * @property currentStreak 当前连续学习天数
 * @property totalLearningMinutes 总学习分钟数
 * @property lastLearningDate 最后学习时间戳
 * 
 * 使用示例：
 * - 个人中心展示学习成就
 * - 家长查看学习报告
 * - 生成学习激励徽章
 */
/**
 * LearningStats - LearningStats
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
data class LearningStats(
    val totalStoriesCompleted: Int = 0,
    val totalLearningDays: Int = 0,
    val currentStreak: Int = 0,
    val totalLearningMinutes: Int = 0,
    val lastLearningDate: Long? = null
)