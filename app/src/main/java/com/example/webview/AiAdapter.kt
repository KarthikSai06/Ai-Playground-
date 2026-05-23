package com.example.webview

interface AiAdapter {
    val platform: AiPlatform
    
    // Returns JS to insert text into the prompt box and trigger send
    fun getInjectPromptScript(prompt: String): String
    
    // Returns JS to observe DOM changes for the streaming response and send to Android
    fun getObserverScript(): String
}
