package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.ChatRepository
import com.example.webview.AiPlatform
import com.example.webview.WebViewManager
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val selectedTab: String = "Compare",
    val promptInput: String = "",
    val streamingResponses: Map<AiPlatform, String> = emptyMap(),
    val activePlatforms: Set<AiPlatform> = AiPlatform.values().toSet(),
    val userName: String = "Karthik"
)

class MainViewModel(
    private val chatRepository: ChatRepository
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

    init {
        WebViewManager.onResponseUpdate = { platform, text ->
            _uiState.update { state ->
                val newResponses = state.streamingResponses.toMutableMap()
                newResponses[platform] = text
                state.copy(streamingResponses = newResponses)
            }
            // Realtime update in db can be complex, skipping for scope or just update final
        }
    }

    fun updateUserName(name: String) {
        _uiState.update { it.copy(userName = name) }
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
                promptInput = ""
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
            active.forEach { platform ->
                chatRepository.saveMessage(sessionId, prompt, platform.name, "Processing...")
                // Note: The real update would save the final response, this is a conceptual demo
            }
        }
    }
}
