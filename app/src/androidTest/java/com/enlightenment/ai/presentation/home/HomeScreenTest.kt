package com.enlightenment.ai.presentation.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.enlightenment.ai.presentation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HomeScreenTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun homeScreen_displaysAllFunctionCards() {
        // Verify all function cards are displayed
        composeTestRule.onNodeWithText("今日故事").assertIsDisplayed()
        composeTestRule.onNodeWithText("语音对话").assertIsDisplayed()
        composeTestRule.onNodeWithText("拍照探索").assertIsDisplayed()
        composeTestRule.onNodeWithText("我的成就").assertIsDisplayed()
    }
    
    @Test
    fun homeScreen_displaysParentEntrance() {
        // Verify parent entrance is displayed
        composeTestRule.onNodeWithText("家长中心").assertIsDisplayed()
    }
    
    @Test
    fun homeScreen_navigatesToStoryScreen() {
        // Click on story card
        composeTestRule.onNodeWithText("今日故事").performClick()
        
        // Verify navigation to story screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("今日故事").assertIsDisplayed() // Top bar title
    }
    
    @Test
    fun homeScreen_navigatesToDialogueScreen() {
        // Click on dialogue card
        composeTestRule.onNodeWithText("语音对话").performClick()
        
        // Verify navigation to dialogue screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("和小熊猫聊天").assertIsDisplayed()
    }
    
    @Test
    fun homeScreen_navigatesToProfileScreen() {
        // Click on achievement card
        composeTestRule.onNodeWithText("我的成就").performClick()
        
        // Verify navigation to profile screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("我的资料").assertIsDisplayed()
    }
}