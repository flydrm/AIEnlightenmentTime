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
import android.app.TimePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Show time picker dialog when needed
    if (uiState.showTimePickerDialog) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, uiState.timePickerHour)
        calendar.set(Calendar.MINUTE, uiState.timePickerMinute)
        
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                viewModel.setReminderTime(hourOfDay, minute)
                viewModel.dismissTimePicker()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // Use 24-hour format
        ).apply {
            setOnCancelListener {
                viewModel.dismissTimePicker()
            }
        }.show()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("应用设置") },
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
            // 显示设置
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "显示设置",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("字体大小")
                        Text(
                            text = when(uiState.fontSize) {
                                0 -> "小"
                                1 -> "中"
                                2 -> "大"
                                else -> "中"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Slider(
                        value = uiState.fontSize.toFloat(),
                        onValueChange = { viewModel.updateFontSize(it.toInt()) },
                        valueRange = 0f..2f,
                        steps = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("护眼模式")
                        Switch(
                            checked = uiState.eyeProtectionMode,
                            onCheckedChange = { viewModel.toggleEyeProtection() }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 声音设置
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "声音设置",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("背景音乐")
                        Switch(
                            checked = uiState.backgroundMusic,
                            onCheckedChange = { viewModel.toggleBackgroundMusic() }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("音效")
                        Switch(
                            checked = uiState.soundEffects,
                            onCheckedChange = { viewModel.toggleSoundEffects() }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("语音速度")
                    Slider(
                        value = uiState.speechRate,
                        onValueChange = { viewModel.updateSpeechRate(it) },
                        valueRange = 0.5f..1.5f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("慢", style = MaterialTheme.typography.bodySmall)
                        Text("正常", style = MaterialTheme.typography.bodySmall)
                        Text("快", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 通知设置
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "通知设置",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("学习提醒")
                        Switch(
                            checked = uiState.learningReminders,
                            onCheckedChange = { viewModel.toggleLearningReminders() }
                        )
                    }
                    
                    if (uiState.learningReminders) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = { viewModel.showTimePicker() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("设置提醒时间: ${uiState.reminderTime}")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 语言设置
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "语言设置",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    RadioButtonGroup(
                        options = listOf("简体中文", "繁體中文", "English"),
                        selectedOption = uiState.language,
                        onOptionSelected = { viewModel.updateLanguage(it) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 关于
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "关于",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("版本")
                        Text(
                            text = uiState.appVersion,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextButton(
                        onClick = { viewModel.checkForUpdates() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isCheckingUpdate
                    ) {
                        if (uiState.isCheckingUpdate) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("检查更新")
                        }
                    }
                    
                    if (uiState.updateCheckMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.updateCheckMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.hasUpdate) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun RadioButtonGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    options.forEach { option ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = option == selectedOption,
                onClick = { onOptionSelected(option) }
            )
            Text(
                text = option,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}