// === 全域變數定義 ===

// 用來儲存當前對話的ID，確保與AI的對話有上下文關聯。
// 如果是新的對話，這個值會是空的。
let conversationId = ""; 
// let conversationId = localStorage.getItem("conversation_id") || ""; // 這行被註解掉了，原本是用來從瀏覽器本地儲存中讀取之前的對話ID，以實現跨頁面的對話記憶。

// 獲取HTML中ID為 "messages" 的元素，這是我們的聊天視窗容器。
const messagesContainer = document.getElementById("messages");
// 獲取HTML中ID為 "msg" 的元素，這是使用者輸入訊息的輸入框。
const msgInput = document.getElementById("msg"); 

// 用來追蹤當前AI正在回覆的那個對話氣泡元素。
// 這樣我們才能把陸續收到的文字片段填入同一個氣泡中。
let currentAiMessageElement = null;
// 一個布林值的「旗標」，用來判斷是否為AI回覆的第一個資料片段。
// 如果是，我們需要先清除 "..." 的載入動畫。
let isFirstAiChunk = true;

// === 函式定義 ===

/**
 * 處理鍵盤按下事件。
 * 如果使用者按下的是 'Enter' 鍵，就呼叫 send() 函式來發送訊息。
 * @param {KeyboardEvent} event - 鍵盤事件物件
 */
function handleKeyPress(event) {
  if (event.key === 'Enter') {
    send();
  }
}

/**
 * 在聊天視窗中添加一個新的對話氣泡。
 * @param {string} text - 要顯示的訊息文字。
 * @param {string} sender - 發送者，'user' 或 'ai'，用來決定樣式。
 * @param {boolean} isLoading - 是否顯示為載入中狀態 (顯示三個點的動畫)。
 * @returns {HTMLElement} - 返回被創建的訊息內容元素，方便後續更新。
 */
function appendMessage(text, sender, isLoading = false) {
    // 創建最外層的氣泡 div
    const messageBubble = document.createElement("div");
    messageBubble.classList.add("message-bubble", sender + "-message"); // 加上 CSS class 來區分使用者和AI的樣式

    // 創建容納文字內容的 div
    const messageContent = document.createElement("div");
    messageContent.classList.add("message-content");

    if (isLoading) {
        // 如果是載入中狀態，創建並添加三個點的動畫
        const loadingDots = document.createElement('span');
        loadingDots.classList.add('loading-dots');
        loadingDots.innerHTML = '<span>.</span><span>.</span><span>.</span>';
        messageContent.appendChild(loadingDots);
    } else {
        // 否則，直接設定文字內容
        messageContent.textContent = text;
    }

    // 將內容放入氣泡，再將氣泡放入聊天視窗容器
    messageBubble.appendChild(messageContent);
    messagesContainer.appendChild(messageBubble);
    // 自動滾動到底部，確保最新的訊息可見
    scrollToBottom();
    // 返回內容元素，這樣 send() 函式才能後續更新它的文字
    return messageContent; 
}

/**
 * 將聊天視窗的捲軸滾動到最底部。
 */
