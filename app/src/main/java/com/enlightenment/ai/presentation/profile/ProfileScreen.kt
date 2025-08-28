package com.enlightenment.ai.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enlightenment.ai.presentation.theme.CreamBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的资料") },
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
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is ProfileUiState.NoProfile -> {
                    ProfileSetupForm(
                        onSaveProfile = { name, age ->
                            viewModel.createProfile(name, age)
                        }
                    )
                }
                
                is ProfileUiState.HasProfile -> {
                    ProfileContent(
                        profile = state.profile,
                        onUpdateInterests = { interests ->
                            viewModel.updateInterests(interests)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSetupForm(
    onSaveProfile: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "让我们先认识一下！",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("你的名字") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = age,
            onValueChange = { 
                if (it.all { char -> char.isDigit() }) {
                    age = it
                }
            },
            label = { Text("你的年龄") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                val ageInt = age.toIntOrNull()
                if (name.isNotBlank() && ageInt != null && ageInt in 3..6) {
                    onSaveProfile(name, ageInt)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && age.toIntOrNull() in 3..6
        ) {
            Text("开始学习之旅")
        }
    }
}

@Composable
private fun ProfileContent(
    profile: ProfileDisplayModel,
    onUpdateInterests: (List<String>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "基本信息",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("姓名")
                    Text(profile.name, style = MaterialTheme.typography.bodyLarge)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("年龄")
                    Text("${profile.age}岁", style = MaterialTheme.typography.bodyLarge)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("学习等级")
                    Text(profile.learningLevel, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "兴趣爱好",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val availableInterests = listOf(
                    "动物", "太空", "恐龙", "公主", "汽车",
                    "音乐", "画画", "运动", "科学", "故事"
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableInterests.forEach { interest ->
                        FilterChip(
                            selected = interest in profile.interests,
                            onClick = {
                                val newInterests = if (interest in profile.interests) {
                                    profile.interests - interest
                                } else {
                                    profile.interests + interest
                                }
                                onUpdateInterests(newInterests)
                            },
                            label = { Text(interest) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = "学习成就",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "已学习 ${profile.daysLearned} 天",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "听了 ${profile.storiesCompleted} 个故事",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@ExperimentalLayoutApi
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}