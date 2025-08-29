package com.enlightenment.ai.presentation.dialogue

import com.enlightenment.ai.domain.model.Dialogue
import com.enlightenment.ai.domain.repository.DialogueRepository
import com.enlightenment.ai.domain.usecase.SendDialogueMessageUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class DialogueViewModelTest {
    
    private lateinit var sendDialogueMessageUseCase: SendDialogueMessageUseCase
    private lateinit var dialogueRepository: DialogueRepository
    private lateinit var viewModel: DialogueViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sendDialogueMessageUseCase = mockk()
        dialogueRepository = mockk()
        viewModel = DialogueViewModel(sendDialogueMessageUseCase, dialogueRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `sendMessage should add user and AI messages when successful`() = runTest {
        // Given
        val userMessage = "什么是太阳？"
        val aiResponse = "太阳是一颗恒星，它给地球带来光和热。"
        val mockDialogue = Dialogue(
            messages = listOf(
                Dialogue.Message("user", userMessage, System.currentTimeMillis()),
                Dialogue.Message("ai", aiResponse, System.currentTimeMillis())
            )
        )
        
        every { dialogueRepository.getConversationHistory() } returns flowOf(emptyList())
        coEvery { sendDialogueMessageUseCase(userMessage) } returns Result.success(aiResponse)
        
        // When
        viewModel.sendMessage(userMessage)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.messages.size)
        assertEquals(userMessage, state.messages[0].content)
        assertEquals(aiResponse, state.messages[1].content)
    }
    
    @Test
    fun `sendMessage should show error when use case fails`() = runTest {
        // Given
        val userMessage = "测试消息"
        val error = Exception("Network error")
        
        every { dialogueRepository.getConversationHistory() } returns flowOf(emptyList())
        coEvery { sendDialogueMessageUseCase(userMessage) } returns Result.failure(error)
        
        // When
        viewModel.sendMessage(userMessage)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.messages.any { it.content == "抱歉，我遇到了一些问题。请稍后再试。" })
    }
    
    @Test
    fun `clearConversation should reset messages`() = runTest {
        // Given
        every { dialogueRepository.getConversationHistory() } returns flowOf(emptyList())
        coEvery { dialogueRepository.clearConversation() } returns Unit
        
        // When
        viewModel.clearConversation()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.messages.isEmpty())
    }
}