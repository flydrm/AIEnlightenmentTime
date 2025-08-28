package com.enlightenment.ai.presentation.dialogue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enlightenment.ai.presentation.theme.CreamBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogueScreen(
    onNavigateBack: () -> Unit,
    viewModel: DialogueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("和小熊猫聊天") },
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
                .background(CreamBackground)
        ) {
            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = scrollState,
                reverseLayout = true,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.messages.reversed(),
                    key = { it.id }
                ) { message ->
                    MessageBubble(
                        message = message,
                        isFromUser = message.isFromUser
                    )
                }
            }
            
            // Quick replies
            if (uiState.suggestedReplies.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.suggestedReplies.forEach { reply ->
                        SuggestionChip(
                            onClick = {
                                inputText = reply
                                viewModel.sendMessage(reply)
                                coroutineScope.launch {
                                    scrollState.animateScrollToItem(0)
                                }
                            },
                            label = { Text(reply) }
                        )
                    }
                }
            }
            
            // Input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("说点什么...") },
                    shape = RoundedCornerShape(24.dp),
                    enabled = !uiState.isLoading,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                FilledIconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText.trim())
                            inputText = ""
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(0)
                            }
                        }
                    },
                    enabled = inputText.isNotBlank() && !uiState.isLoading
                ) {
                    Icon(Icons.Default.Send, contentDescription = "发送")
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageDisplayModel,
    isFromUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isFromUser) 16.dp else 4.dp,
                        bottomEnd = if (isFromUser) 4.dp else 16.dp
                    )
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (isFromUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (!isFromUser && message.emotion != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getEmotionEmoji(message.emotion),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

private fun getEmotionEmoji(emotion: String): String {
    return when (emotion) {
        "happy" -> "😊"
        "thoughtful" -> "🤔"
        "encouraging" -> "💪"
        "excited" -> "🎉"
        else -> "🐼"
    }
}