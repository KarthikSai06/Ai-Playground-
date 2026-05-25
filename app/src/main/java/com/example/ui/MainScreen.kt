package com.example.ui

import android.view.View
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.viewmodel.MainViewModel
import com.example.webview.AiPlatform
import com.example.webview.WebViewManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000)
        showSplash = false
    }

    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (!showSplash) {
            if (state.showWelcomeScreen) {
                WelcomeScreen(viewModel)
            } else {
                AppContent(viewModel)
            }
        }

        AnimatedVisibility(
            visible = showSplash,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(800))
        ) {
            SplashUI()
        }
    }
}

@Composable
fun SplashUI() {
    val infiniteTransition = rememberInfiniteTransition()
    val spin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing))
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0E0C)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Omni AI Hub",
                fontSize = 48.sp,
                color = Color(0xFFF7F5F0), // Paper
                fontWeight = FontWeight.Medium,
                letterSpacing = (-1.5).sp,
                modifier = Modifier.graphicsLayer { scaleX = pulse; scaleY = pulse }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "ONE PROMPT · ALL MODELS · ONE TRUTH",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0x59F7F5F0),
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AiPlatform.values().forEach {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(it.brandColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(it.title.take(2).uppercase(), color = it.brandColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
            LinearProgressIndicator(color = Color(0x80F7F5F0), trackColor = Color(0x1AF7F5F0), modifier = Modifier.width(120.dp).height(2.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    viewModel: MainViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var currentNavTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavBar(if (currentNavTab == 5) -1 else currentNavTab) { currentNavTab = it }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().alpha(if (currentNavTab == 0) 1f else 0f)) {
                if (currentNavTab == 0) HomeTab(viewModel)
            }
            Box(modifier = Modifier.fillMaxSize().alpha(if (currentNavTab == 1) 1f else 0f)) {
                if (currentNavTab == 1) CompareTab(state.streamingResponses, state.activePlatforms)
            }
            Box(modifier = Modifier.fillMaxSize().alpha(if (currentNavTab == 2) 1f else 0f)) {
                if (currentNavTab == 2) HistoryTab(viewModel)
            }
            Box(modifier = Modifier.fillMaxSize().alpha(if (currentNavTab == 3) 1f else 0f)) {
                if (currentNavTab == 3) SettingsTab(
                    userName = state.userName,
                    onUserNameChange = { viewModel.updateUserName(it) },
                    activePlatforms = state.activePlatforms,
                    onTogglePlatform = { viewModel.togglePlatformActive(it) },
                    onNavigateToBrowser = { 
                        viewModel.onTabSelected(it.title)
                        currentNavTab = 5 
                    }
                )
            }
            Box(modifier = Modifier.fillMaxSize().alpha(if (currentNavTab == 4) 1f else 0f)) {
                if (currentNavTab == 4) ProfileTab(viewModel)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (currentNavTab == 5) 1f else 0f)
            ) {
                EnginesTab(
                    viewModel = viewModel,
                    selectedTab = state.selectedTab,
                    isVisible = currentNavTab == 5,
                    onBack = { currentNavTab = 3 }
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(currentIndex: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        Pair("Home", Icons.Default.Home),
        Pair("Compare", Icons.Default.Layers),
        Pair("History", Icons.Default.History),
        Pair("Settings", Icons.Default.Settings),
        Pair("Profile", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = Color(0xFFF7F5F0), // Paper
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(8.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        items.forEachIndexed { index, pair ->
            NavigationBarItem(
                icon = { Icon(pair.second, contentDescription = pair.first) },
                label = { Text(pair.first, fontSize = 10.sp) },
                selected = currentIndex == index,
                onClick = { onSelect(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun HomeTab(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp).systemBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("AI Aggregator", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Hello ${state.userName}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(state.userName.take(2).uppercase(), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Active Models
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ACTIVE MODELS", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            AiPlatform.values().forEach { platform ->
                val isActive = state.activePlatforms.contains(platform)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isActive) Color.White else Color.Transparent)
                        .clickable { viewModel.togglePlatformActive(platform) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(if (isActive) platform.brandColor.copy(alpha=0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(platform.title.take(2).uppercase(), color = if(isActive) platform.brandColor else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(platform.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(if (isActive) "Ready to prompt" else "Disabled", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isActive) Color(0xFF22C55E) else Color(0xFF9B9890)))
                }
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.outline)

        // Inject Prompt
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("INJECT PROMPT", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        OutlinedTextField(
            value = state.promptInput,
            onValueChange = { viewModel.onPromptChange(it) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).heightIn(min = 100.dp),
            placeholder = { Text("Type a prompt — sent to all active models simultaneously...") },
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            )
        )
        
        Button(
            onClick = { if (!state.isGenerating) viewModel.sendPrompt() },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp).fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (state.isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Synthesizing...")
            } else {
                Text("Send Prompt")
            }
        }

        // Responses Recap underneath
        if (state.streamingResponses.isNotEmpty() && state.streamingResponses.values.any { it.isNotBlank() }) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("RESPONSES", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
                state.activePlatforms.forEach { platform ->
                    val response = state.streamingResponses[platform]
                    if (!response.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(platform.brandColor.copy(0.2f)), contentAlignment = Alignment.Center) {
                                        Text(platform.title.take(1), fontSize = 10.sp, color = platform.brandColor, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(platform.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(response, fontSize = 13.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompareTab(streamingResponses: Map<AiPlatform, String>, activePlatforms: Set<AiPlatform>) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp).systemBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("SIDE-BY-SIDE", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Compare", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (activePlatforms.isEmpty()) {
            Text("No active platforms selected.", modifier = Modifier.padding(24.dp))
            return
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            activePlatforms.forEach { platform ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(platform.brandColor.copy(0.2f)), contentAlignment = Alignment.Center) {
                                Text(platform.title.take(2).uppercase(), fontSize = 10.sp, color = platform.brandColor, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(platform.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            streamingResponses[platform] ?: "Waiting for response...", 
                            fontSize = 13.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.width(8.dp).height(8.dp).clip(CircleShape).background(platform.brandColor))
                            Spacer(Modifier.width(8.dp))
                            Text("Engine Responded", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = platform.brandColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnginesTab(viewModel: MainViewModel, selectedTab: String, isVisible: Boolean, onBack: () -> Unit) {
    val tabs = AiPlatform.values().map { it.title }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.systemBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(selectedTab).takeIf { it >= 0 } ?: 0,
                edgePadding = 8.dp,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = { Text(tab, fontSize = 14.sp) }
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            val loadedPlatforms = remember { mutableStateListOf<AiPlatform>() }
            
            LaunchedEffect(Unit) {
                AiPlatform.values().forEach { platform ->
                    if (!loadedPlatforms.contains(platform)) {
                        delay(1500)
                        loadedPlatforms.add(platform)
                    }
                }
            }
            
            AiPlatform.values().forEach { platform ->
                key(platform.name) {
                    val isPlatformSelected = selectedTab == platform.title
                    if (loadedPlatforms.contains(platform)) {
                        AndroidView(
                        factory = { context ->
                            FrameLayout(context).apply {
                                val wv = WebViewManager.getWebView(context, platform)
                                (wv.parent as? FrameLayout)?.removeView(wv)
                                addView(wv)
                            }
                        },
                        update = { view ->
                            view.visibility = if (isPlatformSelected && isVisible) android.view.View.VISIBLE else android.view.View.GONE
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    )
                }
                }
            }
        }
    }
}

@Composable
fun ProfileTab(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    val stats by viewModel.stats.collectAsState(initial = Triple(0, 0, "0s"))
    val (prompts, models, avgSpeed) = stats
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 80.dp)) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFFE8E5DF)) // Light gradient substitute
                .padding(top = 48.dp, bottom = 24.dp)
                .systemBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(76.dp).clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .border(4.dp, Color.White, RoundedCornerShape(24.dp))
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.userName.take(2).uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(Modifier.height(16.dp))
                Text(state.userName, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("karthikkammala06@gmail.com", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary).padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                    Spacer(Modifier.width(8.dp))
                    Text("Pro · Active", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Prompts" to prompts.toString(), "Models" to models.toString()).forEach { (label, value) ->
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("ACTIVE ENGINES", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val favs = AiPlatform.values().toList()
            favs.forEach { platform ->
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(platform.brandColor.copy(0.2f)), contentAlignment = Alignment.Center) {
                            Text(platform.title.take(2).uppercase(), color = platform.brandColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(platform.title, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun HistoryTab(viewModel: MainViewModel) {
    val sessions by viewModel.chatSessions.collectAsState(initial = emptyList())
    val state by viewModel.uiState.collectAsState()
    val messages by viewModel.selectedHistoryMessages.collectAsState(initial = emptyList())
    
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 24.dp).systemBarsPadding()) {
        Text("ARCHIVE", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("History", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(sessions) { session ->
                val isExpanded = state.selectedHistorySessionId == session.id
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { 
                        viewModel.selectHistorySession(if (isExpanded) null else session.id) 
                    },
                    colors = CardDefaults.cardColors(containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(session.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = if (isExpanded) 10 else 1, overflow = TextOverflow.Ellipsis)
                        Text(android.text.format.DateFormat.format("MMM dd, yyyy - HH:mm", session.timestamp).toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        if (isExpanded) {
                            Spacer(Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(12.dp))
                            
                            messages.forEach { msg ->
                                val platform = AiPlatform.fromString(msg.platform)
                                if (platform != null) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                        Box(
                                            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(platform.brandColor.copy(0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(platform.title.take(2).uppercase(), color = platform.brandColor, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(platform.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(4.dp))
                                            Text(if (msg.response.isBlank()) "No response recorded" else msg.response, fontSize = 13.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                            if (messages.isEmpty()) {
                                Text("Loading details...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            if (sessions.isEmpty()) {
                item {
                    Text("No history yet.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 32.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsTab(
    userName: String,
    onUserNameChange: (String) -> Unit,
    activePlatforms: Set<AiPlatform>,
    onTogglePlatform: (AiPlatform) -> Unit,
    onNavigateToBrowser: (AiPlatform) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(top = 24.dp, bottom = 80.dp).systemBarsPadding()) {
        Text("ACCOUNT", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Profile Setup", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(Modifier.height(32.dp))
        
        Text("ENGINES", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Linked Engines", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        
        AiPlatform.values().forEach { platform ->
            val isActive = activePlatforms.contains(platform)
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(if (isActive) platform.brandColor.copy(0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(platform.title.take(2).uppercase(), color = if (isActive) platform.brandColor else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(platform.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(if (isActive) "Enabled · ${platform.title} Web" else "Not enabled", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { onTogglePlatform(platform) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.outline)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isActive) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isActive) "Session active for use" else "Enable to use in Compare", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(
                            onClick = { onNavigateToBrowser(platform) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(if (isActive) "Open Web" else "Link Now", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        Text("PREFERENCES", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(16.dp)
        ) {
        // Removed scoring engine block
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun WelcomeScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F5F0))
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to Omni AI", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Text("Please set up your profile", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(Modifier.height(48.dp))
        
        OutlinedTextField(
            value = state.userName,
            onValueChange = { viewModel.updateUserName(it) },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = state.userAge,
            onValueChange = { viewModel.updateUserAge(it) },
            label = { Text("Your Age") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(Modifier.height(32.dp))
        
        Text("Select Preferred AIs", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(AiPlatform.values()) { platform ->
                val isActive = state.activePlatforms.contains(platform)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { viewModel.togglePlatformActive(platform) },
                    colors = CardDefaults.cardColors(containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(0.3f) else Color.White),
                    border = BorderStroke(1.dp, if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isActive, onCheckedChange = { viewModel.togglePlatformActive(platform) })
                        Spacer(Modifier.width(12.dp))
                        Text(platform.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.completeOnboarding() },
            enabled = state.userName.isNotBlank() && state.activePlatforms.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
