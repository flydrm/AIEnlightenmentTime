package com.enlightenment.ai.presentation.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
// import com.enlightenment.ai.R // Removed as we're using placeholder UI

@Composable
fun PandaAnimation(
    mood: String,
    greeting: String? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "panda")
    
    // Breathing animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Gentle swaying
    val rotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Panda placeholder (in real app, use Lottie or custom drawable)
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
                .rotate(rotation),
            contentAlignment = Alignment.Center
        ) {
            // Panda body (placeholder)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = Color(0xFFE53935), // Red panda color
                        shape = CircleShape
                    )
            ) {
                // Eyes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .background(Color.Black, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(30.dp))
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .background(Color.Black, CircleShape)
                    )
                }
                
                // Smile
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 20.dp)
                        .width(40.dp)
                        .height(20.dp)
                        .background(
                            Color.Black,
                            RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                        )
                )
            }
        }
        
        // Greeting bubble
        greeting?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.padding(horizontal = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}