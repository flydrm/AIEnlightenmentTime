package com.enlightenment.ai.domain.usecase

import com.enlightenment.ai.domain.model.ChildProfile
import com.enlightenment.ai.domain.model.LearningLevel
import com.enlightenment.ai.domain.model.Story
import com.enlightenment.ai.domain.repository.ProfileRepository
import com.enlightenment.ai.domain.repository.StoryRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GenerateStoryUseCaseTest {
    
    private lateinit var storyRepository: StoryRepository
    private lateinit var profileRepository: ProfileRepository
    private lateinit var generateStoryUseCase: GenerateStoryUseCase
    
    @Before
    fun setup() {
        storyRepository = mockk()
        profileRepository = mockk()
        generateStoryUseCase = GenerateStoryUseCase(storyRepository, profileRepository)
    }
    
    @Test
    fun `invoke should use profile data when available`() = runTest {
        // Given
        val profile = ChildProfile(
            id = "test-id",
            name = "小明",
            age = 5,
            interests = listOf("恐龙", "太空")
        )
        val story = Story(
            id = "story-1",
            title = "恐龙太空冒险",
            content = "很久很久以前...",
            duration = 180
        )
        
        coEvery { profileRepository.getProfile() } returns profile
        coEvery { 
            storyRepository.generateStory(
                childAge = 5,
                interests = listOf("恐龙", "太空"),
                theme = null
            )
        } returns Result.success(story)
        
        // When
        val result = generateStoryUseCase()
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(story)
        
        coVerify {
            profileRepository.getProfile()
            storyRepository.generateStory(5, listOf("恐龙", "太空"), null)
        }
    }
    
    @Test
    fun `invoke should use default values when profile is null`() = runTest {
        // Given
        val story = Story(
            id = "story-1",
            title = "默认故事",
            content = "从前有座山...",
            duration = 180
        )
        
        coEvery { profileRepository.getProfile() } returns null
        coEvery { 
            storyRepository.generateStory(
                childAge = 4,
                interests = emptyList(),
                theme = "friendship"
            )
        } returns Result.success(story)
        
        // When
        val result = generateStoryUseCase(theme = "friendship")
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(story)
        
        coVerify {
            profileRepository.getProfile()
            storyRepository.generateStory(4, emptyList(), "friendship")
        }
    }
    
    @Test
    fun `invoke should propagate failure from repository`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { profileRepository.getProfile() } returns null
        coEvery { 
            storyRepository.generateStory(any(), any(), any())
        } returns Result.failure(exception)
        
        // When
        val result = generateStoryUseCase()
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }
}