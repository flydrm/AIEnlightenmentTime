package com.enlightenment.ai.presentation.home

import app.cash.turbine.test
import com.enlightenment.ai.domain.model.ChildProfile
import com.enlightenment.ai.domain.repository.ProfileRepository
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
class HomeViewModelTest {
    
    private lateinit var profileRepository: ProfileRepository
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        profileRepository = mockk()
    }
    
    @Test
    fun `initial state should show default greeting`() = runTest {
        // Given
        coEvery { profileRepository.getProfile() } returns null
        
        // When
        viewModel = HomeViewModel(profileRepository)
        
        // Then
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertThat(initialState.greeting).isEqualTo("嗨，小朋友！")
            assertThat(initialState.pandaMood).isEqualTo("happy")
            assertThat(initialState.isLoading).isFalse()
        }
    }
    
    @Test
    fun `should show personalized greeting when profile exists`() = runTest {
        // Given
        val profile = ChildProfile(
            id = "test-id",
            name = "小明",
            age = 5
        )
        coEvery { profileRepository.getProfile() } returns profile
        
        // When
        viewModel = HomeViewModel(profileRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.greeting).isEqualTo("嗨，小明！今天想学什么呢？")
            assertThat(state.pandaMood).isEqualTo("happy")
        }
    }
}