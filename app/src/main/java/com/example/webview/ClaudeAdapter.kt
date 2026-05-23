package com.example.webview

class ClaudeAdapter : AiAdapter {
    override val platform = AiPlatform.CLAUDE

    // Claude uses a contenteditable div usually
    override fun getInjectPromptScript(prompt: String): String {
        val safePrompt = prompt.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$")
        return """
            (function(promptText) {
                try {
                    let el = document.querySelector('div[contenteditable="true"]') || document.querySelector('.ProseMirror');
                    if (el) {
                        el.focus();
                        
                        let dataTransfer = new DataTransfer();
                        dataTransfer.setData('text/plain', promptText);
                        let pasteEvent = new ClipboardEvent('paste', {
                            clipboardData: dataTransfer,
                            bubbles: true,
                            cancelable: true
                        });
                        el.dispatchEvent(pasteEvent);
                        
                        setTimeout(() => {
                            let sendBtns = Array.from(document.querySelectorAll('button'));
                            let sendBtn = sendBtns.find(b => b.getAttribute('aria-label') && b.getAttribute('aria-label').toLowerCase().includes('send message')) ||
                                          sendBtns.find(b => b.textContent && b.textContent.toLowerCase() === 'send');
                            
                            if (!sendBtn) {
                                sendBtn = sendBtns.filter(b => b.querySelector('svg') && !b.disabled && b.getAttribute('aria-label') === 'Send Message').pop();
                            }
                            
                            if (sendBtn && !sendBtn.disabled) {
                                sendBtn.removeAttribute('disabled');
                                sendBtn.click();
                            }
                            
                            // Aggressive Enter dispatch for ProseMirror (Claude)
                            setTimeout(() => {
                                el.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true }));
                                el.dispatchEvent(new KeyboardEvent('keypress', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true }));
                                el.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true }));
                                
                                // Sometimes Claude needs an empty newline to trigger state change then enter
                                let btn = document.querySelector('button[aria-label="Send Message"]');
                                if (btn && !btn.disabled) btn.click();
                            }, 100);
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
                    let responses = document.querySelectorAll('.font-claude-message, .message, div[data-is-user="false"], div.prose, [data-test-render-count]');
                    if (responses && responses.length > 0) {
                        let latestMsg = responses[responses.length - 1];
                        let newText = latestMsg.innerText;
                        if (newText !== lastResponseText && newText.trim() !== '') {
                            lastResponseText = newText;
                            AndroidBridge.onResponseUpdate("CLAUDE", newText, false);
                        }
                    }
                }, 1000);
            })();
        """.trimIndent()
    }
}
