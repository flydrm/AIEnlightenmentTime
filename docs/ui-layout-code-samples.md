# UI布局方案 - 代码示例

## 方案一：经典卡片式布局 - Compose实现

```kotlin
@Composable
fun ClassicCardHomeScreen(
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmThemeColors.CreamBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部小熊猫区域
            RedPandaSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
            )
            
            // 功能卡片网格
            FunctionCardGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .weight(1f),
                onCardClick = onNavigate
            )
            
            // 底部导航
            ParentCenterBar(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FunctionCardGrid(
    modifier: Modifier = Modifier,
    onCardClick: (String) -> Unit
) {
    val cards = listOf(
        CardData("story", "今日故事", "📖", Color(0xFF64B5F6)),
        CardData("voice", "语音对话", "🎤", Color(0xFF81C784)),
        CardData("camera", "拍照探索", "📸", Color(0xFFFFD54F)),
        CardData("achievement", "我的成就", "🏆", Color(0xFFFFB300))
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(cards) { card ->
            FunctionCard(
                cardData = card,
                onClick = { onCardClick(card.route) }
            )
        }
    }
}

@Composable
private fun FunctionCard(
    cardData: CardData,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardData.backgroundColor.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = cardData.emoji,
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = cardData.title,
                style = MaterialTheme.typography.headlineSmall,
                color = WarmThemeColors.WoodBrown,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

---

## 方案二：探险地图式布局 - Compose实现

```kotlin
@Composable
fun AdventureMapHomeScreen(
    onNavigate: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 背景层
        MapBackground()
        
        // 场景层
        Box(modifier = Modifier.fillMaxSize()) {
            // 故事城堡
            SceneBuilding(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 50.dp, y = 120.dp),
                icon = "🏰",
                label = "故事城堡",
                onClick = { onNavigate("story") }
            )
            
            // 对话帐篷
            SceneBuilding(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-50).dp, y = 120.dp),
                icon = "🎪",
                label = "对话帐篷",
                onClick = { onNavigate("voice") }
            )
            
            // 探索森林
            SceneBuilding(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 50.dp, y = (-150).dp),
                icon = "🌲",
                label = "探索森林",
                onClick = { onNavigate("camera") }
            )
            
            // 成就山峰
            SceneBuilding(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-50).dp, y = (-150).dp),
                icon = "⛰️",
                label = "成就山峰",
                onClick = { onNavigate("achievement") }
            )
            
            // 中心小熊猫
            AnimatedPanda(
                modifier = Modifier.align(Alignment.Center)
            )
            
            // 路径
            MapPaths(modifier = Modifier.fillMaxSize())
        }
        
        // 底部家长中心
        HomeButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun MapBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // 天空渐变
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF87CEEB),
                    Color(0xFFB3E5FC)
                ),
                endY = size.height * 0.6f
            )
        )
        
        // 草地
        drawRect(
            color = Color(0xFF8BC34A),
            topLeft = Offset(0f, size.height * 0.6f),
            size = Size(size.width, size.height * 0.4f)
        )
    }
    
    // 云朵动画
    CloudAnimation()
}

@Composable
private fun SceneBuilding(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.1f else 1f,
        animationSpec = spring()
    )
    
    Column(
        modifier = modifier
            .scale(scale)
            .clickable { onClick() }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHovered = true
                        tryAwaitRelease()
                        isHovered = false
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 光晕效果
        if (isHovered) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        Text(
            text = icon,
            fontSize = 64.sp,
            modifier = Modifier.graphicsLayer {
                shadowElevation = if (isHovered) 16f else 8f
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(
                    color = WarmThemeColors.PrimaryRed,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
```

---

## 方案三：故事书式布局 - Compose实现

```kotlin
@Composable
fun StoryBookHomeScreen(
    onNavigate: (String) -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF5DEB3), // 纸张色
                        Color(0xFFFAF0E6)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 书本标题
            BookTitle()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 书页内容
            BookPages(
                currentPage = currentPage,
                onNavigate = onNavigate,
                modifier = Modifier.weight(1f)
            )
            
            // 翻页控制
            PageControls(
                currentPage = currentPage,
                totalPages = 3,
                onPageChange = { currentPage = it }
            )
        }
    }
}

@Composable
private fun BookPages(
    currentPage: Int,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = currentPage,
        modifier = modifier,
        transitionSpec = {
            if (targetState > initialState) {
                // 向右翻页
                slideInHorizontally { width -> width } + fadeIn() with
                slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                // 向左翻页
                slideInHorizontally { width -> -width } + fadeIn() with
                slideOutHorizontally { width -> width } + fadeOut()
            }
        }
    ) { page ->
        when (page) {
            0 -> FirstPageWithPanda(onNavigate)
            1 -> ChapterGrid(
                chapters = listOf(
                    ChapterData("voice", "第二章", "语音对话", "🎤"),
                    ChapterData("camera", "第三章", "拍照探索", "📸")
                ),
                onNavigate = onNavigate
            )
            2 -> ChapterGrid(
                chapters = listOf(
                    ChapterData("achievement", "第四章", "我的成就", "🏆"),
                    ChapterData("more", "第五章", "更多惊喜", "✨")
                ),
                onNavigate = onNavigate
            )
        }
    }
}

@Composable
private fun FirstPageWithPanda(
    onNavigate: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .bookPageStyle()
    ) {
        // 左页 - 第一章
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "第一章",
                style = MaterialTheme.typography.headlineMedium,
                color = WarmThemeColors.WoodBrown
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "今日故事",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = WarmThemeColors.PrimaryRed
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "📖",
                fontSize = 80.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { onNavigate("story") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmThemeColors.PrimaryRed
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(width = 150.dp, height = 56.dp)
            ) {
                Text("开始阅读", fontSize = 18.sp)
            }
        }
        
        // 中间分割线
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp),
            color = Color.Gray.copy(alpha = 0.3f)
        )
        
        // 右页 - 小熊猫
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RedPandaIllustration()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "嗨！我是红色小熊猫\n让我们一起开始\n今天的学习之旅吧！",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp,
                    color = WarmThemeColors.WoodBrown
                )
            }
        }
    }
}

// 书页样式修饰符
fun Modifier.bookPageStyle() = this
    .clip(RoundedCornerShape(8.dp))
    .background(Color.White)
    .shadow(
        elevation = 8.dp,
        shape = RoundedCornerShape(8.dp)
    )
    .border(
        width = 1.dp,
        color = Color.Gray.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    )
```

---

## 响应式布局处理

```kotlin
@Composable
fun AdaptiveLayout(
    content: @Composable (LayoutConfig) -> Unit
) {
    val configuration = LocalConfiguration.current
    val windowSizeClass = calculateWindowSizeClass()
    
    val layoutConfig = when {
        // 手机竖屏
        windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact &&
        configuration.orientation == Configuration.ORIENTATION_PORTRAIT -> {
            LayoutConfig.PhonePortrait
        }
        
        // 手机横屏
        windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact &&
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE -> {
            LayoutConfig.PhoneLandscape
        }
        
        // 平板竖屏
        windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium &&
        configuration.orientation == Configuration.ORIENTATION_PORTRAIT -> {
            LayoutConfig.TabletPortrait
        }
        
        // 平板横屏
        else -> LayoutConfig.TabletLandscape
    }
    
    content(layoutConfig)
}

sealed class LayoutConfig {
    object PhonePortrait : LayoutConfig()
    object PhoneLandscape : LayoutConfig()
    object TabletPortrait : LayoutConfig()
    object TabletLandscape : LayoutConfig()
}
```