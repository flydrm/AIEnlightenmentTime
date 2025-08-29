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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 内容偏好设置界面
 * 
 * 功能说明：
 * 内容偏好和过滤设置。
 * 选择合适的内容类型、难度级别、主题偏好等。
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
fun ContentPreferencesScreen(  // 可组合UI组件
    onNavigateBack: () -> Unit,
    viewModel: ContentPreferencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("内容偏好设置") },
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
            // 故事主题偏好
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "故事主题",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    listOf(
                        "动物世界" to "animal",
                        "科学探索" to "science",
                        "童话故事" to "fairy_tale",
                        "日常生活" to "daily_life",
                        "历史文化" to "history"
                    ).forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.selectedThemes.contains(value),
                                onCheckedChange = { viewModel.toggleTheme(value) }
                            )
                            Text(
                                text = label,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 难度设置
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "内容难度",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("当前难度")
                        Text(
                            text = when(uiState.difficultyLevel) {
                                0 -> "简单"
                                1 -> "适中"
                                2 -> "挑战"
                                else -> "适中"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Slider(
                        value = uiState.difficultyLevel.toFloat(),
                        onValueChange = { viewModel.updateDifficulty(it.toInt()) },
                        valueRange = 0f..2f,
                        steps = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 内容过滤
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "内容过滤",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("过滤敏感内容")
                        Switch(
                            checked = uiState.filterSensitiveContent,
                            onCheckedChange = { viewModel.toggleSensitiveFilter() }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("避免恐怖元素")
                        Switch(
                            checked = uiState.avoidScaryContent,
                            onCheckedChange = { viewModel.toggleScaryFilter() }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { 
                    viewModel.saveSettings()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存设置")
            }
        }
    }
}