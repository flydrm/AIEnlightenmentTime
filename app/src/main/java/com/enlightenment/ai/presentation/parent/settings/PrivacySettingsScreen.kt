package com.enlightenment.ai.presentation.parent.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 隐私设置界面
 * 
 * 功能说明：
 * 隐私和数据保护设置。
 * 管理数据收集、分享权限、账户安全等。
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
fun PrivacySettingsScreen(  // 可组合UI组件
    onNavigateBack: () -> Unit,
    viewModel: PrivacySettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("隐私设置") },
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
            // 数据收集设置
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "数据收集",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SwitchPreference(
                        title = "收集使用数据",
                        description = "帮助我们改进应用体验",
                        checked = uiState.collectUsageData,
                        onCheckedChange = { viewModel.toggleUsageDataCollection() }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    SwitchPreference(
                        title = "个性化推荐",
                        description = "基于孩子的学习习惯推荐内容",
                        checked = uiState.personalizedRecommendations,
                        onCheckedChange = { viewModel.togglePersonalizedRecommendations() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 数据存储设置
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "数据存储",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SwitchPreference(
                        title = "本地数据备份",
                        description = "自动备份学习进度到设备",
                        checked = uiState.localBackup,
                        onCheckedChange = { viewModel.toggleLocalBackup() }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    SwitchPreference(
                        title = "云端同步",
                        description = "同步学习数据到云端（加密存储）",
                        checked = uiState.cloudSync,
                        onCheckedChange = { viewModel.toggleCloudSync() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 权限管理
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "权限管理",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SwitchPreference(
                        title = "相机权限",
                        description = "允许使用相机进行图像识别",
                        checked = uiState.cameraPermission,
                        onCheckedChange = { viewModel.toggleCameraPermission() }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    SwitchPreference(
                        title = "麦克风权限",
                        description = "允许语音交互功能",
                        checked = uiState.microphonePermission,
                        onCheckedChange = { viewModel.toggleMicrophonePermission() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 数据管理操作
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "数据管理",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { viewModel.clearCache() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("清除缓存")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { viewModel.deleteAllData() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("删除所有数据")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 隐私政策链接
            TextButton(
                onClick = { viewModel.openPrivacyPolicy() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("查看隐私政策")
            }
        }
    }
}

@Composable
private fun SwitchPreference(  // 可组合UI组件
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}