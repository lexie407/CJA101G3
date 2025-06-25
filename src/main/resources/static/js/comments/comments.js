document.addEventListener("DOMContentLoaded", function() {
    const commentForm = document.getElementById('commentForm');
    const statusMessage = document.getElementById('statusMessage');
    const commentsList = document.getElementById('commentsList');

    const actionModal = document.getElementById('actionModal');
    const modalTitle = document.getElementById('modalTitle');
    const modalMessage = document.getElementById('modalMessage');
    const confirmActionButton = document.getElementById('confirmActionButton');
    const modalCancelButton = actionModal.querySelector('.cancel-btn');
    const modalCloseButton = actionModal.querySelector('.close-button');

    // !!! 重要 !!!
    // 模擬當前登入使用者的 ID (實際應用中，這會從後端取得)
    // 例如：從一個隱藏的 input 欄位讀取，或者在頁面載入時透過 AJAX 請求獲取。
    // 在這裡我設定為 'user1' 作為範例。
    // 請根據你的後端邏輯，替換成實際登入使用者的 ID。
    const CURRENT_USER_ID = 'user1';

    // 監聽表單提交事件
    commentForm.addEventListener('submit', async (event) => {
        event.preventDefault(); // 阻止表單的預設提交行為

        statusMessage.style.display = 'none'; // 隱藏之前的狀態訊息

        const formData = new FormData(commentForm); // 從表單建立 FormData 物件

        // 獲取檔案並檢查是否為空
        const imageFile = formData.get('commentImage');
        // 檢查 file 物件是否存在且其 name 屬性為空（表示沒有選擇檔案）
        // 並且文件大小為 0 (這是更嚴格的檢查，因為某些瀏覽器空檔案也可能有 name)
        if (imageFile && imageFile.name === '' && imageFile.size === 0) {
            formData.delete('commentImage'); // 從 FormData 中移除它，這樣後端就不會收到空檔案
        }

        try {
            // 假設 API 接收 { author, content, commentImage } 並回傳新增的 CommentVO
            const response = await fetch('/commentsAPI/addComments', {
                method: 'POST',
                body: formData, // 直接傳入 FormData，fetch 會自動設定 Content-Type 為 multipart/form-data
                // 不要手動設定 'Content-Type': 'multipart/form-data'，讓瀏覽器自動處理邊界
            });

            if (response.ok) { // 檢查 HTTP 狀態碼是否為 2xx (包括 200 OK)
                const newComment = await response.json(); // 假設 API 回傳新增的留言資料 (JSON 格式)
                // 在前端處理時，可以為新留言加上當前使用者 ID 的資訊，
                // 但最佳實踐是讓後端在回傳留言資料時就包含留言者的 ID (例如 memId)。
                // 這裡我們假設 newComment 已經包含了 memId。
                displayComment(newComment); // 在頁面顯示新留言

                showStatusMessage('留言新增成功！', 'success');
                commentForm.reset(); // 清空表單
                document.getElementById("preview").innerHTML = ""; // 清空圖片預覽
            } else {
                const errorData = await response.json(); // 假設 API 會回傳錯誤訊息
                showStatusMessage(`留言新增失敗: ${errorData.message || response.statusText}`, 'error');
            }
        } catch (error) {
            console.error('網路或伺服器錯誤:', error);
            showStatusMessage('請求失敗，請檢查網路連線或伺服器狀態。', 'error');
        }
    });

    // 顯示狀態訊息的輔助函數
    function showStatusMessage(message, type) {
        statusMessage.textContent = message;
        statusMessage.className = `message ${type}`; // 設置樣式類別
        statusMessage.style.display = 'block';
    }

    // 動態顯示留言的函數
    function displayComment(comment) {
        const commentItem = document.createElement('div');
        commentItem.className = 'comment-item';
        // 將留言的 ID 和使用者 ID 儲存在 data 屬性中
        // 這裡假設後端回傳的留言物件中，ID 欄位是 commId，留言者 ID 欄位是 memId
        commentItem.dataset.commentId = comment.commId;
        commentItem.dataset.userId = comment.memId;

        const authorStrong = document.createElement('strong');
        authorStrong.textContent = comment.author || '匿名使用者'; // 假設後端回傳 author 欄位
        commentItem.appendChild(authorStrong);

        // 時間格式化 (假設後端回傳 commCreTime)
        if (comment.commCreTime) {
            // 如果後端已經用 @JsonFormat 格式化好了，這裡可以直接使用 comment.commCreTime
            // 否則，可以在前端進行格式化：
            const date = new Date(comment.commCreTime); // 將 ISO 8601 字串轉換為 Date 物件

            // 手動建構格式 (更精確控制，與你之前的格式一致)
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0'); // 月份從0開始
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            const seconds = String(date.getSeconds()).padStart(2, '0');

            const formattedTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;

            const timeParagraph = document.createElement('p');
            timeParagraph.className = 'comment-time';
            timeParagraph.textContent = `時間: ${formattedTime}`;
            commentItem.appendChild(timeParagraph);
        }

        const contentParagraph = document.createElement('p');
        contentParagraph.textContent = comment.commCon; // 假設後端回傳 content 欄位
        commentItem.appendChild(contentParagraph);

        // 如果有圖片路徑，則顯示圖片
        if (comment.commImg) { // 假設後端回傳 commImg 是圖片的資料，或者這裡應該是圖片的 URL
            const img = document.createElement('img');
            const commId = comment.commId; // 用留言 ID 來構建圖片的路徑
            img.src = `/commentsReport/read.do?commId=${commId}`; // 圖片顯示的路徑
            img.alt = '留言圖片';
            commentItem.appendChild(img);
        }


        // --- 新增操作按鈕和選單 (這整個區塊都應該在這裡面！) ---
        const actionsDiv = document.createElement('div');
        actionsDiv.className = 'comment-actions';

        const actionsButton = document.createElement('button');
        actionsButton.className = 'actions-button';
        actionsButton.textContent = '...';
        actionsButton.setAttribute('aria-expanded', 'false'); // 無障礙輔助

        const dropdownMenu = document.createElement('div');
        dropdownMenu.className = 'dropdown-menu';

        // 檢舉按鈕 (任何留言都可以檢舉)
        const reportBtn = document.createElement('button');
        reportBtn.className = 'action-report';
        reportBtn.dataset.action = 'report';
        reportBtn.textContent = '檢舉';
        dropdownMenu.appendChild(reportBtn);

        // 修改和刪除按鈕 (只有自己的留言才顯示)
        // 使用從後端回傳的 comment.memId 與前端 CURRENT_USER_ID 進行比對
        if (comment.memId === CURRENT_USER_ID) {
            const editBtn = document.createElement('button');
            editBtn.className = 'action-edit';
            editBtn.dataset.action = 'edit';
            editBtn.textContent = '修改';
            dropdownMenu.appendChild(editBtn);

            const deleteBtn = document.createElement('button');
            deleteBtn.className = 'action-delete';
            deleteBtn.dataset.action = 'delete';
            deleteBtn.textContent = '刪除';
            dropdownMenu.appendChild(deleteBtn);
        }

        actionsDiv.appendChild(actionsButton);
        actionsDiv.appendChild(dropdownMenu);
        commentItem.appendChild(actionsDiv); // <-- 這行現在在正確的作用域內
        // --- 結束新增 ---


        // 將新留言添加到留言列表的最前面
        commentsList.prepend(commentItem);
    }

    //圖片上傳預覽
    let fileEl = document.getElementById("commentImage");
    fileEl.addEventListener("change", function(e) {
        console.log("檔案選擇事件觸發！");

        let reader = new FileReader();

        if (e.target.files.length > 0) {
            const selectedFile = e.target.files[0];
            console.log("選擇的檔案名稱:", selectedFile.name);
            console.log("選擇的檔案類型:", selectedFile.type);
            console.log("選擇的檔案大小:", selectedFile.size, " bytes");

            reader.readAsDataURL(selectedFile);

            reader.addEventListener("load", function() {
                console.log("FileReader load 事件觸發！");
                console.log("reader.result 的值 (Base64 資料):", reader.result ? reader.result.substring(0, 50) + "..." : "null");

                if (reader.result) {
                    let previewDiv = document.getElementById("preview");
                    if (!previewDiv) { // 如果沒有 preview 元素，可以動態創建一個
                        previewDiv = document.createElement('div');
                        previewDiv.id = 'preview';
                        // 找到一個合適的位置插入，例如在 input type="file" 後面
                        fileEl.parentNode.insertBefore(previewDiv, fileEl.nextSibling);
                    }
                    previewDiv.innerHTML = ""; // 先清空預覽區

                    let imgElement = document.createElement("img"); // 創建一個新的 img 元素
                    imgElement.src = reader.result; // 設定 src 屬性
                    imgElement.className = "preview_img"; // 設定 class
                    imgElement.id = "preview_img"; // 設定 id
                    imgElement.style.maxWidth = "100px"; // 設定最大寬度以適應預覽

                    previewDiv.appendChild(imgElement); // 將 img 元素添加到預覽區

                    console.log("圖片預覽已設定成功！");

                    let createdImg = document.getElementById("preview_img");
                    if (createdImg) {
                        console.log("DOM中獲取到的<img>元素的src (直接賦值後):", createdImg.src ? createdImg.src.substring(0, 50) + "..." : "null");
                        if (createdImg.src === reader.result) {
                            console.log("src屬性值與reader.result一致！(直接賦值)");
                        } else {
                            console.warn("src屬性值與reader.result不一致，可能存在刷新問題！(直接賦值)");
                        }
                    } else {
                        console.error("無法在DOM中找到 id 為 preview_img 的元素！(直接賦值)");
                    }

                } else {
                    console.error("reader.result 為空，無法顯示圖片！");
                }
            });

            reader.addEventListener("error", function() {
                console.error("FileReader 讀取檔案時發生錯誤！", reader.error);
            });

            reader.addEventListener("abort", function() {
                console.warn("FileReader 讀取檔案被中斷！");
            });

        } else {
            console.log("沒有選擇檔案。");
            document.getElementById("preview").innerHTML = ""; // 清空預覽
        }
    });

    // --- 燈箱和選單的 JavaScript 邏輯 ---

    // 關閉所有打開的選單
    function closeAllDropdowns() {
        document.querySelectorAll('.dropdown-menu.show').forEach(menu => {
            menu.classList.remove('show');
            menu.previousElementSibling.setAttribute('aria-expanded', 'false');
        });
    }

    // 點擊留言列表內的元素（事件委派）
    commentsList.addEventListener('click', (event) => {
        const target = event.target;

        // 處理三點按鈕點擊
        if (target.classList.contains('actions-button')) {
            const dropdown = target.nextElementSibling; // 獲取同層的 dropdown-menu
            closeAllDropdowns(); // 關閉其他選單
            dropdown.classList.toggle('show'); // 切換當前選單的顯示狀態
            target.setAttribute('aria-expanded', dropdown.classList.contains('show'));
        }
        // 處理選單選項點擊
        else if (target.classList.contains('action-report') ||
            target.classList.contains('action-edit') ||
            target.classList.contains('action-delete')) {

            closeAllDropdowns(); // 點擊選項後關閉選單

            const action = target.dataset.action;
            const commentItem = target.closest('.comment-item'); // 找到最近的留言父元素
            const commentId = commentItem.dataset.commentId;
            const userId = commentItem.dataset.userId; // 獲取留言作者ID

            // 根據動作打開不同的燈箱或執行操作
            switch (action) {
                case 'report':
                    openModal('檢舉留言', `確定要檢舉這條留言嗎？`, 'report', commentId);
                    break;
                case 'edit':
                    // TODO: 執行修改操作，例如跳轉到編輯頁面或在當前頁面打開編輯表單
                    alert(`您點擊了修改留言 ID: ${commentId}`);
                    console.log('執行修改操作，留言 ID:', commentId);
                    break;
                case 'delete':
                    openModal('刪除留言', `確定要刪除這條留言嗎？此操作無法恢復。`, 'delete', commentId);
                    break;
            }
        }
        // 點擊其他地方關閉選單
        else {
            closeAllDropdowns();
        }
    });

    // 點擊視窗任何地方關閉選單 (不在留言列表內的操作)
    document.addEventListener('click', (event) => {
        // 如果點擊的目標不是任何 .comment-item 內的元素，也不是 .actions-button
        if (!event.target.closest('.comment-item') && !event.target.classList.contains('actions-button')) {
            closeAllDropdowns();
        }
    });


    // --- 燈箱相關函數 ---

    let currentOperation = null; // 儲存當前燈箱的操作類型 (report/delete)
    let currentCommentId = null; // 儲存當前操作的留言 ID

    function openModal(title, message, operationType, commentId) {
        modalTitle.textContent = title;
        modalMessage.textContent = message;
        confirmActionButton.className = 'confirm-btn ' + operationType; // 根據操作類型添加 class
        currentOperation = operationType;
        currentCommentId = commentId;
        actionModal.classList.add('show');
    }

    function closeModal() {
        actionModal.classList.remove('show');
        currentOperation = null;
        currentCommentId = null;
    }

    // 燈箱的取消按鈕和關閉按鈕
    modalCancelButton.addEventListener('click', closeModal);
    modalCloseButton.addEventListener('click', closeModal);
    // 點擊燈箱外部關閉 (點擊半透明背景)
    actionModal.addEventListener('click', (event) => {
        if (event.target === actionModal) { // 只有點擊 modal 本身（背景）才關閉
            closeModal();
        }
    });

    // 燈箱的確認按鈕事件
    confirmActionButton.addEventListener('click', async () => {
        if (!currentCommentId || !currentOperation) {
            console.error('無效的操作或留言 ID。');
            closeModal();
            return;
        }

        // 根據 currentOperation 執行不同的 API 請求
        if (currentOperation === 'report') {
            console.log(`執行檢舉操作，留言 ID: ${currentCommentId}`);
            showStatusMessage(`正在檢舉留言 ID: ${currentCommentId}...`, 'info');
            try {
                // 假設你的檢舉 API 是 /commentsAPI/reportComment/{id} (POST 或 PUT)
                const response = await fetch(`/commentsAPI/reportComment/${currentCommentId}`, {
                    method: 'POST', // 或 'PUT'
                    headers: {
                        'Content-Type': 'application/json'
                        // 如果需要認證，請添加 'Authorization' 標頭
                    },
                    body: JSON.stringify({ commentId: currentCommentId, reason: "使用者檢舉", reporterId: CURRENT_USER_ID }) // 可以傳送更多檢舉資訊
                });

                if (response.ok) {
                    showStatusMessage(`留言 ID: ${currentCommentId} 檢舉成功！`, 'success');
                    // 可以在前端更新 UI，例如標記已檢舉 (但通常檢舉是後台處理，前端不需即時顯示)
                } else {
                    const errorData = await response.json();
                    showStatusMessage(`檢舉失敗: ${errorData.message || response.statusText}`, 'error');
                }
            } catch (error) {
                console.error('檢舉請求錯誤:', error);
                showStatusMessage('檢舉請求失敗，請檢查網路。', 'error');
            }
        } else if (currentOperation === 'delete') {
            console.log(`執行刪除操作，留言 ID: ${currentCommentId}`);
            showStatusMessage(`正在刪除留言 ID: ${currentCommentId}...`, 'info');
            try {
                // 假設你的刪除 API 是 /commentsAPI/deleteComment/{id} (DELETE)
                const response = await fetch(`/commentsAPI/deleteComment/${currentCommentId}`, {
                    method: 'DELETE',
                    headers: {
                        // 如果需要認證，請添加 'Authorization' 標頭
                        // 也可以在請求體中傳遞使用者ID，讓後端驗證權限
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ userId: CURRENT_USER_ID }) // 傳遞當前使用者ID以供後端驗證
                });

                if (response.ok) {
                    showStatusMessage(`留言 ID: ${currentCommentId} 刪除成功！`, 'success');
                    // 從 DOM 中移除被刪除的留言項目
                    const deletedCommentElement = document.querySelector(`[data-comment-id="${currentCommentId}"]`);
                    if (deletedCommentElement) {
                        deletedCommentElement.remove();
                    }
                } else {
                    const errorData = await response.json();
                    showStatusMessage(`刪除失敗: ${errorData.message || response.statusText}`, 'error');
                }
            } catch (error) {
                console.error('刪除請求錯誤:', error);
                showStatusMessage('刪除請求失敗，請檢查網路。', 'error');
            }
        }
        closeModal(); // 操作完成後關閉燈箱
    });

});