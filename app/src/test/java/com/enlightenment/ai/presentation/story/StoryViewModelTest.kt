package com.enlightenment.ai.presentation.story

import app.cash.turbine.test
import com.enlightenment.ai.domain.model.Question
import com.enlightenment.ai.domain.model.Story
import com.enlightenment.ai.domain.usecase.GenerateStoryUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StoryViewModelTest {
    
    private lateinit var generateStoryUseCase: GenerateStoryUseCase
    private lateinit var viewModel: StoryViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        generateStoryUseCase = mockk()
    }
    
    @Test
    fun `initial state should be loading`() = runTest {
        // Given
        coEvery { generateStoryUseCase(null) } returns Result.success(createTestStory())
        
        // When
        viewModel = StoryViewModel(generateStoryUseCase)
        
        // Then
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertThat(initialState).isInstanceOf(StoryUiState.Loading::class.java)
        }
    }
    
    @Test
    fun `should show success state when story is generated`() = runTest {
        // Given
        val story = createTestStory()
        coEvery { generateStoryUseCase(null) } returns Result.success(story)
        
        // When
        viewModel = StoryViewModel(generateStoryUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(StoryUiState.Success::class.java)
            
            val successState = state as StoryUiState.Success
            assertThat(successState.story.title).isEqualTo(story.title)
            assertThat(successState.story.content).isEqualTo(story.content)
        }
    }
    
    @Test
    fun `should show error state when story generation fails`() = runTest {
        // Given
        coEvery { generateStoryUseCase(null) } returns Result.failure(Exception("Network error"))
        
        // When
        viewModel = StoryViewModel(generateStoryUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(StoryUiState.Error::class.java)
            
            val errorState = state as StoryUiState.Error
            assertThat(errorState.message).contains("生成故事失败")
        }
    }
    
    @Test
    fun `answerQuestion should update question state correctly`() = runTest {
        // Given
        val story = createTestStory()
        coEvery { generateStoryUseCase(null) } returns Result.success(story)
        
        viewModel = StoryViewModel(generateStoryUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.answerQuestion("q1", 0) // Correct answer
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem() as StoryUiState.Success
            val question = state.story.questions.first()
            
            assertThat(question.isAnswered).isTrue()
            assertThat(question.selectedAnswer).isEqualTo(0)
            assertThat(question.feedback).contains("太棒了")
        }
    }
    
    private fun createTestStory(): Story {
        return Story(
            id = "test-story",
            title = "测试故事",
            content = "这是一个测试故事的内容",
            duration = 180,
            questions = listOf(
                Question(
                    id = "q1",
                    text = "小熊猫是什么颜色？",
                    options = listOf("红色", "黑色", "白色"),
                    correctAnswerIndex = 0,
                    explanation = "小熊猫是红色的"
                )
            )
        )
    }
}