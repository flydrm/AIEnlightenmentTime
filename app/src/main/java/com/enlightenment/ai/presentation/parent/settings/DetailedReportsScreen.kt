package com.enlightenment.ai.presentation.parent.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.format.DateTimeFormatter

/**
 * 详细报告界面
 * 
 * 功能说明：
 * 展示详细的学习报告和数据分析。
 * 包含学习时长、完成内容、进步趋势等信息。
 * 
 * UI布局：
 * 1. 顶部栏：标题和导航按钮
 * 2. 内容区域：主要功能展示
 * 3. 操作区域：用户交互控件
 * 
 * 交互设计：
 * - 响应式布局：适配不同屏幕尺寸
 * - 即时反馈：操作后立即显示结果
 * - 错误处理：友好的错误提示
 * - 加载状态：异步操作时显示进度
 * 
 * 用户体验优化：
 * - 简洁明了的界面设计
 * - 符合Material Design规范
 * - 支持手势操作
 * - 无障碍支持
 * 
 * 技术特点：
 * - Jetpack Compose声明式UI
 * - StateFlow状态管理
 * - 协程处理异步操作
 * - MVVM架构模式
 * 
 * @param onNavigateBack 返回导航回调
 * @param viewModel 界面对应的ViewModel
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedReportsScreen(  // 可组合UI组件
    onNavigateBack: () -> Unit,
    viewModel: DetailedReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("学习报告") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 总览卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "学习总览",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            label = "总学习天数",
                            value = "${uiState.totalDays}天",
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatItem(
                            label = "总故事数",
                            value = "${uiState.totalStories}个",
                            color = MaterialTheme.colorScheme.secondary
                        )
                        StatItem(
                            label = "总学习时长",
                            value = "${uiState.totalMinutes}分钟",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 本周进度
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "本周学习进度",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    uiState.weeklyProgress.forEach { dayProgress ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayProgress.dayName,
                                modifier = Modifier.width(60.dp)
                            )
                            
                            LinearProgressIndicator(
                                progress = { dayProgress.progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .padding(horizontal = 8.dp),
                                color = if (dayProgress.progress >= 1f) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.secondary
                            )
                            
                            Text(
                                text = "${dayProgress.minutes}分钟",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 学习兴趣分析
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "兴趣分析",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    uiState.interestAnalysis.forEach { interest ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = interest.category)
                            Text(
                                text = "${interest.percentage}%",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 成就徽章
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "获得成就",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        uiState.achievements.forEach { achievement ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Card(
                                    modifier = Modifier.size(60.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (achievement.unlocked)
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        else
                                            Color.Gray.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = achievement.icon,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                }
                                Text(
                                    text = achievement.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 导出按钮
            OutlinedButton(
                onClick = { viewModel.exportReport() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("导出完整报告")
            }
        }
    }
}

@Composable
private fun StatItem(  // 可组合UI组件
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}