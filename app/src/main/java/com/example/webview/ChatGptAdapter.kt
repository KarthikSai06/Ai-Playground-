package com.example.webview

class ChatGptAdapter : AiAdapter {
    override val platform = AiPlatform.CHATGPT

    override fun getInjectPromptScript(prompt: String): String {
        val safePrompt = prompt.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$")
        return """
            (function(promptText) {
                try {
                    let el = document.querySelector('#prompt-textarea');
                    if (el) {
                        if (el.tagName === 'TEXTAREA') {
                            let nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, "value").set;
                            nativeInputValueSetter.call(el, promptText);
                        } else {
                            el.innerHTML = '<p>' + promptText + '</p>';
                        }
                        
                        el.dispatchEvent(new Event('input', { bubbles: true }));
                        
                        setTimeout(() => {
                            let sendBtn = document.querySelector('button[data-testid="send-button"]');
                            if (sendBtn && !sendBtn.disabled) {
                                sendBtn.click();
                            } else {
                                el.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true }));
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
                    let messages = document.querySelectorAll('div[data-message-author-role="assistant"], .markdown');
                    if (messages && messages.length > 0) {
                        let latestMsg = messages[messages.length - 1];
                        let newText = latestMsg.innerText;
                        if (newText && newText !== lastResponseText && newText.trim() !== '') {
                            lastResponseText = newText;
                            AndroidBridge.onResponseUpdate("CHATGPT", newText, false);
                        }
                    }
                }, 1000);
            })();
        """.trimIndent()
    }
}
