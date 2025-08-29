package com.enlightenment.ai.presentation.camera

import com.enlightenment.ai.domain.usecase.RecognizeImageUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class CameraViewModelTest {
    
    private lateinit var recognizeImageUseCase: RecognizeImageUseCase
    private lateinit var viewModel: CameraViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        recognizeImageUseCase = mockk()
        viewModel = CameraViewModel(recognizeImageUseCase)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `recognizeImage should update state to success when use case succeeds`() = runTest {
        // Given
        val imageUri = "content://test/image.jpg"
        val expectedResult = "这是一只可爱的小猫"
        coEvery { recognizeImageUseCase(imageUri) } returns Result.success(expectedResult)
        
        // When
        viewModel.recognizeImage(imageUri)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is CameraUiState.RecognitionSuccess)
        assertEquals(expectedResult, state.result)
    }
    
    @Test
    fun `recognizeImage should update state to error when use case fails`() = runTest {
        // Given
        val imageUri = "content://test/image.jpg"
        val error = Exception("Network error")
        coEvery { recognizeImageUseCase(imageUri) } returns Result.failure(error)
        
        // When
        viewModel.recognizeImage(imageUri)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is CameraUiState.Error)
        assertEquals("识别失败，请重试", state.message)
    }
    
    @Test
    fun `resetState should return state to capturing`() {
        // Given
        viewModel.recognizeImage("test")
        
        // When
        viewModel.resetState()
        
        // Then
        assertTrue(viewModel.uiState.value is CameraUiState.Capturing)
    }
}