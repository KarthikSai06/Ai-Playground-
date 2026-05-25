package com.example.ui

import android.app.Activity
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.draw.scale
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
import com.example.viewmodel.UiState
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
    val spin = remember { Animatable(0f) }
    val mergePhase = remember { Animatable(0f) }
    val iconScale = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            spin.animateTo(
                targetValue = 1080f,
                animationSpec = tween(1500, easing = EaseInOutCubic)
            )
        }
        launch {
            delay(1100)
            mergePhase.animateTo(
                targetValue = 1f,
                animationSpec = tween(400, easing = EaseInCubic)
            )
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(400, easing = LinearEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F5F0)), // Light theme background
        contentAlignment = Alignment.Center
    ) {
        val centerOffset = 60.dp
        val colors = listOf(
            Color(0xFF00E5FF), // Cyan
            Color(0xFFFF1744), // Red
            Color(0xFFFFC107), // Yellow
            Color(0xFF00E676)  // Green
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                // The emerging icon
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(iconScale.value)
                        .alpha(iconScale.value.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.ic_launcher_background),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    Image(
                        painter = painterResource(id = com.example.R.drawable.ic_launcher_foreground),
                        contentDescription = "App Icon",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // The spinning dots
                if (iconScale.value < 0.2f) {
                    for (i in 0 until 4) {
                        val angle = Math.toRadians((spin.value + i * 90 - 90).toDouble())
                        val distance = (1f - mergePhase.value) * centerOffset.value
                        
                        val offsetX = distance * kotlin.math.cos(angle).toFloat()
                        val offsetY = distance * kotlin.math.sin(angle).toFloat()
                        
                        Box(
                            modifier = Modifier
                                .offset(offsetX.dp, offsetY.dp)
                                .size(24.dp)
                                .alpha((1f - iconScale.value * 5f).coerceIn(0f, 1f))
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(colors[i])
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Omni AI Hub",
                fontSize = 32.sp,
                color = Color(0xFF0F0E0C),
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.alpha(textAlpha.value.coerceIn(0f, 1f))
            )
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
    
    val loadedPlatforms = remember { mutableStateListOf<AiPlatform>() }
    LaunchedEffect(state.activePlatforms) {
        state.activePlatforms.forEach { platform ->
            if (!loadedPlatforms.contains(platform)) {
                delay(1000)
                loadedPlatforms.add(platform)
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(if (currentNavTab == 5) -1 else currentNavTab) { currentNavTab = it }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (currentNavTab != 5) {
                Box(modifier = Modifier.size(1.dp).alpha(0.01f)) {
                    loadedPlatforms.forEach { platform ->
                        if (state.activePlatforms.contains(platform)) {
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { context ->
                                    android.widget.FrameLayout(context).apply {
                                        val wv = com.example.webview.WebViewManager.getWebView(context, platform)
                                        (wv.parent as? android.view.ViewGroup)?.removeView(wv)
                                        addView(wv)
                                    }
                                },
                                update = { frame ->
                                    val wv = com.example.webview.WebViewManager.getWebView(frame.context, platform)
                                    if (wv.parent != frame) {
                                        (wv.parent as? android.view.ViewGroup)?.removeView(wv)
                                        frame.addView(wv)
                                    }
                                },
                                modifier = Modifier.size(1.dp)
                            )
                        }
                    }
                }
            }

            AnimatedContent(targetState = currentNavTab, label = "tabAnim", transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }) { tab ->
                when (tab) {
                    0 -> HomeTab(viewModel)
                    1 -> CompareTab(state.streamingResponses, state.activePlatforms)
                    2 -> HistoryTab(viewModel)
                    3 -> SettingsTab(
                        userName = state.userName,
                        onUserNameChange = { viewModel.updateUserName(it) },
                        userEmail = state.userEmail,
                        onUserEmailChange = { viewModel.updateUserEmail(it) },
                        activePlatforms = state.activePlatforms,
                        onTogglePlatform = { viewModel.togglePlatformActive(it) },
                        onNavigateToBrowser = { 
                            viewModel.onTabSelected(it.title)
                            currentNavTab = 5 
                        }
                    )
                    4 -> ProfileTab(viewModel)
                    5 -> EnginesTab(
                        viewModel = viewModel,
                        selectedTab = state.selectedTab,
                        isVisible = currentNavTab == 5,
                        onBack = { currentNavTab = 3 }
                    )
                }
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
    
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (!spokenText.isNullOrEmpty()) {
                viewModel.onPromptChange(state.promptInput + (if (state.promptInput.isEmpty()) "" else " ") + spokenText)
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp).systemBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Omni AI Hub", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                var helloText by remember { mutableStateOf("") }
                val targetText = "Hello ${state.userName}"
                LaunchedEffect(targetText) {
                    helloText = ""
                    for (i in targetText.indices) {
                        delay(60)
                        helloText = targetText.take(i + 1)
                    }
                }
                val infiniteTransition = rememberInfiniteTransition()
                val cursorAlpha by infiniteTransition.animateFloat(
                    initialValue = 0f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(animation = tween(500), repeatMode = RepeatMode.Reverse)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(helloText, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("|", fontSize = 28.sp, fontWeight = FontWeight.Light, modifier = Modifier.alpha(cursorAlpha))
                }
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

        val activePlatformsList = AiPlatform.values().toList()
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            for (i in activePlatformsList.indices step 2) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val p1 = activePlatformsList[i]
                    Box(modifier = Modifier.weight(1f)) { ActiveModelBox(p1, state, viewModel) }
                    if (i + 1 < activePlatformsList.size) {
                        val p2 = activePlatformsList[i + 1]
                        Box(modifier = Modifier.weight(1f)) { ActiveModelBox(p2, state, viewModel) }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
        
        androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.outline)

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
            trailingIcon = {
                val context = LocalContext.current
                Icon(
                    androidx.compose.material.icons.Icons.Default.Mic, 
                    contentDescription = "Mic Input", 
                    modifier = Modifier.padding(16.dp).clickable {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            }
                            speechLauncher.launch(intent)
                        } catch (e: android.content.ActivityNotFoundException) {
                            android.widget.Toast.makeText(context, "Speech recognition is not available on this device.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            },
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
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("RESPONSES", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { viewModel.clearResponses() }) { Text("Clear All", fontSize = 12.sp) }
            }
            
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 80.dp)) {
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
        } else {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun CompareTab(streamingResponses: Map<AiPlatform, String>, activePlatforms: Set<AiPlatform>) {
    val context = LocalContext.current
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
            IconButton(onClick = {
                val shareText = streamingResponses.entries.joinToString("\n\n") { "${it.key.title}:\n${it.value}" }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Omni AI Hub Responses:\n\n$shareText")
                }
                context.startActivity(Intent.createChooser(intent, "Share Responses"))
            }) {
                Icon(androidx.compose.material.icons.Icons.Default.Share, contentDescription = "Share")
            }
        }

        if (activePlatforms.isEmpty()) {
            Text("No active platforms selected.", modifier = Modifier.padding(24.dp))
            return
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp).padding(bottom = 80.dp)) {
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
                Text(state.userEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTab(viewModel: MainViewModel) {
    val sessions by viewModel.chatSessions.collectAsState(initial = emptyList())
    val state by viewModel.uiState.collectAsState()
    val messages by viewModel.selectedHistoryMessages.collectAsState(initial = emptyList())
    
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 24.dp).systemBarsPadding()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("ARCHIVE", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("History", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            if (sessions.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearHistory() }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(sessions, key = { it.id }) { session ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                            viewModel.deleteHistorySession(session.id)
                            true
                        } else false
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(Modifier.fillMaxSize().padding(bottom = 8.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.error).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    },
                    content = {
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
                )
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
    userEmail: String,
    onUserEmailChange: (String) -> Unit,
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
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = userEmail,
            onValueChange = onUserEmailChange,
            label = { Text("Your Email") },
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
fun ActiveModelBox(platform: AiPlatform, state: UiState, viewModel: MainViewModel) {
    val isActive = state.activePlatforms.contains(platform)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) Color.White else Color.Transparent)
            .clickable { viewModel.togglePlatformActive(platform) }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(if (isActive) platform.brandColor.copy(alpha=0.2f) else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(platform.title.take(2).uppercase(), color = if(isActive) platform.brandColor else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text(platform.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(if (isActive) "Ready" else "Disabled", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isActive) Color(0xFF22C55E) else Color(0xFF9B9890)))
    }
}

@Composable
fun SetupModelBox(platform: AiPlatform, state: UiState, viewModel: MainViewModel) {
    val isActive = state.activePlatforms.contains(platform)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { viewModel.togglePlatformActive(platform) },
        colors = CardDefaults.cardColors(containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(0.3f) else Color.White),
        border = BorderStroke(1.dp, if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(if (isActive) platform.brandColor.copy(0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(platform.title.take(2).uppercase(), color = if (isActive) platform.brandColor else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(platform.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
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
        
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = state.userEmail,
            onValueChange = { viewModel.updateUserEmail(it) },
            label = { Text("Your Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(Modifier.height(32.dp))
        
        Text("Select Preferred AIs", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.height(16.dp))
        
        val platforms = AiPlatform.values().toList()
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            for (i in platforms.indices step 2) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val p1 = platforms[i]
                    Box(modifier = Modifier.weight(1f)) { SetupModelBox(p1, state, viewModel) }
                    if (i + 1 < platforms.size) {
                        val p2 = platforms[i + 1]
                        Box(modifier = Modifier.weight(1f)) { SetupModelBox(p2, state, viewModel) }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(8.dp))
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