function scrollToBottom() {
  messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

/**
 * 發送訊息的核心函式。
 */
async function send() {
  // 獲取輸入框中的文字，並移除頭尾的空白。
  const msg = msgInput.value.trim();

  // 如果沒有輸入任何內容，就直接返回，不執行任何操作。
  if (!msg) {
    return;
  }

  // 1. 在畫面上顯示使用者剛剛輸入的訊息。
  appendMessage(msg, "user");
  // 2. 清空輸入框，方便使用者下一次輸入。
  msgInput.value = "";
  
  // 3. 顯示一個 AI 的對話氣泡，並呈現載入中 ("...") 的動畫。
  //    同時，將這個氣泡元素存到 currentAiMessageElement，準備接收AI的回覆。
  currentAiMessageElement = appendMessage("", "ai", true); 
  //    重設旗標，表示我們正準備接收AI的第一個回覆片段。
  isFirstAiChunk = true;

  // 4. 使用 fetch API 向後端發送請求。
  const response = await fetch("/api/chat", { // 請求的網址
    method: "POST", // 使用 POST 方法
    headers: {
      "Content-Type": "application/json" // 告訴後端我們發送的是 JSON 格式的資料
    },
    body: JSON.stringify({ // 將 JavaScript 物件轉換成 JSON 字串
      query: msg, // 使用者輸入的訊息
      conversation_id: conversationId // 當前的對話ID
    })
  });

  // 5. 處理後端回傳的「串流」資料。
  const reader = response.body.getReader(); // 獲取串流的讀取器
  const decoder = new TextDecoder("utf-8"); // 建立一個解碼器，將二進位資料轉為文字

  let buffer = ""; // 建立一個緩衝區，用來暫存可能不完整的資料片段

  // 6. 開始一個無限迴圈來持續讀取串流資料
  while (true) {
    const { value, done } = await reader.read(); // 讀取一塊資料
    if (done) break; // 如果資料流結束 (done 為 true)，就跳出迴圈

    // 將讀取到的二進位資料塊解碼成文字，並加到緩衝區
    const chunk = decoder.decode(value, { stream: true });
    buffer += chunk;

    // 處理緩衝區中的完整行 (以換行符 '\n' 分隔)
    let newlineIndex;
    while ((newlineIndex = buffer.indexOf('\n')) !== -1) {
      const line = buffer.substring(0, newlineIndex); // 取出一行
      buffer = buffer.substring(newlineIndex + 1); // 更新緩衝區，移除已處理的行

      const trimmedLine = line.trim(); // 去掉這行的頭尾空白

      // SSE (Server-Sent Events) 的標準格式是以 "data:" 開頭
      if (trimmedLine.startsWith('data:')) {
        // 移除 "data:" 前綴，得到真正的 JSON 字串
        const jsonString = trimmedLine.replace(/^(data:\s*)+/, '').trim();
        
        // 確保這是一個有效的 JSON 物件
        if (jsonString && (jsonString.startsWith('{') && jsonString.endsWith('}'))) { 
          try {
            const json = JSON.parse(jsonString); // 解析 JSON 字串
            
            // 如果 JSON 中有 'answer' 這個欄位，代表是 AI 的回覆文字
            if (json.answer) {
              // 如果這是 AI 回覆的第一個片段
              if (isFirstAiChunk) {
                  currentAiMessageElement.innerHTML = ""; // 清除 "..." 載入動畫
                  isFirstAiChunk = false; // 將旗標設為 false，之後的片段就不用再清除了
              }
              // 將新的文字片段「追加」到 AI 的對話氣泡中，實現打字機效果
              currentAiMessageElement.textContent += json.answer;
              scrollToBottom(); // 每次更新都滾動到底部
            }

            // 如果 JSON 中有 'conversation_id'，就更新全域變數
            if (json.conversation_id) {
              conversationId = json.conversation_id;
//                  localStorage.setItem("conversation_id", conversationId);
            }
          } catch (e) {
            // 如果 JSON 解析失敗，就在控制台印出警告，但程式不會中斷
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
  
  // 迴圈結束後，檢查緩衝區是否還有剩餘的、不以換行符結尾的資料，並做最後一次處理
  // (這段邏輯與迴圈內的處理基本相同)
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
      }
      else if (trimmedLine) {
           console.warn("忽略無法解析的段落 (結尾非data:開頭)：", trimmedLine);
      }
  }

  // 如果整個串流結束後，AI 仍然沒有回覆任何內容 (isFirstAiChunk 依然為 true)
  if (isFirstAiChunk && currentAiMessageElement) {
      currentAiMessageElement.innerHTML = "（AI 沒有回覆內容）"; // 顯示提示訊息
      scrollToBottom();
  }
}

// === 程式初始化 ===

// 頁面載入後，自動將聊天視窗滾動到底部
scrollToBottom();
// 讓使用者可以直接在輸入框打字
msgInput.focus();
// 為輸入框加上事件監聽，監聽鍵盤按下的事件
msgInput.addEventListener("keypress", handleKeyPress);