package com.enlightenment.ai.domain.model

/**
 * 儿童档案实体
 * 
 * 功能说明：
 * 存储儿童的个人信息和学习偏好，用于AI个性化教育内容生成。
 * 系统会根据档案信息调整故事难度、对话风格和学习内容。
 * 
 * 核心作用：
 * 1. 个性化内容推荐
 * 2. 学习进度跟踪
 * 3. 智能难度调整
 * 4. AI伴侣性格定制
 * 
 * 隐私保护：
 * - 所有信息本地存储
 * - 不上传敏感信息
 * - 家长可随时删除
 * 
 * @property id 档案唯一标识
 * @property name 儿童姓名，用于个性化称呼
 * @property age 年龄（3-6岁），决定内容难度
 * @property gender 性别，可选，用于故事角色选择
 * @property interests 兴趣爱好，用于主题推荐
 * @property learningLevel 学习水平，动态调整难度
 * @property preferredLearningStyle 学习风格偏好
 * @property recentTopics 最近学习主题，避免重复
 * @property companionPersonality AI伴侣性格设置
 */
data class ChildProfile(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String? = null,
    val interests: List<String> = emptyList(),
    val learningLevel: LearningLevel = LearningLevel.NORMAL,
    val preferredLearningStyle: LearningStyle = LearningStyle.MIXED,
    val recentTopics: List<String> = emptyList(),
    val companionPersonality: CompanionPersonality = CompanionPersonality()
)

/**
 * 学习水平枚举
 * 
 * 说明：
 * 根据儿童的认知能力和学习进度划分等级。
 * AI会根据等级调整内容难度和互动方式。
 * 
 * 等级说明：
 * - BEGINNER: 初学者，刚接触学习，需要更多引导
 * - NORMAL: 正常水平，适合大部分同龄儿童
 * - ADVANCED: 进阶水平，可以接受更复杂的内容
 */
enum class LearningLevel {
    BEGINNER,    // 初学者
    NORMAL,      // 正常水平
    ADVANCED     // 进阶水平
}

/**
 * 学习风格枚举
 * 
 * 说明：
 * 基于教育心理学的VARK模型，识别儿童的学习偏好。
 * 用于优化内容呈现方式，提高学习效率。
 * 
 * 风格特点：
 * - VISUAL: 视觉型，喜欢图片、动画、色彩
 * - AUDITORY: 听觉型，喜欢语音、音乐、韵律
 * - KINESTHETIC: 动觉型，喜欢互动、操作、体验
 * - MIXED: 混合型，多种风格结合
 */
enum class LearningStyle {
    VISUAL,      // 视觉型学习者
    AUDITORY,    // 听觉型学习者
    KINESTHETIC, // 动觉型学习者
    MIXED        // 混合型学习者
}

/**
 * AI伴侣性格配置
 * 
 * 功能说明：
 * 定义AI小熊猫的性格特征和交流风格。
 * 通过个性化设置，让AI伴侣更贴合儿童喜好。
 * 
 * 设计理念：
 * - 温暖友好的形象
 * - 鼓励式的教育方式
 * - 耐心细致的引导
 * 
 * @property name AI伴侣名称，默认"小熊猫"
 * @property traits 性格特征列表，如友好、鼓励、耐心
 * @property speechStyle 说话风格，温柔亲切型
 */
data class CompanionPersonality(
    val name: String = "小熊猫",
    val traits: List<String> = listOf("friendly", "encouraging", "patient"),
    val speechStyle: String = "warm_and_gentle"
)