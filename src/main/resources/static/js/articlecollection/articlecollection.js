document.addEventListener('DOMContentLoaded', function() {
	let selectAllCheckbox = document.getElementsByClassName('selectAllNotifications')[0];
	let notificationCheckboxes = document.querySelectorAll('.notification-checkbox');
	let notificationContext = document.querySelectorAll('.notification-item');
	let deleteBtn = document.querySelector('.delete-selected-btn');


	// 全選/取消全選功能
	selectAllCheckbox.addEventListener('change', function() {
		notificationCheckboxes.forEach(checkbox => {
			checkbox.checked = this.checked;
		});
	});

	// 監聽個別通知的 checkbox，如果所有通知都被選中，則勾選全選 checkbox
	notificationCheckboxes.forEach(checkbox => {
		checkbox.addEventListener('change', function() {
			const allChecked = Array.from(notificationCheckboxes).every(cb => cb.checked);
			selectAllCheckbox.checked = allChecked;
		});
	});


	// 通知列表點擊動作綁定
	notificationContext.forEach(function(item) {
		item.addEventListener("click", function(e) {
			if (
				e.target.closest(".checkbox-container") ||
				e.target.classList.contains("notification-checkbox") ||
				e.target.tagName === "INPUT"
			) {
				e.stopPropagation(); // 阻止冒泡
				return;
			}

			this.getAttribute("data-artId");
			
			//建立form標籤
			let form = document.createElement("form");
			form.method = "get";
			form.action = "/forum/article";

			let Input = document.createElement("input");
			Input.type = "hidden";
			Input.name = "artId";
			Input.value = this.getAttribute("data-artId");
			form.appendChild(Input); 
			
			//把form標籤放進body標籤
			document.body.appendChild(form);
			form.submit();
		})
	});

	//刪除資料的form (修改為使用 Fetch API 提交 JSON)
	deleteBtn.addEventListener("click", function() {
		let checkedCheckboxes = document.querySelectorAll(".notification-checkbox:checked");
		let action = "/articleCollection/deleteLine";
		
		if (checkedCheckboxes.length === 0) {
			alert("至少選一個項目才能刪除!");
			return;
		} 
		
		if (confirm("確定要刪除" + checkedCheckboxes.length + "個項目嗎?")) {
			const dataToSend = [];
			checkedCheckboxes.forEach(function(checkbox) {
				// 假設您在 checkbox 上有 data-art-id 和 data-mem-id 屬性
				const artId = parseInt(checkbox.value); 
				const memId = document.getElementById("member").value; 

				if (!isNaN(artId) && !isNaN(memId)) {
					dataToSend.push({
						artId: artId,
						memId: memId
					});
				} else {
					console.error("無法獲取有效的 artId 或 memId 從 checkbox:", checkbox);
				}
			});

			fetch(action, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify(dataToSend) // 將 JavaScript 陣列轉換為 JSON 字串
			})
			.then(response => {
				if (!response.ok) {
					// 如果響應狀態碼不是 2xx，拋出錯誤
					throw new Error('Network response was not ok ' + response.statusText);
				}
				return response.text(); // 或者 response.json() 如果後端返回 JSON
			})
			.then(data => {
				console.log('Success:', data);
				alert("刪除成功!");
				// 重新載入頁面或更新 UI
				window.location.reload(); 
			})
			.catch((error) => {
				console.error('Error:', error);
				alert("刪除失敗: " + error.message);
			});
		}
	});

});
