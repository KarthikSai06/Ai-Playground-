package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.ChatMessage
import com.example.repository.ChatRepository
import com.example.webview.AiPlatform
import com.example.webview.WebViewManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

import android.content.SharedPreferences

data class UiState(
    val showWelcomeScreen: Boolean = true,
    val selectedTab: String = "Compare",
    val promptInput: String = "",
    val streamingResponses: Map<AiPlatform, String> = emptyMap(),
    val activePlatforms: Set<AiPlatform> = AiPlatform.values().toSet(),
    val userName: String = "",
    val userAge: String = "",
    val isGenerating: Boolean = false,
    val selectedHistorySessionId: Long? = null
)

class MainViewModel(
    private val chatRepository: ChatRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val chatSessions = chatRepository.allSessions

    val stats = chatSessions.map { sessions ->
        val prompts = sessions.size
        val models = _uiState.value.activePlatforms.size
        val avgSpeed = "1.8s" // Placeholder as speed tracking isn't fully implemented in webviews
        Triple(prompts, models, avgSpeed)
    }

    private var debounceJob: Job? = null
    private var currentSessionId: Long? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedHistoryMessages: Flow<List<ChatMessage>> = _uiState
        .map { it.selectedHistorySessionId }
        .flatMapLatest { id ->
            if (id != null) chatRepository.getMessages(id) else flowOf(emptyList())
        }

    init {
        val isFirstTime = prefs.getBoolean("isFirstTime", true)
        val savedName = prefs.getString("userName", "") ?: ""
        val savedAge = prefs.getString("userAge", "") ?: ""
        val savedPlatformsStr = prefs.getStringSet("activePlatforms", null)
        
        val loadedActivePlatforms = if (savedPlatformsStr != null) {
            savedPlatformsStr.mapNotNull { AiPlatform.fromString(it) }.toSet()
        } else {
            AiPlatform.values().toSet()
        }

        _uiState.update { it.copy(
            showWelcomeScreen = isFirstTime,
            userName = savedName,
            userAge = savedAge,
            activePlatforms = loadedActivePlatforms
        ) }

        WebViewManager.onResponseUpdate = { platform, text ->
            _uiState.update { state ->
                val newResponses = state.streamingResponses.toMutableMap()
                newResponses[platform] = text
                state.copy(streamingResponses = newResponses)
            }
            
            // Check for timeout to mark complete
            if (_uiState.value.isGenerating) {
                debounceJob?.cancel()
                debounceJob = viewModelScope.launch {
                    delay(3000)
                    finishGeneration()
                }
            }
        }
    }

    private fun finishGeneration() {
        _uiState.update { it.copy(isGenerating = false) }
        val sessionId = currentSessionId ?: return
        val currentResponses = _uiState.value.streamingResponses
        
        viewModelScope.launch {
            currentResponses.forEach { (platform, response) ->
                chatRepository.updateMessageResponse(sessionId, platform.name, response)
            }
        }
    }

    fun selectHistorySession(id: Long?) {
        _uiState.update { it.copy(selectedHistorySessionId = id) }
    }

    fun updateUserName(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun updateUserAge(age: String) {
        _uiState.update { it.copy(userAge = age) }
    }
    
    fun completeOnboarding() {
        _uiState.update { it.copy(showWelcomeScreen = false) }
        val state = _uiState.value
        prefs.edit()
            .putBoolean("isFirstTime", false)
            .putString("userName", state.userName)
            .putString("userAge", state.userAge)
            .putStringSet("activePlatforms", state.activePlatforms.map { it.name }.toSet())
            .apply()
    }

    fun onTabSelected(tab: String) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onPromptChange(text: String) {
        _uiState.update { it.copy(promptInput = text) }
    }

    fun togglePlatformActive(platform: AiPlatform) {
        _uiState.update { state ->
            val set = state.activePlatforms.toMutableSet()
            if (set.contains(platform)) set.remove(platform) else set.add(platform)
            state.copy(activePlatforms = set)
        }
    }

    fun sendPrompt() {
        val prompt = _uiState.value.promptInput
        if (prompt.isBlank()) return

        val active = _uiState.value.activePlatforms
        // Clear previous streams for active platforms
        _uiState.update { state ->
            val newResponses = state.streamingResponses.toMutableMap()
            active.forEach { p -> newResponses[p] = "" }
            state.copy(
                streamingResponses = newResponses,
                selectedTab = "Compare",
                promptInput = "",
                isGenerating = true
            )
        }

        // Trigger WebView Injection
        active.forEach { platform ->
            WebViewManager.currentActivityContext?.let { ctx ->
                WebViewManager.sendPrompt(ctx, platform, prompt)
            }
        }

        // Save to DB History
        viewModelScope.launch {
            val sessionId = chatRepository.saveSession(prompt.take(30) + "...")
            currentSessionId = sessionId
            active.forEach { platform ->
                chatRepository.saveMessage(sessionId, prompt, platform.name, "Processing...")
            }
            // fallback timeout just in case no responses come
            debounceJob?.cancel()
            debounceJob = launch {
                delay(12000)
                if (_uiState.value.isGenerating) finishGeneration()
            }
        }
    }
}
