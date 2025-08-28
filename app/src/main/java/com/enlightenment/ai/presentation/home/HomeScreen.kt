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

@Composable
fun HomeScreen(
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
            
            // Parent entrance
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

@Composable
private fun FunctionCard(
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