let conversationId = localStorage.getItem("conversation_id") || "";
    const messagesContainer = document.getElementById("messages");
    const msgInput = document.getElementById("msg"); // 獲取輸入框元素

    let currentAiMessageElement = null;
    let isFirstAiChunk = true;

    // 處理 Enter 鍵發送訊息
    function handleKeyPress(event) {
      if (event.key === 'Enter') {
        send();
      }
    }

    // 將訊息添加到聊天視窗中
    function appendMessage(text, sender, isLoading = false) {
      const messageBubble = document.createElement("div");
      messageBubble.classList.add("message-bubble");
      if (sender === "user") {
        messageBubble.classList.add("user-message");
        messageBubble.textContent = text;
        messagesContainer.appendChild(messageBubble);
      } else if (sender === "ai") {
        messageBubble.classList.add("ai-message");
        if (isLoading) {
            messageBubble.innerHTML = '<span class="loading-dots"><span>.</span><span>.</span><span>.</span></span>';
        } else {
            messageBubble.textContent = text;
        }
        messagesContainer.appendChild(messageBubble);
      }
      scrollToBottom();
      return messageBubble;
    }

    // 滾動聊天視窗到底部
    function scrollToBottom() {
      messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    async function send() {
      const msg = msgInput.value.trim();

      if (!msg) {
        return;
      }

      appendMessage(msg, "user");
      msgInput.value = "";
      
      currentAiMessageElement = appendMessage("", "ai", true); 
      isFirstAiChunk = true;

      const response = await fetch("/api/chat", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          query: msg,
          conversation_id: conversationId
        })
      });

      const reader = response.body.getReader();
      const decoder = new TextDecoder("utf-8");

      let buffer = "";

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value, { stream: true });
        buffer += chunk;

        let newlineIndex;
        while ((newlineIndex = buffer.indexOf('\n')) !== -1) {
          const line = buffer.substring(0, newlineIndex);
          buffer = buffer.substring(newlineIndex + 1);

          const trimmedLine = line.trim();

          if (trimmedLine.startsWith('data:')) {
            const jsonString = trimmedLine.replace(/^(data:\s*)+/, '').trim();
            
            if (jsonString && (jsonString.startsWith('{') && jsonString.endsWith('}'))) { 
              try {
                const json = JSON.parse(jsonString);
                if (json.answer) {
                  if (isFirstAiChunk) {
                      currentAiMessageElement.innerHTML = "";
                      isFirstAiChunk = false;
                  }
                  currentAiMessageElement.textContent += json.answer;
                  scrollToBottom();
                }

                if (json.conversation_id) {
                  conversationId = json.conversation_id;
                  localStorage.setItem("conversation_id", conversationId);
                }
              } catch (e) {
                console.warn("忽略無法解析的段落：", jsonString, e);
              }
            } else if (jsonString) { 
                 console.warn("忽略無法解析的段落 (非JSON格式)：", jsonString);
            }
          } else if (trimmedLine) { 
               console.warn("忽略無法解析的段落 (非data:開頭)：", trimmedLine);
            }
        }
      }
      
      if (buffer.trim() !== "") {
          const trimmedLine = buffer.trim();
          if (trimmedLine.startsWith('data:')) {
              const jsonString = trimmedLine.replace(/^(data:\s*)+/, '').trim(); 
              if (jsonString && (jsonString.startsWith('{') && jsonString.endsWith('}'))) {
                  try {
                      const json = JSON.parse(jsonString);
                      if (json.answer) {
                          if (isFirstAiChunk) {
                              currentAiMessageElement.innerHTML = "";
                              isFirstAiChunk = false;
                          }
                          currentAiMessageElement.textContent += json.answer;
                          scrollToBottom();
                      }
                      if (json.conversation_id) {
                          conversationId = json.conversation_id;
                          localStorage.setItem("conversation_id", conversationId);
                      }
                  } catch (e) {
                      console.warn("忽略無法解析的段落 (結尾):", jsonString, e);
                  }
              } else if (jsonString) {
                    console.warn("忽略無法解析的段落 (結尾非JSON格式)：", jsonString);
              }
          } else if (trimmedLine) {
               console.warn("忽略無法解析的段落 (結尾非data:開頭)：", trimmedLine);
          }
      }

      if (isFirstAiChunk && currentAiMessageElement) {
          currentAiMessageElement.innerHTML = "（AI 沒有回覆內容）";
          scrollToBottom();
      }
    }