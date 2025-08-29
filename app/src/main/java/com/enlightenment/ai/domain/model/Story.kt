package com.enlightenment.ai.domain.model

import java.util.UUID

/**
 * 领域层 - 故事实体
 * 
 * 功能说明：
 * 代表一个AI生成的儿童故事，包含故事内容和配套的教育问题。
 * 用于3-6岁儿童的互动学习，通过故事激发想象力和思维能力。
 * 
 * 业务规则：
 * 1. 故事内容需要适合目标年龄段
 * 2. 每个故事必须包含至少3个互动问题
 * 3. 故事时长控制在5-10分钟阅读时间
 * 
 * 使用场景：
 * - 故事列表展示
 * - 故事详情页播放
 * - 问答互动环节
 * - 学习进度追踪
 * 
 * @property id 故事唯一标识，用于缓存和历史记录
 * @property title 故事标题，展示在列表中，建议10字以内
 * @property content 故事正文，300-500字的儿童故事内容
 * @property imageUrl 配图URL，可选，用于增强视觉体验
 * @property duration 预计阅读时长（秒），用于时间管理
 * @property questions 配套问题列表，用于检验理解程度
 * @property createdAt 创建时间戳，用于排序和统计
 * @property childAge 适合年龄，用于内容个性化推荐
 * @property personalizedElements 个性化元素，增强故事代入感
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
data class Story(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val imageUrl: String? = null,
    val duration: Int, // in seconds
    val questions: List<Question> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val childAge: Int? = null,
    val personalizedElements: PersonalizedElements? = null
)

/**
 * 互动问题实体
 * 
 * 功能说明：
 * 用于检验儿童对故事内容的理解程度，通过选择题形式进行互动。
 * 题目设计符合儿童认知水平，注重启发思考而非死记硬背。
 * 
 * 设计原则：
 * - 问题简单明了，避免歧义
 * - 选项设置合理，避免过于相似
 * - 提供友好的解释，鼓励式教育
 * 
 * @property id 问题唯一标识
 * @property text 问题内容，建议20字以内
 * @property options 选项列表，通常3-4个选项
 * @property correctAnswerIndex 正确答案索引（0开始）
 * @property explanation 答案解释，用于教育引导
 */
data class Question(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String? = null
)

/**
 * 个性化元素
 * 
 * 功能说明：
 * 存储儿童的个人信息，用于生成个性化的故事内容。
 * 通过融入熟悉的元素，增强故事的代入感和吸引力。
 * 
 * 应用场景：
 * - 在故事中提及孩子的名字
 * - 根据兴趣生成相关主题
 * - 加入喜欢的颜色描述
 * - 引入家庭成员角色
 * 
 * @property childName 儿童姓名，用于故事主角命名
 * @property interests 兴趣爱好列表，如"恐龙"、"公主"等
 * @property favoriteColor 喜欢的颜色，用于场景描述
 * @property familyMembers 家庭成员，可作为故事配角
 */
data class PersonalizedElements(
    val childName: String? = null,
    val interests: List<String> = emptyList(),
    val favoriteColor: String? = null,
    val familyMembers: List<String> = emptyList()
)