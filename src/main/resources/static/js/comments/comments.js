/**
 * @file 留言區塊的主要 JavaScript 檔案。
 * 使用 Shadow DOM 將留言區塊的 HTML、CSS 和 JavaScript 完全封裝，
 * 避免與主頁面的樣式和腳本產生衝突。
 */

/**
 * @description 跳轉到登入頁面，並儲存當前請求資訊以便登入後恢復。
 */
function redirectToLoginPage() {
	console.log("未登入或需要重新登入，跳轉到登入頁面...");

	alert("請登入後重新操作一次!");

	// 實際的跳轉邏輯
	// 你需要將當前頁面的 URL 作為回調參數傳遞給登入頁面，登入成功後導回
	const currentPath = window.location.pathname + window.location.search;
	window.location.href = "/members/login?redirect=" + encodeURIComponent(currentPath);
}



/**
 * 初始化留言區塊的所有功能。
 * @param {ShadowRoot} shadowRoot - 封裝留言區塊的 Shadow DOM 根節點。
 */
function initComments(shadowRoot) {
	// --- DOM 元素獲取 ---
	const commentForm = shadowRoot.getElementById('commentForm');
	const statusMessage = shadowRoot.getElementById('statusMessage');
	const commentsList = shadowRoot.getElementById('commentsList');
	const commentImageInput = shadowRoot.getElementById('commentImage');
	const imagePreviewContainer = shadowRoot.getElementById('image-preview');
	const commentTextarea = shadowRoot.getElementById('commentContent');

	// --- 從 URL 獲取文章 ID ---
	const params = new URLSearchParams(window.location.search);
	const artId = params.get('artId');

	// --- 獲取當前登入使用者 ID ---
	// 建議作法：從主文件 meta 標籤讀取，由後端（Thymeleaf）設定。
	let CURRENT_USER_ID = null;
	if (document.getElementById("currentUserNav")) {
		CURRENT_USER_ID = document.getElementById("currentUserNav").value;
		console.log(CURRENT_USER_ID);
	}


	// 新增變數來儲存文章類別和擁有者 ID
	let ARTICLE_CATEGORY = null;
	let ARTICLE_OWNER_ID = null;

	// 新增函式來獲取文章詳細資訊
	async function fetchArticleDetails() {
		try {
			const response = await fetch(`/article/${artId}`);
			if (!response.ok) {
				throw new Error(`無法獲取文章資料: ${response.status} ${response.statusText}`);
			}
			const article = await response.json();
			ARTICLE_CATEGORY = article.artCat; // 假設 artCat 是文章類別的數字代碼
			ARTICLE_OWNER_ID = article.artHol; // 假設 artHol 是文章擁有者的 ID
		} catch (error) {
			console.error('載入文章詳情失敗:', error);
			showStatusMessage('載入文章詳情失敗，部分功能可能受限。', 'error');
		}
	}

	// --- 事件監聽器：提交新留言 ---
	commentForm.addEventListener('submit', async (event) => {
		event.preventDefault(); // 防止表單傳統提交
		statusMessage.style.display = 'none'; // 隱藏舊的狀態訊息

		//判斷是否有登入
		if (!document.getElementById("currentUserNav")) {
			redirectToLoginPage();
			rerurn;
		}

		const formData = new FormData(commentForm);
		formData.append("commArt", artId); // 附加文章 ID
		// 如果有登入者，才附加留言者 ID
		if (CURRENT_USER_ID) {
			formData.append("commHol", CURRENT_USER_ID);
		}

		// 如果未選擇圖片，則從 FormData 中移除，避免後端收到空檔案
		const imageFile = formData.get('commentImage');
		if (imageFile && imageFile.name === '' && imageFile.size === 0) {
			formData.delete('commentImage');
		}

		try {
			const response = await fetch('/commentsAPI/addComments', {
				method: 'POST',
				body: formData,
			});

			if (response.ok) {
				const newComment = await response.json();
				displayComment(newComment); // 在列表頂部顯示新留言
				showStatusMessage('留言新增成功！', 'success');
				commentForm.reset(); // 清空表單
				imagePreviewContainer.innerHTML = ""; // 清空圖片預覽
				imagePreviewContainer.style.display = 'none';

				// 手動移除 MaterializeCSS textareaAutoResize 可能殘留的隱藏 div
				const hiddendiv = shadowRoot.querySelector('.hiddendiv.common');
				if (hiddendiv) {
					hiddendiv.remove();
				}
			} else {
				const errorData = await response.json();
				showStatusMessage(`留言新增失敗: ${errorData.message || response.statusText}`, 'error');
			}
		} catch (error) {
			console.error('網路或伺服器錯誤:', error);
			showStatusMessage('請求失敗，請檢查網路連線或伺服器狀態。', 'error');
		}
	});

	/**
	 * 顯示狀態訊息（如成功、失敗）。
	 * @param {string} message - 要顯示的訊息內容。
	 * @param {'success' | 'error' | 'info'} type - 訊息的類型。
	 */
	function showStatusMessage(message, type) {
		statusMessage.textContent = message;
		statusMessage.className = `message ${type}`;
		statusMessage.style.display = 'block';
	}

	/**
	 * 將一則留言的資料轉換為 HTML 元素並顯示在頁面上。
	 * @param {object} comment - 從後端 API 獲取的留言物件。
	 * @returns {HTMLElement} 建立的留言 DOM 元素。
	 */
	function displayComment(comment) {
		const commentItem = document.createElement('div');
		commentItem.className = 'comment-item card';
		commentItem.dataset.commentId = comment.commId;
		commentItem.dataset.userId = comment.commHol;

		// 顯示留言者名稱
		const authorStrong = document.createElement('strong');
		authorStrong.textContent = comment.holName;
		commentItem.appendChild(authorStrong);

		// 顯示留言時間
		if (comment.commCreTime) {
			const date = new Date(comment.commCreTime);
			const formattedTime = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
			const timeParagraph = document.createElement('p');
			timeParagraph.className = 'comment-time';
			timeParagraph.textContent = formattedTime;
			commentItem.appendChild(timeParagraph);
		}

		// 顯示留言內容
		const contentParagraph = document.createElement('pre');
		contentParagraph.className = 'comment-content-display';
		contentParagraph.textContent = comment.commCon;
		commentItem.appendChild(contentParagraph);

		// 如果有圖片，則顯示圖片
		if (comment.commImg) {
			const img = document.createElement('img');
			img.src = `/commentsReport/read.do?commId=${comment.commId}&t=${new Date().getTime()}`;
			img.alt = '留言圖片';
			img.classList.add('comment-main-image'); // 添加 class 以便在編輯時找到它
			img.style.maxWidth = "250px";
			commentItem.appendChild(img);
		}

		// --- 建立操作選單（...按鈕）---
		const actionsDiv = document.createElement('div');
		actionsDiv.className = 'comment-actions';

		const actionsButton = document.createElement('button');
		actionsButton.className = 'actions-button btn-flat';
		actionsButton.innerHTML = '<i class="material-icons">more_vert</i>';

		// Manually toggle dropdown visibility
		actionsButton.addEventListener('click', (e) => {
			e.stopPropagation(); // 防止文件點擊事件

			closeAllDropdowns(); // 關閉其他選單
			dropdownMenu.style.display = 'block';

			// 取得選單與 Shadow DOM 的相對位置
			const buttonRect = actionsButton.getBoundingClientRect();
			const menuRect = dropdownMenu.getBoundingClientRect();
			const shadowRect = shadowRoot.host.getBoundingClientRect(); // Shadow Host 的位置

			// 計算選單是否會超出右邊界
			const overflowRight = buttonRect.left + menuRect.width > shadowRect.right;

			// 如果會超出右邊界，靠左對齊；否則靠右
			if (overflowRight) {
				dropdownMenu.style.left = 'auto';
				dropdownMenu.style.right = '0px';
			} else {
				dropdownMenu.style.left = '0px';
				dropdownMenu.style.right = 'auto';
			}
		});

		const dropdownMenu = document.createElement('ul');
		dropdownMenu.id = `dropdown-${comment.commId}`;
		dropdownMenu.className = 'dropdown-content';
		dropdownMenu.style.display = 'none'; // Initially hidden
		dropdownMenu.style.position = 'absolute';
		dropdownMenu.style.backgroundColor = 'white';
		dropdownMenu.style.border = '1px solid #ccc';
		dropdownMenu.style.zIndex = '1000';
		dropdownMenu.style.minWidth = '150px';
		dropdownMenu.style.boxShadow = '0 2px 5px 0 rgba(0, 0, 0, 0.16), 0 2px 10px 0 rgba(0, 0, 0, 0.12)';
		dropdownMenu.style.opacity = '1'; // Ensure it's not transparent
		dropdownMenu.style.visibility = 'visible'; // Ensure it's visible

		// --- 根據是否為本人留言，決定顯示不同選單項目 ---
		if (comment.commHol == CURRENT_USER_ID) {
			// 本人的留言：顯示修改和刪除
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
		} else {
			// 他人的留言：只顯示檢舉
			const reportLi = document.createElement('li');
			const reportLink = document.createElement('a');
			reportLink.href = '#!';
			reportLink.className = 'action-report';
			reportLink.dataset.action = 'report';
			reportLink.textContent = '檢舉';
			reportLi.appendChild(reportLink);
			dropdownMenu.appendChild(reportLi);
		}

		actionsDiv.appendChild(actionsButton);
		actionsDiv.appendChild(dropdownMenu);
		commentItem.appendChild(actionsDiv);

		// --- 處理最佳解邏輯 ---
		if (ARTICLE_CATEGORY === 2 && CURRENT_USER_ID === ARTICLE_OWNER_ID) { // 發問中 & 文章擁有者
			const bestAnswerButton = document.createElement('button');
			bestAnswerButton.className = 'btn-small waves-effect waves-light green darken-1 best-answer-button';
			bestAnswerButton.textContent = '選我最佳解';
			bestAnswerButton.style.marginLeft = 'auto'; // 將按鈕推到右邊
			bestAnswerButton.addEventListener('click', async () => {
				if (confirm('確定要選擇此留言為最佳解嗎？')) {
					try {
						const formData = new FormData();
						formData.append('commId', comment.commId);
						formData.append('artId', artId);
						const response = await fetch('/commentsAPI/bestAnswer', {
							method: 'POST',
							body: formData // 傳遞 commId 和 artId
						});
						if (response.ok) {
							alert('問題已解決！');
							location.reload(); // 重新整理頁面以顯示更新後的狀態
						} else {
							const errorData = await response.json();
							showStatusMessage(`設定最佳解失敗: ${errorData.message || response.statusText}`, 'error');
						}
					} catch (error) {
						console.error('設定最佳解請求失敗:', error);
						showStatusMessage('設定最佳解請求失敗，請檢查網路連線。', 'error');
					}
				}
			});
			actionsDiv.appendChild(bestAnswerButton); // 將按鈕添加到 actionsDiv
		} else if (ARTICLE_CATEGORY === 3 && comment.commSta === 3) { // 已解決 & 是最佳解
			const bestAnswerBlock = document.createElement('div');
			bestAnswerBlock.style.display = 'flex';
			bestAnswerBlock.style.alignItems = 'center';
			bestAnswerBlock.style.marginBottom = "10px";
			bestAnswerBlock.className = 'best-answer-tag';
			//            const bestAnswerIcon = document.createElement('i');
			//            bestAnswerIcon.className = 'material-icons best-answer-icon';
			//            bestAnswerIcon.textContent = 'check_circle'; // 最佳解的 Material Icon
			//            bestAnswerIcon.style.color = 'gold'; // 顏色
			//            bestAnswerIcon.style.fontSize = '24px';
			//            bestAnswerBlock.appendChild(bestAnswerIcon);
			const bestAnswerword = document.createElement('span');
			bestAnswerword.innerHTML = "最佳解";
			bestAnswerword.style.fontWeight = 'bold';     // 設定字體加粗
			bestAnswerword.style.textDecoration = 'underline'; // 設定文字加底線
			bestAnswerBlock.appendChild(bestAnswerword);
			commentItem.prepend(bestAnswerBlock);
		}


		// --- 建立按讚按鈕和計數 ---
		const commentFooter = document.createElement('div');
		commentFooter.className = 'comment-footer';

		const likeButton = document.createElement('button');
		likeButton.className = 'like-button';
		likeButton.dataset.commentId = comment.commId;
		likeButton.innerHTML = '<i class="material-icons">thumb_up</i>';

		const likeCountSpan = document.createElement('span');
		likeCountSpan.className = 'like-count';
		likeCountSpan.dataset.commentId = comment.commId;
		likeCountSpan.textContent = comment.commLike !== undefined ? comment.commLike : 0;

		commentFooter.appendChild(likeButton);
		commentFooter.appendChild(likeCountSpan);
		commentItem.appendChild(commentFooter);

		// 將完成的留言項目插入到列表的最前面
		commentsList.prepend(commentItem); // 這行會被下面的排序邏輯取代

		//初始化按讚樣式
		fetch('/likeAPI/getCommLike', {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: new URLSearchParams({
				artId: artId,
				commId: comment.commId,
				memId: CURRENT_USER_ID
			})
		})
			.then(res => res.text()) // 注意：API 回傳的是純文字 true/false，不是 JSON
			.then(text => {
				const liked = text.trim() === 'true';
				if (liked) {
					likeButton.classList.add('liked');
				}
			})
			.catch(err => console.error('查詢按讚狀態失敗:', err));


		return commentItem;
	}

	/**
	 * 關閉所有已開啟的下拉選單。
	 */
	function closeAllDropdowns() {
		shadowRoot.querySelectorAll('.dropdown-content').forEach(menu => {
			menu.style.display = 'none';
		});
	}

	// --- 事件委派：處理所有留言列表中的點擊事件 ---
	commentsList.addEventListener('click', async (event) => {
		const target = event.target;

		// 判斷點擊的是否為操作按鈕（檢舉、修改、刪除）
		if (target.matches('.action-report, .action-edit, .action-delete')) {
			closeAllDropdowns(); // 先關閉其他選單
			const action = target.dataset.action;
			const commentItem = target.closest('.comment-item');
			const commentId = parseInt(commentItem.dataset.commentId, 10);

			//判斷是否有登入
			if (!document.getElementById("currentUserNav")) {
				redirectToLoginPage();
				return;
			}

			switch (action) {
				case 'report':
					const userInput = window.prompt(`確定檢舉這條留言，請輸入檢舉原因...`);
					if (userInput) {
						showStatusMessage(`正在檢舉留言 ID: ${commentId}...`, 'info');
						try {
							const response = await fetch(`/CommentsReportsAPI/addCommentsReports`, {
								method: 'POST',
								headers: { 'Content-Type': 'application/json' },
								body: JSON.stringify({ commId: commentId, repCat: "使用者檢舉", repDes: userInput, memId: CURRENT_USER_ID })
							});
							if (response.ok) {
								showStatusMessage(`留言 ID: ${commentId} 檢舉成功！`, 'success');
							} else {
								const errorData = await response.json();
								showStatusMessage(`檢舉失敗: ${errorData.message || response.statusText}`, 'error');
							}
						} catch (error) {
							showStatusMessage('檢舉請求失敗，請檢查網路。', 'error');
						}
					}
					break;
				case 'edit':
					startEditComment(commentItem, commentId);
					break;
				case 'delete':
					if (window.confirm(`確定要刪除這條留言嗎？此操作無法恢復。`)) {
						showStatusMessage(`正在刪除留言 ID: ${commentId}...`, 'info');
						try {
							const formData = new FormData();
							formData.append('commId', commentId);
							const response = await fetch(`/commentsAPI/deleteComments`, {
								method: 'POST',
								body: formData,
							});
							if (response.ok) {
								showStatusMessage(`留言 ID: ${commentId} 刪除成功！`, 'success');
								commentItem.remove(); // 從畫面上移除
							} else {
								const errorData = await response.json();
								showStatusMessage(`刪除失敗: ${errorData.message || response.statusText}`, 'error');
							}
						} catch (error) {
							showStatusMessage('刪除請求失敗，請檢查網路。', 'error');
						}
					}
					break;
			}
		} else if (target.closest('.like-button')) {
			// 處理按讚按鈕點擊
			const likeButton = target.closest('.like-button');
			const commentId = likeButton.dataset.commentId;
			handleLikeComment(commentId, likeButton);
		}
	});

	// --- 事件監聽器：點擊留言區塊外部時，關閉選單 ---
	shadowRoot.addEventListener('click', (event) => {
		if (!event.target.closest('.comment-actions')) { // 點擊非操作按鈕區域時關閉
			closeAllDropdowns();
		}
	});

	/**
	 * 處理按讚/收回讚的邏輯。
	 * @param {string} commentId - 被按讚的留言 ID。
	 * @param {HTMLElement} likeButton - 被點擊的按讚按鈕。
	 */
	async function handleLikeComment(commentId, likeButton) {
		const likeCountSpan = commentsList.querySelector(`.like-count[data-comment-id="${commentId}"]`);
		if (!likeCountSpan) return;

		//判斷是否登入
		if (!document.getElementById("currentUserNav")) {
			redirectToLoginPage();
			return;
		}


		try {
			const likeFormData = new FormData();
			likeFormData.append('docId', commentId);
			likeFormData.append('parDocId', artId);
			likeFormData.append('memId', CURRENT_USER_ID);

			const response = await fetch('/likeAPI/dolike', {
				method: 'POST',
				body: likeFormData,
			});

			if (response.ok) {
				const updatedLikeCount = await response.json();
				likeCountSpan.textContent = updatedLikeCount; // 更新按讚數
				if (updatedLikeCount > 0) {
					likeButton.classList.add('liked');
				} else {
					likeButton.classList.remove('liked');
				}
			} else {
				const errorText = await response.text();
				showStatusMessage(`按讚失敗: ${errorText}`, 'error');
			}
		} catch (error) {
			showStatusMessage('按讚請求失敗，請檢查網路。', 'error');
		}
	}

	/**
	 * 啟動留言的「就地編輯」模式。
	 * @param {HTMLElement} originalCommentItem - 要編輯的原始留言 DOM 元素。
	 * @param {string} commentId - 要編輯的留言 ID。
	 */
	async function startEditComment(originalCommentItem, commentId) {
		// 顯示載入提示
		originalCommentItem.innerHTML = '<p>正在載入編輯器...</p>';

		const updateFormData = new FormData();
		updateFormData.append('commHol', commentId);
		// 1. 呼叫 API 獲取最新的留言資料
		const response = await fetch(`/commentsAPI/getOneComment`, {
			method: 'POST',
			body: updateFormData
		});
		if (!response.ok) {
			throw new Error('無法獲取留言資料');
		}
		const comment = await response.json();

		// 清空載入提示
		originalCommentItem.innerHTML = '';

		// --- 建立編輯介面 ---
		const editContainer = document.createElement('div');
		editContainer.className = 'edit-container';

		// 2. 文字編輯區
		const textarea = document.createElement('textarea');
		textarea.className = 'materialize-textarea';
		textarea.value = comment.commCon;
		editContainer.appendChild(textarea);

		// 3. 圖片預覽和操作區
		const editImagePreview = document.createElement('div');
		editImagePreview.className = 'image-preview-wrapper';
		if (comment.commImg) {
			editImagePreview.innerHTML = `
                    <img src="/commentsReport/read.do?commId=${comment.commId}" alt="現有圖片">
                    <span class="remove-preview">&times;</span>
                `;
		}
		editContainer.appendChild(editImagePreview);

		// 4. 檔案選擇器
		const fileInput = document.createElement('input');
		fileInput.type = 'file';
		fileInput.id = `edit-comment-image-${commentId}`; // 為 input 建立唯一 ID
		fileInput.accept = 'image/*';
		fileInput.style.display = 'none';
		editContainer.appendChild(fileInput);

		const fileInputLabel = document.createElement('label');
		fileInputLabel.htmlFor = `edit-comment-image-${commentId}`; // 將 label 的 for 屬性指向 input 的 ID
		fileInputLabel.className = 'btn-flat waves-effect waves-light';
		fileInputLabel.innerHTML = '<i class="material-icons">photo_camera</i> 更換圖片';
		fileInputLabel.style.cursor = 'pointer';
		editContainer.appendChild(fileInputLabel);

		// --- 建立儲存和取消按鈕 ---
		const saveButton = document.createElement('button');
		saveButton.className = 'btn waves-effect waves-light green darken-1';
		saveButton.textContent = '儲存';

		const cancelButton = document.createElement('button');
		cancelButton.className = 'btn waves-effect waves-light grey';
		cancelButton.textContent = '取消';

		const buttonContainer = document.createElement('div');
		buttonContainer.className = 'edit-buttons';
		buttonContainer.appendChild(saveButton);
		buttonContainer.appendChild(cancelButton);
		editContainer.appendChild(buttonContainer);

		originalCommentItem.appendChild(editContainer);

		M.textareaAutoResize(textarea);
		textarea.focus();

		// --- 編輯介面的事件處理 ---
		let imageRemoved = false;

		const removeBtn = editImagePreview.querySelector('.remove-preview');
		if (removeBtn) {
			removeBtn.onclick = () => {
				editImagePreview.innerHTML = '';
				imageRemoved = true;
			};
		}

		fileInput.addEventListener('change', (event) => {
			const file = event.target.files[0];
			if (file) {
				const reader = new FileReader();
				reader.onload = (e) => {
					editImagePreview.innerHTML = `
                            <img src="${e.target.result}" alt="新圖片預覽">
                            <span class="remove-preview">&times;</span>
                        `;
					editImagePreview.querySelector('.remove-preview').onclick = () => {
						editImagePreview.innerHTML = '';
						fileInput.value = '';
						imageRemoved = true;
					};
				};
				reader.readAsDataURL(file);
				imageRemoved = false;
			}
		});

		// --- 儲存和取消的邏輯 ---
		const exitEditMode = () => {
			// 用最新的留言資料重新渲染，恢復原樣
			const newCommentItem = displayComment(comment);
			originalCommentItem.parentNode.replaceChild(newCommentItem, originalCommentItem);
		};

		cancelButton.addEventListener('click', exitEditMode);

		saveButton.addEventListener('click', async () => {
			const newContent = textarea.value.trim();
			if (!newContent) return alert('留言內容不能為空！');

			const updateFormData = new FormData();
			updateFormData.append('commId', commentId);
			updateFormData.append('commCon', newContent);

			if (fileInput.files[0]) {
				updateFormData.append('commImg', fileInput.files[0]);
			} else if (imageRemoved) {
				updateFormData.append('removeImage', 'true');
			} else if (comment.commImg) { // If original comment had an image and it's not removed or replaced
				updateFormData.append('keepImage', 'true');
			}

			try {
				const updateResponse = await fetch(`/commentsAPI/updateComments`, {
					method: 'POST',
					body: updateFormData,
				});

				if (updateResponse.ok) {
					const updatedComment = await updateResponse.json();
					console.log('Updated comment from API:', updatedComment);
					// 用 API 回傳的最新資料重新渲染留言
					const newCommentItem = displayComment(updatedComment);
					originalCommentItem.parentNode.replaceChild(newCommentItem, originalCommentItem);
					showStatusMessage('留言更新成功！', 'success');
				} else {
					const errorData = await updateResponse.json();
					showStatusMessage(`更新失敗: ${errorData.message || updateResponse.statusText}`, 'error');
					exitEditMode(); // 更新失敗時也恢復原狀
				}
			} catch (error) {
				showStatusMessage('更新請求失敗，請檢查網路。', 'error');
				exitEditMode(); // 發生錯誤時也恢復原狀
			}
		});
	} // <-- 這裡缺少一個大括號，導致 "Unexpected end of input" 錯誤


	// --- 事件監聽器：輸入時自動調整留言框高度 ---
	commentTextarea.addEventListener('input', function() {
		M.textareaAutoResize(this);
	});

	// --- 事件監聽器：處理圖片預覽 ---
	commentImageInput.addEventListener('change', function(event) {
		const file = event.target.files[0];
		if (file) {
			const reader = new FileReader();
			reader.onload = function(e) {
				imagePreviewContainer.innerHTML = ''; // 清空舊預覽
				const imageWrapper = document.createElement('div');
				imageWrapper.className = 'image-preview-wrapper';
				const img = document.createElement('img');
				img.src = e.target.result;
				const removeBtn = document.createElement('span');
				removeBtn.className = 'remove-preview';
				removeBtn.innerHTML = '&times;';
				removeBtn.onclick = function() {
					imagePreviewContainer.innerHTML = '';
					imagePreviewContainer.style.display = 'none';
					commentImageInput.value = ''; // 清空 file input
				};
				imageWrapper.appendChild(img);
				imageWrapper.appendChild(removeBtn);
				imagePreviewContainer.appendChild(imageWrapper);
				imagePreviewContainer.style.display = 'block';
			}
			reader.readAsDataURL(file);
		}
	});

	/**
	 * 從後端 API 獲取並載入所有留言。
	 */
	async function fetchComments() {
		try {
			// 確保文章資訊已載入
			if (ARTICLE_CATEGORY === null || ARTICLE_OWNER_ID === null) {
				await fetchArticleDetails();
			}

			const formData = new FormData();
			formData.append('commArt', artId);
			const response = await fetch('/commentsAPI/getComments', {
				method: 'POST',
				body: formData,
			});

			if (response.ok) {
				let comments = await response.json();
				commentsList.innerHTML = ''; // 清空現有列表

				comments.forEach(comment => commentsList.appendChild(displayComment(comment))); // 顯示每則留言

			} else {
				showStatusMessage('無法載入留言，請稍後再試。', 'error');
			}
		} catch (error) {
			console.error('載入留言請求錯誤:', error);
			showStatusMessage('載入留言失敗，請檢查網路連線。', 'error');
		}
	}

	// --- 初始化：立即載入留言 ---
	fetchComments();
} // This is the missing brace for initComments function

