package com.example.webview

class PerplexityAdapter : AiAdapter {
    override val platform = AiPlatform.PERPLEXITY

    override fun getInjectPromptScript(prompt: String): String {
        val safePrompt = prompt.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$")
        return """
            (function(promptText) {
                try {
                    let textarea = document.querySelector('textarea') || 
                                   document.querySelector('[contenteditable="true"]') || 
                                   document.querySelector('input[type="text"]');
                    if (textarea) {
                        textarea.focus();
                        
                        // Try standard execCommand first (highly compatible with React/Next state tracking)
                        try {
                            document.execCommand('selectAll', false, null);
                            document.execCommand('insertText', false, promptText);
                        } catch(err) {
                            console.warn("execCommand failed, trying prototype setter", err);
                        }
                        
                        // Force update using native prototype descriptor just in case execCommand didn't fully apply
                        if (textarea.tagName === 'TEXTAREA' || textarea.tagName === 'INPUT') {
                            let nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, "value")?.set;
                            let nativeInputSetter2 = Object.getOwnPropertyDescriptor(Object.getPrototypeOf(textarea), "value")?.set;
                            if (nativeInputValueSetter) {
                                nativeInputValueSetter.call(textarea, promptText);
                            } else if (nativeInputSetter2) {
                                nativeInputSetter2.call(textarea, promptText);
                            } else {
                                textarea.value = promptText;
                            }
                        }
                        
                        // Dispatch standard events
                        textarea.dispatchEvent(new Event('input', { bubbles: true }));
                        textarea.dispatchEvent(new Event('change', { bubbles: true }));
                        
                        // Simulate paste event
                        try {
                            let dataTransfer = new DataTransfer();
                            dataTransfer.setData('text/plain', promptText);
                            textarea.dispatchEvent(new ClipboardEvent('paste', {
                                clipboardData: dataTransfer,
                                bubbles: true,
                                cancelable: true
                            }));
                        } catch(pe) {}
                        
                        // Wait a short duration and click the submit button
                        setTimeout(() => {
                            let sendBtn = document.querySelector('button[aria-label*="Submit"]') || 
                                          document.querySelector('button[aria-label*="submit"]') ||
                                          document.querySelector('button[aria-label*="query"]') ||
                                          document.querySelector('button[aria-label*="Query"]') ||
                                          document.querySelector('button[aria-label*="send"]') ||
                                          document.querySelector('button[aria-label*="Send"]');
                                          
                            if (!sendBtn) {
                                // Fallback: find any button with a submit-like SVG inside
                                let sendBtns = Array.from(document.querySelectorAll('button'));
                                sendBtn = sendBtns.find(b => {
                                    let svg = b.querySelector('svg');
                                    let aria = (b.getAttribute('aria-label') || '').toLowerCase();
                                    return aria.includes('submit') || aria.includes('send') || (svg && (b.innerHTML.includes('bg-') || b.className.includes('bg-')));
                                });
                            }
                            
                            if (sendBtn) {
                                sendBtn.disabled = false;
                                sendBtn.removeAttribute('disabled');
                                sendBtn.click();
                            } else {
                                // Default fallback to Enter key
                                textarea.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true }));
                                textarea.dispatchEvent(new KeyboardEvent('keypress', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true }));
                                textarea.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true }));
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
                    let responses = document.querySelectorAll('.prose, [class*="prose"]');
                    if (!responses || responses.length === 0) {
                        responses = document.querySelectorAll('div.answer, .answer-block');
                    }
                    if (responses && responses.length > 0) {
                        // Perplexity's actual answer is usually in the last prose element
                        let latestMsg = responses[responses.length - 1];
                        let newText = latestMsg.innerText;
                        
                        if (newText && newText !== lastResponseText && newText.trim() !== '') {
                            lastResponseText = newText;
                            AndroidBridge.onResponseUpdate("PERPLEXITY", newText, false);
                        }
                    }
                }, 1000);
            })();
        """.trimIndent()
    }
}
