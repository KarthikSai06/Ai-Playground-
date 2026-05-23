package com.example.webview

import android.webkit.JavascriptInterface

class WebAppInterface(private val onResponseUpdateCallback: (String, String, Boolean) -> Unit) {
    
    @JavascriptInterface
    fun onResponseUpdate(platformName: String, text: String, isComplete: Boolean) {
        onResponseUpdateCallback(platformName, text, isComplete)
    }
}
