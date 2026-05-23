package com.example.webview

class PerplexityAdapter : AiAdapter {
    override val platform = AiPlatform.PERPLEXITY

    override fun getInjectPromptScript(prompt: String): String {
        val safePrompt = prompt.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$")
        return """
            (function(promptText) {
                try {
                    let textarea = document.querySelector('textarea, [contenteditable="true"]');
                    if (textarea) {
                        textarea.focus();
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
                        } else {
                            // Content Editable
                            document.execCommand('selectAll', false, null);
                            document.execCommand('insertText', false, promptText);
                        }
                        textarea.dispatchEvent(new Event('input', { bubbles: true }));
                        textarea.dispatchEvent(new Event('change', { bubbles: true }));
                        
                        setTimeout(() => {
                            let sendBtns = document.querySelectorAll('button');
                            let sendBtn = Array.from(sendBtns).find(b => {
                                let svg = b.querySelector('svg');
                                return b.getAttribute('aria-label') === 'Submit' || (svg && svg.innerHTML.includes('path') && b.innerHTML.includes('bg-'));
                            }) || document.querySelector('button[aria-label="Submit"]'); 
                            
                            if (sendBtn && !sendBtn.disabled) {
                                sendBtn.click();
                            } else {
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
