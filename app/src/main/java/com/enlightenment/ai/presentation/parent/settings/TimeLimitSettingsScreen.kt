package com.enlightenment.ai.presentation.parent.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeLimitSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TimeLimitSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("使用时长设置") },
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
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "每日学习时长",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("当前设置")
                        Text(
                            text = "${uiState.dailyLimitMinutes}分钟",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Slider(
                        value = uiState.dailyLimitMinutes.toFloat(),
                        onValueChange = { viewModel.updateDailyLimit(it.toInt()) },
                        valueRange = 10f..60f,
                        steps = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10分钟", style = MaterialTheme.typography.bodySmall)
                        Text("60分钟", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "休息提醒",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("启用休息提醒")
                        Switch(
                            checked = uiState.breakReminderEnabled,
                            onCheckedChange = { viewModel.toggleBreakReminder() }
                        )
                    }
                    
                    if (uiState.breakReminderEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("提醒间隔")
                            Text(
                                text = "${uiState.breakIntervalMinutes}分钟",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Slider(
                            value = uiState.breakIntervalMinutes.toFloat(),
                            onValueChange = { viewModel.updateBreakInterval(it.toInt()) },
                            valueRange = 5f..30f,
                            steps = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("睡眠时间保护")
                    Switch(
                        checked = uiState.sleepProtectionEnabled,
                        onCheckedChange = { viewModel.toggleSleepProtection() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
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