package com.enlightenment.ai.domain.usecase

import com.enlightenment.ai.domain.repository.ImageRecognitionRepository
import java.io.File
import javax.inject.Inject

/**
 * 图像识别用例
 * 
 * 功能说明：
 * 处理拍照识别功能的业务逻辑，通过AI识别图片中的物体，
 * 并返回教育性的内容，帮助儿童认识和学习周围的世界。
 * 
 * 核心价值：
 * 1. 探索式学习：鼓励儿童主动探索
 * 2. 即时反馈：快速识别并教育
 * 3. 知识扩展：提供相关知识点
 * 4. 趣味互动：增加学习乐趣
 * 
 * 业务流程：
 * 1. 接收拍摄的图片文件
 * 2. 调用AI图像识别服务
 * 3. 获取识别结果和教育内容
 * 4. 返回结构化的学习信息
 * 
 * 使用场景：
 * - 认识日常物品
 * - 学习动植物知识
 * - 探索周围环境
 * - 亲子互动学习
 * 
 * @property imageRecognitionRepository 图像识别仓库，负责调用AI服务
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
/**
 * RecognizeImageUseCase - RecognizeImage用例
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
class RecognizeImageUseCase @Inject constructor(  // 依赖注入
    private val imageRecognitionRepository: ImageRecognitionRepository
) {
    /**
     * 执行图像识别
     * 
     * 处理说明：
     * 直接调用仓库层的识别方法，保持用例层的简洁性。
     * 复杂的业务逻辑（如结果过滤、内容审核）由仓库层处理。
     * 
     * @param imageFile 要识别的图片文件
     * @return 识别结果，包含物体信息和教育内容
     * 
     * 异常处理：
     * - 文件不存在：返回失败结果
     * - 网络异常：返回离线教育内容
     * - 识别失败：返回鼓励性提示
     */
    suspend operator fun invoke(imageFile: File): Result<RecognitionResult> {
        return imageRecognitionRepository.recognizeImage(imageFile)
    }
}

/**
 * 图像识别结果
 * 
 * 数据说明：
 * 包含AI识别的物体信息和配套的教育内容。
 * 设计为儿童友好的结构，便于理解和学习。
 * 
 * 内容策略：
 * - 简单易懂：使用儿童能理解的语言
 * - 教育价值：每个识别都包含学习点
 * - 扩展学习：提供相关主题引导
 * - 趣味性强：包含有趣的小知识
 * 
 * @property objectName 识别出的物体名称，如"小狗"、"苹果"
 * @property description 物体的简单描述，适合3-6岁儿童理解
 * @property funFact 有趣的知识点，增加学习趣味性
 * @property confidence 识别置信度（0-1），用于内部判断可靠性
 * @property educationalContent 扩展的教育内容，深入学习
 * @property relatedTopics 相关主题列表，引导继续探索
 * 
 * 示例：
 * ```
 * RecognitionResult(
 *     objectName = "蝴蝶",
 *     description = "这是一只美丽的蝴蝶，它有彩色的翅膀",
 *     funFact = "蝴蝶的翅膀上有很多小鳞片，就像小瓦片一样",
 *     confidence = 0.95f,
 *     educationalContent = "蝴蝶是昆虫，它们会经历毛毛虫变蝴蝶的神奇过程",
 *     relatedTopics = ["昆虫", "变态发育", "花朵"]
 * )
 * ```
 */
/**
 * RecognitionResult - RecognitionResult
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
data class RecognitionResult(
    val objectName: String,
    val description: String,
    val funFact: String? = null,
    val confidence: Float,
    val educationalContent: String? = null,
    val relatedTopics: List<String> = emptyList()
)