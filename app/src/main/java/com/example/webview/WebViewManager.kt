package com.example.webview

import kotlinx.coroutines.launch
import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

@SuppressLint("SetJavaScriptEnabled", "StaticFieldLeak")
object WebViewManager {

    private val webViews = mutableMapOf<AiPlatform, WebView>()
    private val adapters = mapOf(
        AiPlatform.CHATGPT to ChatGptAdapter(),
        AiPlatform.GEMINI to GeminiAdapter(),
        AiPlatform.CLAUDE to ClaudeAdapter(),
        AiPlatform.PERPLEXITY to PerplexityAdapter()
    )

    var onResponseUpdate: ((AiPlatform, String) -> Unit)? = null
    var currentActivityContext: Context? = null
    private val activeDialogs = mutableListOf<android.app.AlertDialog>()

    init {
        // We will initialize CookieManager when getWebView is called for the first time
    }

    fun getWebView(context: Context, platform: AiPlatform): WebView {
        currentActivityContext = context
        val appContext = context.applicationContext
        
        // Enable third party cookies for logins (some sites need this if they use OAuth)
        CookieManager.getInstance().setAcceptCookie(true)

        return webViews.getOrPut(platform) {
            WebView(appContext).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    mediaPlaybackRequiresUserGesture = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    javaScriptCanOpenWindowsAutomatically = true
                    setSupportMultipleWindows(true)
                    cacheMode = WebSettings.LOAD_DEFAULT
                    allowFileAccess = false
                    allowContentAccess = false
                    userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                }

                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                val jsInterface = WebAppInterface { platName, text, _ ->
                    AiPlatform.fromString(platName)?.let { p ->
                        onResponseUpdate?.invoke(p, text)
                    }
                }
                addJavascriptInterface(jsInterface, "AndroidBridge")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Inject observer script when page finishes loading
                        adapters[platform]?.getObserverScript()?.let { script ->
                            evaluateJavascript(script, null)
                        }
                    }
                    
                    override fun onRenderProcessGone(view: WebView?, detail: android.webkit.RenderProcessGoneDetail?): Boolean {
                        // Recover from renderer crashes
                        if (view != null) {
                            (view.parent as? android.view.ViewGroup)?.removeView(view)
                            view.destroy()
                        }
                        webViews.remove(platform)
                        return true
                    }
                    
                    override fun onReceivedSslError(view: WebView?, handler: android.webkit.SslErrorHandler?, error: android.net.http.SslError?) {
                        handler?.proceed() // Proceed to make it resilient against SSL issues (though slightly less secure, users often want it to 'just work' for AI tools)
                    }
                }
                
                webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                        request?.grant(request.resources) // Automatically grant all permissions (microphone, etc.)
                    }
                    
                    override fun onCreateWindow(
                        view: WebView?,
                        isDialog: Boolean,
                        isUserGesture: Boolean,
                        resultMsg: android.os.Message?
                    ): Boolean {
                        val activeContext = currentActivityContext ?: return false
                        val newWebView = WebView(activeContext).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                setSupportZoom(true)
                                javaScriptCanOpenWindowsAutomatically = true
                                setSupportMultipleWindows(true)
                                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                            }
                            
                            val container = android.widget.FrameLayout(activeContext)
                            container.addView(this)
                            
                            var dialog: android.app.AlertDialog? = null
                            
                            webChromeClient = object : WebChromeClient() {
                                override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                                    request?.grant(request.resources)
                                }
                                override fun onCloseWindow(window: WebView?) {
                                    dialog?.dismiss()
                                }
                            }
                            
                            val adBuilder = android.app.AlertDialog.Builder(activeContext, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
                            adBuilder.setView(container)
                            adBuilder.setOnDismissListener {
                                activeDialogs.remove(dialog)
                                container.removeAllViews()
                                destroy()
                            }
                            dialog = adBuilder.create()
                            dialog?.let { activeDialogs.add(it) }
                            dialog?.show()
                        }
                        
                        val transport = resultMsg?.obj as? WebView.WebViewTransport
                        transport?.webView = newWebView
                        resultMsg?.sendToTarget()
                        return true
                    }
                }
                loadUrl(platform.homeUrl)
            }
        }
    }

    fun sendPrompt(context: Context, platform: AiPlatform, prompt: String) {
        val adapter = adapters[platform] ?: return
        val webView = getWebView(context, platform)
        val script = adapter.getInjectPromptScript(prompt)
        webView.evaluateJavascript(script, null)
        
        // Re-inject observer just in case it was lost
        adapter.getObserverScript()?.let { observerScript ->
            webView.evaluateJavascript(observerScript, null)
        }
    }

    fun dismissAllDialogs() {
        activeDialogs.forEach { it.dismiss() }
        activeDialogs.clear()
    }

    fun destroy() {
        // We no longer destroy WebViews on activity destruction to support configuration changes.
        // If we want to fully shut down, we can call this.
    }
}
