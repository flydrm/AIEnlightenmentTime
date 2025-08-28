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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.Canvas

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
        // Red Panda character implementation
        RedPandaCharacter(
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
                .rotate(rotation),
            mood = mood
        )
        
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

@Composable
private fun RedPandaCharacter(
    modifier: Modifier = Modifier,
    mood: String = "happy"
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2.5f
        
        // Red panda body (main circle)
        drawCircle(
            color = Color(0xFFD84315), // Deep orange-red for red panda
            radius = radius,
            center = center
        )
        
        // Ears
        val earRadius = radius * 0.3f
        drawCircle(
            color = Color(0xFF6D4C41), // Brown for inner ear
            radius = earRadius,
            center = androidx.compose.ui.geometry.Offset(centerX - radius * 0.6f, centerY - radius * 0.7f)
        )
        drawCircle(
            color = Color(0xFF6D4C41),
            radius = earRadius,
            center = androidx.compose.ui.geometry.Offset(centerX + radius * 0.6f, centerY - radius * 0.7f)
        )
        
        // White face patch
        drawOval(
            color = Color.White,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - radius * 0.4f, centerY - radius * 0.2f),
            size = androidx.compose.ui.geometry.Size(radius * 0.8f, radius * 0.6f)
        )
        
        // Eyes based on mood
        val eyeRadius = radius * 0.08f
        val eyeY = centerY - radius * 0.1f
        
        when (mood) {
            "happy" -> {
                // Happy eyes (curved)
                drawArc(
                    color = Color.Black,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(centerX - radius * 0.25f - eyeRadius, eyeY - eyeRadius),
                    size = androidx.compose.ui.geometry.Size(eyeRadius * 2, eyeRadius * 2)
                )
                drawArc(
                    color = Color.Black,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(centerX + radius * 0.25f - eyeRadius, eyeY - eyeRadius),
                    size = androidx.compose.ui.geometry.Size(eyeRadius * 2, eyeRadius * 2)
                )
            }
            else -> {
                // Normal eyes
                drawCircle(
                    color = Color.Black,
                    radius = eyeRadius,
                    center = androidx.compose.ui.geometry.Offset(centerX - radius * 0.25f, eyeY)
                )
                drawCircle(
                    color = Color.Black,
                    radius = eyeRadius,
                    center = androidx.compose.ui.geometry.Offset(centerX + radius * 0.25f, eyeY)
                )
            }
        }
        
        // Nose
        drawCircle(
            color = Color.Black,
            radius = radius * 0.05f,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
        
        // Mouth (smile)
        val mouthPath = Path().apply {
            moveTo(centerX - radius * 0.15f, centerY + radius * 0.1f)
            quadraticBezierTo(
                centerX, centerY + radius * 0.2f,
                centerX + radius * 0.15f, centerY + radius * 0.1f
            )
        }
        drawPath(
            path = mouthPath,
            color = Color.Black,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
    }
}