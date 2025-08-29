package com.enlightenment.ai.domain.usecase

import com.enlightenment.ai.domain.model.Story
import com.enlightenment.ai.domain.repository.ProfileRepository
import com.enlightenment.ai.domain.repository.StoryRepository
import javax.inject.Inject

/**
 * 故事生成用例
 * 
 * 功能说明：
 * 协调故事生成的业务逻辑，整合儿童档案信息和故事主题，
 * 调用AI服务生成个性化的儿童故事。
 * 
 * 业务流程：
 * 1. 获取当前儿童档案信息
 * 2. 提取年龄和兴趣爱好
 * 3. 结合主题生成故事
 * 4. 返回生成结果
 * 
 * 设计理念：
 * - 单一职责：只负责故事生成的业务编排
 * - 依赖注入：通过构造函数注入依赖
 * - 错误传递：不处理异常，由调用方决定
 * 
 * 使用场景：
 * - 用户点击"生成故事"按钮
 * - 每日推荐故事生成
 * - 主题故事创作
 * 
 * @property storyRepository 故事仓库，负责实际的故事生成
 * @property profileRepository 档案仓库，获取儿童信息
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
/**
 * GenerateStoryUseCase - GenerateStory用例
 * 
 * 业务用例类，封装特定的业务操作流程
 * 
 * 用例职责：
 * - 协调多个仓库的数据操作
 * - 实现复杂的业务规则
 * - 保证业务事务的一致性
 * 
 * 设计原则：
 * - 单一职责原则
 * - 与UI层解耦
 * - 可测试性设计
 * 
 * @since 1.0.0
 */
class GenerateStoryUseCase @Inject constructor(  // 依赖注入
    private val storyRepository: StoryRepository,
    private val profileRepository: ProfileRepository
) {
    /**
     * 执行故事生成
     * 
     * 操作说明：
     * 1. 从档案库获取当前儿童信息
     * 2. 如果没有档案，使用默认值（4岁）
     * 3. 调用故事仓库生成个性化故事
     * 
     * 默认值策略：
     * - 年龄默认：4岁（中间值）
     * - 兴趣默认：空列表（生成通用故事）
     * 
     * @param 主题 故事主题，可选。如"恐龙冒险"、"太空探索"等
     * @return Result<故事> 成功返回故事对象，失败返回错误信息
     * 
     * 示例：
     * ```kotlin
     * // 生成默认主题故事
     * val result = generateStoryUseCase()
     * 
     * // 生成特定主题故事
     * val result = generateStoryUseCase("海底世界")
     * ```
     */
    suspend operator fun invoke(theme: String? = null): Result<Story> {
        // 获取儿童档案，用于个性化
        val profile = profileRepository.getProfile()
        
        // 调用仓库生成故事，没有档案时使用默认值
        return storyRepository.generateStory(
            childAge = profile?.age ?: 4,          // 默认4岁
            interests = profile?.interests ?: emptyList(),  // 默认无特定兴趣
            theme = theme                           // 可选主题
        )
    }
}