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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
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
private fun SwitchPreference(
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