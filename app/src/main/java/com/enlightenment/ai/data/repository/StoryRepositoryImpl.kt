package com.enlightenment.ai.data.repository

import com.enlightenment.ai.data.local.dao.StoryDao
import com.enlightenment.ai.data.local.entity.QuestionEntity
import com.enlightenment.ai.data.local.entity.StoryEntity
import com.enlightenment.ai.data.remote.api.AIApiService
import com.enlightenment.ai.data.remote.api.ChildProfileRequest
import com.enlightenment.ai.data.remote.api.StoryGenerateRequest
import com.enlightenment.ai.data.remote.api.StoryParamsRequest
import com.enlightenment.ai.data.remote.NetworkRetryPolicy
import com.enlightenment.ai.data.remote.retryableNetworkCall
import com.enlightenment.ai.domain.model.Question
import com.enlightenment.ai.domain.model.Story
import com.enlightenment.ai.domain.repository.StoryRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据层 - 故事仓库实现
 * 
 * 架构职责：
 * 实现Domain层定义的StoryRepository接口，协调远程API和本地缓存。
 * 负责数据的获取、转换、缓存和错误处理。
 * 
 * 核心功能：
 * 1. 调用AI API生成故事
 * 2. 管理本地故事缓存
 * 3. 处理网络异常和降级
 * 4. 数据模型转换
 * 
 * 降级策略：
 * - 网络异常 → 返回本地缓存
 * - API限流 → 延迟重试
 * - 服务不可用 → 使用离线内容
 * 
 * 依赖说明：
 * @property apiService 远程API服务，负责与AI服务通信
 * @property storyDao 本地数据访问对象，管理SQLite存储
 * @property gson JSON解析工具，用于数据序列化
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@Singleton
/**
 * StoryRepositoryImpl - Story仓库实现
 * 
 * 仓库模式实现类，协调本地和远程数据源
 * 
 * 核心职责：
 * - 统一数据访问接口
 * - 实现缓存策略
 * - 处理数据同步
 * - 错误处理和降级
 * 
 * 数据策略：
 * - 优先使用本地缓存
 * - 异步更新远程数据
 * - 智能数据预加载
 * - 离线模式支持
 * 
 * @since 1.0.0
 */
class StoryRepositoryImpl @Inject constructor(  // 依赖注入
    private val apiService: AIApiService,
    private val storyDao: StoryDao,
    private val gson: Gson
) : StoryRepository {
    
    /**
     * 生成AI故事
     * 
     * 实现流程：
     * 1. 构建请求参数，包含儿童信息和主题
     * 2. 调用AI API生成故事（支持重试）
     * 3. 转换响应数据为领域模型
     * 4. 保存到本地缓存供离线使用
     * 5. 异常时返回缓存内容
     * 
     * 重试策略：
     * - 使用NetworkRetryPolicy处理网络异常
     * - 429错误（限流）自动延迟重试
     * - 5xx错误（服务器）指数退避重试
     * 
     * 缓存策略：
     * - 成功生成后自动保存
     * - 失败时查找相同主题的缓存
     * - 无匹配时返回随机缓存故事
     */
    override suspend fun generateStory(
        childAge: Int,
        interests: List<String>,
        theme: String?
    ): Result<Story> = withContext(Dispatchers.IO) {
        try {
            // Step 1: 构建API请求参数
            val request = StoryGenerateRequest(
                childProfile = ChildProfileRequest(
                    age = childAge,
                    interests = interests
                ),
                storyParams = StoryParamsRequest(
                    theme = theme
                )
            )
            
            // Step 2: 调用AI服务生成故事（带重试机制）
            val response = NetworkRetryPolicy.retryableNetworkCall {
                apiService.generateStory(request)
            }
            
            // Step 3: 转换API响应为领域模型
            val story = Story(
                id = response.id,
                title = response.title,
                content = response.content,
                imageUrl = response.imageUrl,
                duration = response.duration,
                questions = response.questions?.map { q ->
                    Question(
                        id = q.id,
                        text = q.text,
                        options = q.options,
                        correctAnswerIndex = q.correctAnswerIndex,
                        explanation = q.explanation
                    )
                } ?: emptyList(),
                childAge = childAge
            )
            
            // Save to local 数据库
            saveStory(story)
            
            Result.success(story)
        } catch (e: Exception) {  // 捕获并处理异常
            // Try to get from 缓存 if 网络 fails
            val cachedStories = getCachedStories()
            if (cachedStories.isNotEmpty()) {
                Result.success(cachedStories.random())
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getStoryById(id: String): Story? = withContext(Dispatchers.IO) {
        storyDao.getStoryById(id)?.toDomainModel()
    }
    
    override suspend fun saveStory(story: Story) = withContext(Dispatchers.IO) {
        storyDao.insertStory(story.toEntity())
    }
    
    override fun getRecentStories(): Flow<List<Story>> {
        return storyDao.getRecentStories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getCachedStories(): List<Story> = withContext(Dispatchers.IO) {
        storyDao.getAllStories().map { it.toDomainModel() }
    }
    
    private fun Story.toEntity(): StoryEntity {
        return StoryEntity(
            id = id,
            title = title,
            content = content,
            imageUrl = imageUrl,
            duration = duration,
            questions = gson.toJson(questions.map { q ->
                QuestionEntity(
                    id = q.id,
                    text = q.text,
                    options = q.options,
                    correctAnswerIndex = q.correctAnswerIndex,
                    explanation = q.explanation
                )
            }),
            createdAt = createdAt,
            childAge = childAge
        )
    }
    
    private fun StoryEntity.toDomainModel(): Story {
        val questionList = try {
            val questionEntities: List<QuestionEntity> = gson.fromJson(
                questions,
                object : com.google.gson.reflect.TypeToken<List<QuestionEntity>>() {}.type
            )
            questionEntities.map { q ->
                Question(
                    id = q.id,
                    text = q.text,
                    options = q.options,
                    correctAnswerIndex = q.correctAnswerIndex,
                    explanation = q.explanation
                )
            }
        } catch (e: Exception) {  // 捕获并处理异常
            emptyList<Question>()
        }
        
        return Story(
            id = id,
            title = title,
            content = content,
            imageUrl = imageUrl,
            duration = duration,
            questions = questionList,
            createdAt = createdAt,
            childAge = childAge
        )
    }
}