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

@Singleton
class StoryRepositoryImpl @Inject constructor(
    private val apiService: AIApiService,
    private val storyDao: StoryDao,
    private val gson: Gson
) : StoryRepository {
    
    override suspend fun generateStory(
        childAge: Int,
        interests: List<String>,
        theme: String?
    ): Result<Story> = withContext(Dispatchers.IO) {
        try {
            val request = StoryGenerateRequest(
                childProfile = ChildProfileRequest(
                    age = childAge,
                    interests = interests
                ),
                storyParams = StoryParamsRequest(
                    theme = theme
                )
            )
            
            val response = apiService.generateStory(request)
            
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
            
            // Save to local database
            saveStory(story)
            
            Result.success(story)
        } catch (e: Exception) {
            // Try to get from cache if network fails
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
        } catch (e: Exception) {
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