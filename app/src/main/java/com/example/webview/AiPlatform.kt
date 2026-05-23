package com.example.webview

import androidx.compose.ui.graphics.Color

enum class AiPlatform(val title: String, val brandColor: Color, val homeUrl: String) {
    CHATGPT("ChatGPT", Color(0xFF10A37F), "https://chatgpt.com/"),
    GEMINI("Gemini", Color(0xFF4285F4), "https://gemini.google.com/app"),
    CLAUDE("Claude", Color(0xFFD97757), "https://claude.ai/new"),
    PERPLEXITY("Perplexity", Color(0xFF20B8CD), "https://www.perplexity.ai/");

    companion object {
        fun fromString(title: String): AiPlatform? {
            return values().find { it.title.equals(title, ignoreCase = true) || it.name.equals(title, ignoreCase = true) }
        }
    }
}
