package com.enlightenment.ai.presentation.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enlightenment.ai.presentation.theme.CreamBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentLoginScreen(
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: ParentLoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState) {
        if (uiState is ParentLoginUiState.Success) {
            onLoginSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("家长验证") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "请输入家长密码",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "为了保护孩子的隐私和安全，请验证您的身份",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Math challenge
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "验证问题：12 + 7 = ?",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("答案") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            visualTransformation = if (showPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) {
                                            Icons.Default.Visibility
                                        } else {
                                            Icons.Default.VisibilityOff
                                        },
                                        contentDescription = if (showPassword) "隐藏答案" else "显示答案"
                                    )
                                }
                            },
                            isError = uiState is ParentLoginUiState.Error,
                            singleLine = true
                        )
                        
                        if (uiState is ParentLoginUiState.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { viewModel.verifyPassword(password) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = password.isNotBlank() && uiState !is ParentLoginUiState.Loading
                        ) {
                            if (uiState is ParentLoginUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("验证")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TextButton(onClick = { /* TODO: Forgot password */ }) {
                            Text("忘记密码？")
                        }
                    }
                }
            }
        }
    }
}