package com.enlightenment.ai.presentation.story

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.enlightenment.ai.presentation.common.LoadingAnimation
import com.enlightenment.ai.presentation.theme.CreamBackground

/**
 * 故事界面
 * 
 * 功能说明：
 * 展示AI生成的儿童故事，支持语音朗读和互动问答。
 * 采用儿童友好的设计，大字体、配图、互动按钮。
 * 
 * UI结构：
 * 1. 顶部栏：标题和返回按钮
 * 2. 故事区域：标题、配图、正文内容
 * 3. 控制区：播放/暂停按钮、换一个按钮
 * 4. 问答区：故事相关的选择题
 * 
 * 交互功能：
 * - 语音朗读：TTS朗读故事内容
 * - 滚动阅读：支持上下滑动
 * - 互动问答：检验理解程度
 * - 重新生成：获取新故事
 * 
 * 状态处理：
 * - Loading：显示加载动画
 * - Success：展示故事内容
 * - Error：显示错误提示
 * 
 * @param onNavigateBack 返回上一页的回调
 * @param viewModel 故事界面的ViewModel
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: StoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("今日故事") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(CreamBackground)
        ) {
            when (val state = uiState) {
                is StoryUiState.Loading -> {
                    LoadingAnimation(
                        message = "小熊猫正在创作精彩的故事...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is StoryUiState.Success -> {
                    StoryContent(
                        story = state.story,
                        onPlayAudio = { viewModel.playStory() },
                        onAnswerQuestion = { questionId, answerIndex ->
                            viewModel.answerQuestion(questionId, answerIndex)
                        }
                    )
                }
                
                is StoryUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.generateStory() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun StoryContent(
    story: StoryDisplayModel,
    onPlayAudio: () -> Unit,
    onAnswerQuestion: (String, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Story image
        story.imageUrl?.let { url ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "故事插图",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Story title
        Text(
            text = story.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Story content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text(
                text = story.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Play button
        Button(
            onClick = onPlayAudio,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("🔊 听故事")
        }
        
        // Questions
        if (story.questions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "回答问题",
                style = MaterialTheme.typography.titleLarge
            )
            
            story.questions.forEach { question ->
                Spacer(modifier = Modifier.height(16.dp))
                QuestionCard(
                    question = question,
                    onAnswerSelected = { answerIndex ->
                        onAnswerQuestion(question.id, answerIndex)
                    }
                )
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: QuestionDisplayModel,
    onAnswerSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = question.text,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            question.options.forEachIndexed { index, option ->
                OutlinedButton(
                    onClick = { onAnswerSelected(index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    enabled = !question.isAnswered
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "${index + 1}. $option",
                            color = if (question.isAnswered && index == question.selectedAnswer) {
                                if (index == question.correctAnswerIndex) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
            
            if (question.isAnswered && question.feedback != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = question.feedback,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "😔",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}