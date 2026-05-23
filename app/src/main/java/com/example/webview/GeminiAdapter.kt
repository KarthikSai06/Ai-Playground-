package com.example.webview

class GeminiAdapter : AiAdapter {
    override val platform = AiPlatform.GEMINI

    override fun getInjectPromptScript(prompt: String): String {
        val safePrompt = prompt.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$")
        return """
            (function(promptText) {
                try {
                    let el = document.querySelector('rich-textarea div[role="textbox"]') || document.querySelector('.ql-editor') || document.querySelector('div[contenteditable="true"]');
                    if (el) {
                        el.focus();
                        
                        // Clear and set using execCommand
                        document.execCommand('selectAll', false, null);
                        document.execCommand('insertText', false, promptText);
                        
                        // Dispatch various events to trigger state update
                        el.dispatchEvent(new Event('input', { bubbles: true }));
                        el.dispatchEvent(new Event('change', { bubbles: true }));
                        
                        let dataTransfer = new DataTransfer();
                        dataTransfer.setData('text/plain', promptText);
                        el.dispatchEvent(new ClipboardEvent('paste', { clipboardData: dataTransfer, bubbles: true, cancelable: true }));
                        
                        setTimeout(() => {
                            let sendBtns = Array.from(document.querySelectorAll('button'));
                            let sendBtn = sendBtns.find(b => b.getAttribute('aria-label') && b.getAttribute('aria-label').toLowerCase().includes('send')) ||
                                          sendBtns.find(b => b.querySelector('svg') && !b.disabled && (b.getAttribute('aria-label') || '').toLowerCase().includes('send')) ||
                                          document.querySelector('.send-button, .bottom-container button.send');
                            
                            if (sendBtn && !sendBtn.disabled) {
                                sendBtn.removeAttribute('disabled');
                                sendBtn.click();
                            } else {
                                el.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true }));
                                el.dispatchEvent(new KeyboardEvent('keypress', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true }));
                                el.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true }));
                            }
                        }, 500);
                    }
                } catch (e) {
                    console.error("AIHub Injection Error:", e);
                }
            })(`$safePrompt`);
        """.trimIndent()
    }

    override fun getObserverScript(): String {
        return """
            (function() {
                if (window.aiHubPollerId) clearInterval(window.aiHubPollerId);
                let lastResponseText = "";
                window.aiHubPollerId = setInterval(() => {
                    let responses = document.querySelectorAll('model-response, message-content, div[class*="message-content"], [data-message-author-role="model"], .model-response-text');
                    if (responses && responses.length > 0) {
                        let latestMsg = responses[responses.length - 1];
                        let newText = latestMsg.innerText;
                        if (newText !== lastResponseText && newText.trim() !== '') {
                            lastResponseText = newText;
                            AndroidBridge.onResponseUpdate("GEMINI", newText, false);
                        }
                    }
                }, 1000);
            })();
        """.trimIndent()
    }
}
