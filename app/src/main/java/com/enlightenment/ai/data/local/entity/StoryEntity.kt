package com.enlightenment.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 数据实体 - 故事表
 * 
 * 数据说明：
 * Room数据库中存储故事信息的实体类。
 * 对应数据库中的stories表。
 * 
 * 字段说明：
 * @property id 故事唯一标识（主键）
 * @property title 故事标题
 * @property content 故事正文内容
 * @property imageUrl 配图URL，可为空
 * @property duration 阅读时长（秒）
 * @property questions 问题列表（JSON格式）
 * @property createdAt 创建时间戳
 * @property lastPlayedAt 最后播放时间
 * @property playCount 播放次数
 * @property childAge 适合年龄
 * 
 * @实体 Room实体标注，指定表名
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */
@Entity(tableName = "stories")
/**
 * StoryEntity - Story实体
 * 
 * Room数据库实体类，对应本地数据库表结构
 * 
 * 数据持久化策略：
 * - 支持离线数据缓存
 * - 自动数据同步
 * - 版本迁移兼容
 * 
 * @since 1.0.0
 */
data class StoryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val duration: Int,
    val questions: String, // JSON
    val createdAt: Long,
    val childAge: Int?,
    val lastPlayedAt: Long? = null,
    val playCount: Int = 0
)

/**
 * Room类型转换器
 * 
 * 功能说明：
 * 处理Room不直接支持的数据类型转换。
 * 实现复杂类型与基础类型之间的序列化和反序列化。
 * 
 * 转换策略：
 * - List<QuestionEntity>：使用Gson转换为JSON字符串存储
 * - JSON字符串：反序列化为List<QuestionEntity>对象
 * - 保持数据完整性和类型安全
 * 
 * 使用场景：
 * - 存储故事的问题列表
 * - 从数据库读取问题数据
 * - 支持复杂对象的持久化
 * 
 * @TypeConverters Room类型转换器标注
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
/**
 * Converters - Converters
 * 
 * 本地数据存储组件，提供离线数据支持
 * 
 * @自版本 1.0.0
 */
class Converters {
    private val gson = Gson()
    
    /**
     * 将问题列表转换为JSON字符串
     * 
     * @param questions 问题实体列表
     * @return JSON格式的字符串
     */
    @TypeConverter
    fun fromQuestionList(questions: List<QuestionEntity>): String {
        return gson.toJson(questions)
    }
    
    /**
     * 将JSON字符串转换为问题列表
     * 
     * @param json JSON格式的问题数据
     * @return 问题实体列表，解析失败返回空列表
     */
    @TypeConverter
    fun toQuestionList(json: String): List<QuestionEntity> {
        val type = object : TypeToken<List<QuestionEntity>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}

/**
 * 问题实体
 * 
 * 数据说明：
 * 故事配套问题的数据结构，用于Room数据库存储。
 * 每个故事包含多个问题，用于检验儿童的理解程度。
 * 
 * @property id 问题唯一标识
 * @property text 问题内容
 * @property options 选项列表
 * @property correctAnswerIndex 正确答案索引（0开始）
 * @property explanation 答案解释，可选
 */
/**
 * QuestionEntity - Question实体
 * 
 * Room数据库实体类，对应本地数据库表结构
 * 
 * 数据持久化策略：
 * - 支持离线数据缓存
 * - 自动数据同步
 * - 版本迁移兼容
 * 
 * @since 1.0.0
 */
data class QuestionEntity(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String?
)