// --- 主程式進入點：直接呼叫 initComments ---
const commentsBlockContainer = document.getElementById("commentsBlock");
if (commentsBlockContainer) {
	const shadow = commentsBlockContainer.attachShadow({ mode: "open" });

	// 將原始 HTML 內容暫存到一個 wrapper 中
	const wrapper = document.createElement("div");
	wrapper.innerHTML = commentsBlockContainer.innerHTML;
	commentsBlockContainer.innerHTML = ""; // 清空原始容器

	// --- 非同步載入所有 CSS 樣式 ---
	const styleSources = [
		"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css",
		"/css/comments/comments.css", // 你的自訂樣式
		"https://fonts.googleapis.com/icon?family=Material+Icons" // Material 圖示字體
	];
	let stylesLoaded = 0;

	// 定義一個回呼函式，在所有樣式都載入完成後執行
	const onStyleLoad = () => {
		stylesLoaded++;
		if (stylesLoaded === styleSources.length) {
			shadow.appendChild(wrapper); // 將 HTML 內容放回 Shadow DOM
			requestAnimationFrame(() => {
				initComments(shadow);
			});
		}
	};

	// 遍歷並載入所有 CSS 檔案
	styleSources.forEach(src => {
		const link = document.createElement("link");
		link.rel = "stylesheet";
		link.href = src;
		link.onload = onStyleLoad;
		link.onerror = onStyleLoad; // 即使某個 CSS 載入失敗，也繼續執行以避免卡住
		shadow.appendChild(link);
	});
}