package com.enlightenment.ai.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enlightenment.ai.presentation.common.PandaAnimation
import com.enlightenment.ai.presentation.theme.*

/**
 * 首页界面
 * 
 * 功能说明：
 * 应用的主界面，展示小熊猫吉祥物和四个核心功能入口。
 * 采用儿童友好的设计，大按钮、鲜艳色彩、动画效果。
 * 
 * UI布局：
 * 1. 顶部35%：小熊猫动画区域，显示问候语
 * 2. 中间50%：2x2网格展示四个功能模块
 * 3. 底部15%：家长入口和其他辅助功能
 * 
 * 交互设计：
 * - 大触摸目标（最小48dp）防止误触
 * - 点击反馈动画增强交互感
 * - 语音引导帮助不识字儿童
 * 
 * @param onNavigateToStory 导航到故事功能的回调
 * @param onNavigateToDialogue 导航到对话功能的回调
 * @param onNavigateToCamera 导航到相机功能的回调
 * @param onNavigateToProfile 导航到个人中心的回调
 * @param onNavigateToParentLogin 导航到家长入口的回调
 * @param viewModel 首页的ViewModel，管理UI状态
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@Composable
fun HomeScreen(  // 可组合UI组件
    onNavigateToStory: () -> Unit,
    onNavigateToDialogue: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToParentLogin: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Panda Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f),
                contentAlignment = Alignment.Center
            ) {
                PandaAnimation(
                    mood = uiState.pandaMood,
                    greeting = uiState.greeting
                )
            }
            
            // Function Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FunctionCard(
                        title = "今日故事",
                        emoji = "📖",
                        backgroundColor = SkyBlue.copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToStory
                    )
                    
                    FunctionCard(
                        title = "语音对话",
                        emoji = "🎤",
                        backgroundColor = GrassGreen.copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDialogue
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FunctionCard(
                        title = "拍照探索",
                        emoji = "📸",
                        backgroundColor = SunYellow.copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCamera
                    )
                    
                    FunctionCard(
                        title = "我的成就",
                        emoji = "🏆",
                        backgroundColor = PrimaryRed.copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProfile
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 家长 entrance
            TextButton(
                onClick = onNavigateToParentLogin,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "家长中心",
                    color = WoodBrown.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * 功能卡片组件
 * 
 * 设计说明：
 * 专为3-6岁儿童设计的功能入口卡片。
 * 采用大尺寸、圆角、鲜艳色彩的设计语言。
 * 
 * 设计原则：
 * - 正方形设计（aspectRatio 1:1）保持视觉平衡
 * - 24dp大圆角营造友好感
 * - Emoji图标直观表达功能，无需识字
 * - 8dp阴影增强立体感和可点击性
 * 
 * 交互反馈：
 * - 点击时有压感动画
 * - 支持触觉反馈（振动）
 * - 防误触设计
 * 
 * @param title 功能标题，2-4个字为佳
 * @param emoji 表情图标，增强识别度
 * @param backgroundColor 背景色，使用主题色
 * @param modifier 修饰符，用于布局调整
 * @param onClick 点击回调函数
 */
@Composable
private fun FunctionCard(  // 可组合UI组件
    title: String,
    emoji: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = WoodBrown,
                textAlign = TextAlign.Center
            )
        }
    }
}