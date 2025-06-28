document.addEventListener("DOMContentLoaded", function() {
    const commentForm = document.getElementById('commentForm');
    const statusMessage = document.getElementById('statusMessage');
    const commentsList = document.getElementById('commentsList');
	
	const params = new URLSearchParams(window.location.search);
	const artId = params.get('artId');
	
    // !!! 重要 !!!
    // 模擬當前登入使用者的 ID (實際應用中，這會從後端取得)
    // 例如：從一個隱藏的 input 欄位讀取，或者在頁面載入時透過 AJAX 請求獲取。
    // 在這裡我設定為 'user1' 作為範例。
    // 請根據你的後端邏輯，替換成實際登入使用者的 ID。
    const CURRENT_USER_ID = 1;

    // 獲取圖片輸入元素和預覽容器
    const commentImageInput = document.getElementById('commentImage');
    const imagePreviewContainer = document.getElementById('image-preview');
    const commentTextarea = document.getElementById('commentContent');

    // 監聽表單提交事件
    commentForm.addEventListener('submit', async (event) => {
        event.preventDefault(); // 阻止表單的預設提交行為

        statusMessage.style.display = 'none'; // 隱藏之前的狀態訊息

        const formData = new FormData(commentForm); // 從表單建立 FormData 物件
		formData.append("commArt", artId);
		formData.append("commHol", CURRENT_USER_ID);

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

            if (response.ok) {
                const newComment = await response.json();
                displayComment(newComment);

                showStatusMessage('留言新增成功！', 'success');
                commentForm.reset();
                imagePreviewContainer.innerHTML = "";
                imagePreviewContainer.style.display = 'none';
            } else {
                const errorData = await response.json();
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
        commentItem.className = 'comment-item card';
        commentItem.dataset.commentId = comment.commId;
        commentItem.dataset.userId = comment.memId;

        const authorStrong = document.createElement('strong');
        authorStrong.textContent = comment.author || '匿名使用者';
        commentItem.appendChild(authorStrong);

        if (comment.commCreTime) {
            const date = new Date(comment.commCreTime);
            const formattedTime = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
            const timeParagraph = document.createElement('p');
            timeParagraph.className = 'comment-time';
            timeParagraph.textContent = `${formattedTime}`;
            commentItem.appendChild(timeParagraph);
        }

        const contentParagraph = document.createElement('p');
        contentParagraph.className = 'comment-content-display';
        contentParagraph.textContent = comment.commCon;
        commentItem.appendChild(contentParagraph);

        if (comment.commImg) {
            const img = document.createElement('img');
            const commId = comment.commId;
            img.src = `/commentsReport/read.do?commId=${commId}`;
            img.alt = '留言圖片';
            commentItem.appendChild(img);
        }

        const actionsDiv = document.createElement('div');
        actionsDiv.className = 'comment-actions';

        const actionsButton = document.createElement('button');
        actionsButton.className = 'actions-button btn-flat dropdown-trigger';
        actionsButton.dataset.target = `dropdown-${comment.commId}`;
        actionsButton.innerHTML = '<i class="material-icons">more_vert</i>';
        actionsButton.setAttribute('aria-expanded', 'false');

        const dropdownMenu = document.createElement('ul');
        dropdownMenu.id = `dropdown-${comment.commId}`;
        dropdownMenu.className = 'dropdown-content';

        const reportLi = document.createElement('li');
        const reportLink = document.createElement('a');
        reportLink.href = '#!';
        reportLink.className = 'action-report';
        reportLink.dataset.action = 'report';
        reportLink.textContent = '檢舉';
        reportLi.appendChild(reportLink);
        dropdownMenu.appendChild(reportLi);

        if (comment.memId === CURRENT_USER_ID) {
            const editLi = document.createElement('li');
            const editLink = document.createElement('a');
            editLink.href = '#!';
            editLink.className = 'action-edit';
            editLink.dataset.action = 'edit';
            editLink.textContent = '修改';
            editLi.appendChild(editLink);
            dropdownMenu.appendChild(editLi);

            const deleteLi = document.createElement('li');
            const deleteLink = document.createElement('a');
            deleteLink.href = '#!';
            deleteLink.className = 'action-delete';
            deleteLink.dataset.action = 'delete';
            deleteLink.textContent = '刪除';
            deleteLi.appendChild(deleteLink);
            dropdownMenu.appendChild(deleteLi);
        }

        actionsDiv.appendChild(actionsButton);
        actionsDiv.appendChild(dropdownMenu);
        commentItem.appendChild(actionsDiv);

        // --- 新增按讚按鈕和按讚數 --- 
        const commentFooter = document.createElement('div');
        commentFooter.className = 'comment-footer';

        const likeButton = document.createElement('button');
        likeButton.className = 'like-button';
        likeButton.dataset.commentId = comment.commId; // 儲存留言 ID
        likeButton.innerHTML = '<i class="material-icons">thumb_up</i>';

        const likeCountSpan = document.createElement('span');
        likeCountSpan.className = 'like-count';
        likeCountSpan.dataset.commentId = comment.commId; // 儲存留言 ID
        // 直接使用 comment 物件中傳遞過來的 likeCount
        likeCountSpan.textContent = comment.commLike !== undefined ? comment.commLike : 0; 

        commentFooter.appendChild(likeButton);
        commentFooter.appendChild(likeCountSpan);
        commentItem.appendChild(commentFooter);

        commentsList.prepend(commentItem);

        M.Dropdown.init(actionsButton, { constrainWidth: false });
    }

    function closeAllDropdowns() {
        document.querySelectorAll('.dropdown-content.show').forEach(menu => {
            const instance = M.Dropdown.getInstance(menu.previousElementSibling);
            if (instance) {
                instance.close();
            }
        });
    }

    commentsList.addEventListener('click', async (event) => {
        const target = event.target;

        if (target.classList.contains('action-report') ||
            target.classList.contains('action-edit') ||
            target.classList.contains('action-delete')) {

            closeAllDropdowns();

            const action = target.dataset.action;
            const commentItem = target.closest('.comment-item');
            const commentId = parseInt(commentItem.getAttribute("data-comment-id"), 10);
            const userId = commentItem.dataset.userId;

            switch (action) {
                case 'report':
					let userInput = window.prompt(`確定檢舉這條留言，請輸入檢舉原因...`);
                    if (userInput) {
                        console.log(`執行檢舉操作，留言 ID: ${commentId}`);
                        showStatusMessage(`正在檢舉留言 ID: ${commentId}...`, 'info');
//						Integer memId, Integer commId, String repCat, String repDes
                        try {
                            const response = await fetch(`/CommentsReportsAPI/addCommentsReports`, {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ commId: commentId, repCat: "使用者檢舉", repDes: userInput, memId: CURRENT_USER_ID }) // 傳送更多檢舉資訊
                            });

                            if (response.ok) {
                                showStatusMessage(`留言 ID: ${commentId} 檢舉成功！`, 'success');
                            } else {
                                const errorData = await response.json();
                                showStatusMessage(`檢舉失敗: ${errorData.message || response.statusText}`, 'error');
                            }
                        } catch (error) {
                            console.error('檢舉請求錯誤:', error);
                            showStatusMessage('檢舉請求失敗，請檢查網路。', 'error');
                        }
                    }
                    break;
                case 'edit':
                    startEditComment(commentItem, commentId);
                    break;
                case 'delete':
                    if (window.confirm(`確定要刪除這條留言嗎？此操作無法恢復。`)) {
                        console.log(`執行刪除操作，留言 ID: ${commentId}`);
                        showStatusMessage(`正在刪除留言 ID: ${commentId}...`, 'info');
                        try {
                            // 使用 FormData 傳遞參數以符合後端 @RequestParam
                            const formData = new FormData();
                            formData.append('commId', commentId);

                            const response = await fetch(`/commentsAPI/deleteComments`, {
                                method: 'POST',
                                body: formData,
                            });

                            if (response.ok) {
                                showStatusMessage(`留言 ID: ${commentId} 刪除成功！`, 'success');
                                commentItem.remove();
                            } else {
                                const errorData = await response.json();
                                showStatusMessage(`刪除失敗: ${errorData.message || response.statusText}`, 'error');
                            }
                        } catch (error) {
                            console.error('刪除請求錯誤:', error);
                            showStatusMessage('刪除請求失敗，請檢查網路。', 'error');
                        }
                    }
                    break;
            }
        }
        else if (target.closest('.like-button')) {
            const likeButton = target.closest('.like-button');
            const commentId = likeButton.dataset.commentId;
            handleLikeComment(commentId, likeButton);
        }
    });

    document.addEventListener('click', (event) => {
        if (!event.target.closest('.comment-item') && !event.target.classList.contains('actions-button')) {
            closeAllDropdowns();
        }
    });

    /**
     * 處理按讚功能
     * @param {string} commentId - 留言的 ID
     * @param {HTMLElement} likeButton - 按讚按鈕的 DOM 元素
     */
    async function handleLikeComment(commentId, likeButton) {
        const likeCountSpan = commentsList.querySelector(`.like-count[data-comment-id="${commentId}"]`);
        if (!likeCountSpan) {
            console.error('找不到對應的按讚計數元素。');
            return;
        }

        try {
            // 呼叫 LikeController 的 dolike API
            // 這裡假設 docId 就是 commentId，parDocId 為 null (如果按讚的是留言本身)
            // 您可能需要根據實際的 LikeVO 結構調整這裡的屬性
            const likeFormData = new FormData();
            likeFormData.append('docId', commentId);
            likeFormData.append('parDocId', artId); // 傳遞空字串或 null，取決於後端如何處理 null
            likeFormData.append('memId', CURRENT_USER_ID);

            const response = await fetch('/likeAPI/dolike', {
                method: 'POST',
                body: likeFormData,
            });

            if (response.ok) {
                // dolike 現在直接返回按讚數
                const updatedLikeCount = await response.json(); // 假設返回的是純數字
                if (typeof updatedLikeCount === 'number') {
                    likeCountSpan.textContent = updatedLikeCount; // 更新顯示的按讚數
                    // 可以根據需要改變按鈕的顏色或圖示，表示已按讚
                    // 例如：likeButton.classList.toggle('liked');
                } else {
                    console.warn('dolike API 返回的數據格式不符合預期。', updatedLikeCount);
                }
            } else {
                // 嘗試解析錯誤訊息，如果不是 JSON 則使用 response.statusText
                const errorText = await response.text();
                try {
                    const errorData = JSON.parse(errorText);
                    showStatusMessage(`按讚失敗: ${errorData.message || response.statusText}`, 'error');
                } catch (e) {
                    showStatusMessage(`按讚失敗: ${response.statusText} - ${errorText.substring(0, 100)}...`, 'error');
                }
            }
        } catch (error) {
            console.error('按讚請求錯誤:', error);
            showStatusMessage('按讚請求失敗，請檢查網路。', 'error');
        }
    }


    /**
     * 開始編輯留言的就地編輯模式
     * @param {HTMLElement} commentItem - 留言的 DOM 元素
     * @param {string} commentId - 留言的 ID
     */
    function startEditComment(commentItem, commentId) {
        const contentDisplay = commentItem.querySelector('.comment-content-display');
        const originalContent = contentDisplay.textContent;

        // 創建 textarea
        const textarea = document.createElement('textarea');
        textarea.className = 'materialize-textarea';
        textarea.value = originalContent;
        textarea.style.width = '100%';
        textarea.style.minHeight = '80px';
        textarea.style.marginBottom = '10px';
        textarea.style.border = '1px solid #ccc';
        textarea.style.borderRadius = '4px';
        textarea.style.padding = '8px';
        textarea.style.boxSizing = 'border-box'; // 確保 padding 不會增加寬度

        // 隱藏顯示內容，插入 textarea
        contentDisplay.style.display = 'none';
        contentDisplay.parentNode.insertBefore(textarea, contentDisplay.nextSibling);

        // 創建儲存和取消按鈕
        const saveButton = document.createElement('button');
        saveButton.className = 'btn waves-effect waves-light green darken-1';
        saveButton.textContent = '儲存';
        saveButton.style.marginRight = '10px';

        const cancelButton = document.createElement('button');
        cancelButton.className = 'btn waves-effect waves-light grey';
        cancelButton.textContent = '取消';

        // 將按鈕添加到留言項目中
        const buttonContainer = document.createElement('div');
        buttonContainer.className = 'edit-buttons'; // 添加一個容器以便管理
        buttonContainer.appendChild(saveButton);
        buttonContainer.appendChild(cancelButton);
        commentItem.appendChild(buttonContainer);

        // 自動調整 textarea 大小
        M.textareaAutoResize(textarea);
        textarea.focus(); // 讓 textarea 獲得焦點

        // 儲存按鈕事件監聽器
        saveButton.addEventListener('click', async () => {
            const newContent = textarea.value;
            if (newContent.trim() === '') {
                alert('留言內容不能為空！');
                return;
            }

            showStatusMessage(`正在更新留言 ID: ${commentId}...`, 'info');
            try {
                // 使用 FormData 傳遞參數以符合後端 @RequestParam
                const updateFormData = new FormData();
                updateFormData.append('commId', commentId);
                updateFormData.append('commCon', newContent);
                // 傳遞一個空的 Blob 作為 commImg，以滿足後端 MultipartFile 的要求
                updateFormData.append('commImg', new Blob(), 'empty.txt'); 

                const response = await fetch(`/commentsAPI/updateComments`, {
                    method: 'POST', // CommentsAPIController 的 updateComments 是 POST
                    body: updateFormData,
                });

                if (response.ok) {
                    showStatusMessage(`留言 ID: ${commentId} 更新成功！`, 'success');
                    contentDisplay.textContent = newContent; // 更新顯示內容
                    exitEditMode();
                } else {
                    const errorData = await response.json();
                    showStatusMessage(`更新失敗: ${errorData.message || response.statusText}`, 'error');
                }
            } catch (error) {
                console.error('更新請求錯誤:', error);
                showStatusMessage('更新請求失敗，請檢查網路。', 'error');
            }
        });

        // 取消按鈕事件監聽器
        cancelButton.addEventListener('click', () => {
            exitEditMode();
        });

        // 退出編輯模式的輔助函數
        function exitEditMode() {
            textarea.remove(); // 移除 textarea
            buttonContainer.remove(); // 移除按鈕容器
            contentDisplay.style.display = 'block'; // 顯示原始內容
        }
    }

    // 自動調整 textarea 大小 (Materialize 會處理 materialize-textarea 類別，但為了健壯性保留)
    commentTextarea.addEventListener('input', function() {
        M.textareaAutoResize(this);
    });

    // 處理圖片選擇事件
    commentImageInput.addEventListener('change', function(event) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                imagePreviewContainer.innerHTML = ''; // 清除之前的預覽

                const imageWrapper = document.createElement('div'); // 創建新的圖片包裝器
                imageWrapper.className = 'image-preview-wrapper'; // 添加樣式類別

                const img = document.createElement('img'); // 創建圖片元素
                img.src = e.target.result; // 設定圖片來源為讀取到的資料 URL

                const removeBtn = document.createElement('span'); // 創建移除按鈕
                removeBtn.className = 'remove-preview'; // 添加樣式類別
                removeBtn.innerHTML = '&times;'; // 設定按鈕內容為 'x'
                removeBtn.onclick = function() { // 設定點擊事件
                    imagePreviewContainer.innerHTML = ''; // 清空預覽容器
                    imagePreviewContainer.style.display = 'none'; // 隱藏預覽容器
                    commentImageInput.value = ''; // 清空檔案輸入欄位，以便再次選擇相同檔案
                };

                imageWrapper.appendChild(img); // 將圖片添加到包裝器
                imageWrapper.appendChild(removeBtn); // 將移除按鈕添加到包裝器
                imagePreviewContainer.appendChild(imageWrapper); // 將包裝器添加到預覽容器
                imagePreviewContainer.style.display = 'block'; // 顯示預覽容器
            }
            reader.readAsDataURL(file); // 以 Data URL 格式讀取檔案內容
        }
    });

    // 可選：表單提交後清除預覽
    document.getElementById('commentForm').addEventListener('submit', function() {
        setTimeout(() => {
            imagePreviewContainer.innerHTML = ''; // 清空預覽容器
            imagePreviewContainer.style.display = 'none'; // 隱藏預覽容器
            // M.textareaAutoResize(commentTextarea); // 重新調整 textarea 高度 (如果需要)
        }, 100);
    });

    // 初始化 Materialize 的下拉選單 (dropdowns)
    const dropdowns = document.querySelectorAll('.dropdown-trigger');
    M.Dropdown.init(dropdowns, { constrainWidth: false });

    // --- 新增：載入留言資料 --- 
    async function fetchComments() {
        try {
            const formData = new FormData();
            formData.append('commArt', artId);

            const response = await fetch('/commentsAPI/getComments', {
                method: 'POST',
                body: formData,
            });

            if (response.ok) {
                const comments = await response.json();
                commentsList.innerHTML = ''; // 清空現有留言，重新載入
                // 遍歷所有留言，並使用 displayComment 函數將其顯示在頁面上
                comments.forEach(comment => displayComment(comment));
            } else {
                console.error('獲取留言失敗:', response.statusText);
                showStatusMessage('無法載入留言，請稍後再試。', 'error');
            }
        } catch (error) {
            console.error('載入留言請求錯誤:', error);
            showStatusMessage('載入留言失敗，請檢查網路連線。', 'error');
        }
    }

    // 頁面載入完成後立即載入留言
    fetchComments();
});