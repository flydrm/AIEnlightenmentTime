package com.enlightenment.ai.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.enlightenment.ai.presentation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UserFlowTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun test_story_generation_flow() {
        // 1. 验证主页显示
        composeRule.onNodeWithText("AI启蒙时光").assertIsDisplayed()
        
        // 2. 点击生成故事
        composeRule.onNodeWithText("生成故事").performClick()
        
        // 3. 等待故事生成（可能显示加载状态）
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithText("正在生成故事").fetchSemanticsNodes().isEmpty()
        }
        
        // 4. 验证故事内容显示
        composeRule.onNodeWithTag("story_content").assertIsDisplayed()
    }
    
    @Test
    fun test_dialogue_interaction() {
        // 1. 导航到对话界面
        composeRule.onNodeWithText("智能对话").performClick()
        
        // 2. 输入消息
        composeRule.onNodeWithTag("message_input").performTextInput("太阳为什么是热的？")
        
        // 3. 发送消息
        composeRule.onNodeWithContentDescription("发送").performClick()
        
        // 4. 验证消息显示
        composeRule.onNodeWithText("太阳为什么是热的？").assertIsDisplayed()
        
        // 5. 等待AI回复
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag("ai_message").fetchSemanticsNodes().isNotEmpty()
        }
    }
    
    @Test
    fun test_parent_login_flow() {
        // 1. 导航到家长中心
        composeRule.onNodeWithContentDescription("家长中心").performClick()
        
        // 2. 验证需要登录
        composeRule.onNodeWithText("家长验证").assertIsDisplayed()
        
        // 3. 输入错误答案
        composeRule.onNodeWithTag("answer_input").performTextInput("10")
        composeRule.onNodeWithText("确认").performClick()
        
        // 4. 验证错误提示
        composeRule.onNodeWithText("答案不正确").assertIsDisplayed()
        
        // 5. 输入正确答案
        composeRule.onNodeWithTag("answer_input").performTextClearance()
        composeRule.onNodeWithTag("answer_input").performTextInput("19")
        composeRule.onNodeWithText("确认").performClick()
        
        // 6. 验证进入家长中心
        composeRule.onNodeWithText("家长中心").assertIsDisplayed()
    }
    
    @Test
    fun test_camera_permission_flow() {
        // 1. 导航到拍照识别
        composeRule.onNodeWithText("拍照识别").performClick()
        
        // 2. 验证权限请求（如果需要）
        if (composeRule.onAllNodesWithText("需要相机权限").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("允许").performClick()
        }
        
        // 3. 验证相机界面显示
        composeRule.onNodeWithTag("camera_preview").assertIsDisplayed()
        
        // 4. 验证拍照按钮
        composeRule.onNodeWithContentDescription("拍照").assertIsDisplayed()
    }
}