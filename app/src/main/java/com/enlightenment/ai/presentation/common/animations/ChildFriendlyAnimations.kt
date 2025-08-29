package com.enlightenment.ai.presentation.common.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset

/**
 * 儿童友好的动画效果集合
 */
object ChildFriendlyAnimations {
    
    /**
     * 弹跳进入动画
     */
    @Composable
    fun BounceIn(
        visible: Boolean,
        content: @Composable AnimatedVisibilityScope.() -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                initialScale = 0.3f
            ) + fadeIn(),
            exit = scaleOut(targetScale = 0.3f) + fadeOut()
        ) {
            content()
        }
    }
    
    /**
     * 摇摆动画
     */
    @Composable
    fun Wiggle(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "wiggle")
        val rotation by infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wiggle_rotation"
        )
        
        Box(
            modifier = modifier.graphicsLayer {
                rotationZ = rotation
                transformOrigin = TransformOrigin(0.5f, 1f)
            }
        ) {
            content()
        }
    }
    
    /**
     * 脉冲动画
     */
    @Composable
    fun Pulse(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
        
        Box(
            modifier = modifier.scale(scale)
        ) {
            content()
        }
    }
    
    /**
     * 彩虹出现动画
     */
    @Composable
    fun RainbowAppear(
        visible: Boolean,
        content: @Composable AnimatedVisibilityScope.() -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessVeryLow
                )
            ) + expandVertically(
                expandFrom = Alignment.Top
            ) + fadeIn(),
            exit = slideOutVertically() + shrinkVertically() + fadeOut()
        ) {
            content()
        }
    }
    
    /**
     * 星星闪烁效果
     */
    @Composable
    fun Sparkle(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 2000
                    0.3f at 0
                    1f at 500
                    0.3f at 1000
                    1f at 1500
                    0.3f at 2000
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "sparkle_alpha"
        )
        
        Box(
            modifier = modifier.graphicsLayer {
                this.alpha = alpha
            }
        ) {
            content()
        }
    }
    
    /**
     * 漂浮动画
     */
    @Composable
    fun Float(
        modifier: Modifier = Modifier,
        floatRange: Float = 10f,
        content: @Composable () -> Unit
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "float")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = floatRange,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "float_offset"
        )
        
        Box(
            modifier = modifier.offset { IntOffset(0, offsetY.toInt()) }
        ) {
            content()
        }
    }
    
    /**
     * 成功庆祝动画
     */
    @Composable
    fun CelebrationBounce(
        trigger: Boolean,
        content: @Composable () -> Unit
    ) {
        val scale = remember { Animatable(1f) }
        
        LaunchedEffect(trigger) {
            if (trigger) {
                scale.animateTo(
                    targetValue = 1.3f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                )
            }
        }
        
        Box(
            modifier = Modifier.scale(scale.value)
        ) {
            content()
        }
    }
    
    /**
     * 页面切换动画
     */
    fun pageEnterTransition(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(500))
    }
    
    fun pageExitTransition(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(500, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(500))
    }
}

/**
 * 动画时长常量（毫秒）
 */
object AnimationDuration {
    const val SHORT = 200
    const val MEDIUM = 500
    const val LONG = 1000
    const val EXTRA_LONG = 2000
}

/**
 * 缓动函数
 */
val ChildFriendlyEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)