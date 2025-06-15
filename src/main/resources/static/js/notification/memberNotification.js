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

			let formData = this.querySelector(".notification-form-data");
			let action = formData.getAttribute("data-action");
			let notiId = formData.getAttribute("data-noti-id");
			let notiStatus = formData.getAttribute("data-noti-sta");

			//建立form標籤
			let form = document.createElement("form");
			form.method = "post";
			form.action = action;

			//建立action intup標籤
			let actionInput = document.createElement("input");
			actionInput.type = "hidden";
			actionInput.name = "action";
			actionInput.value = "getNotiDetail";

			//建立notiId intup標籤
			let notiIdIntup = document.createElement("input");
			notiIdIntup.type = "hidden";
			notiIdIntup.name = "notiId";
			notiIdIntup.value = notiId;
			
			//建立notiStatus intup標籤
			let notiStatusInput = document.createElement("input");
			notiStatusInput.type = "hidden";
			notiStatusInput.name = "notiStatus";
			notiStatusInput.value = notiStatus;

			//把input標籤放進form標籤
			form.appendChild(actionInput);
			form.appendChild(notiIdIntup);
			form.appendChild(notiStatusInput);

			//把form標籤放進body標籤
			document.body.appendChild(form);
			form.submit();
		})
	});

	//刪除資料的form
	deleteBtn.addEventListener("click", function() {
		let allNotiId = document.querySelectorAll(".notification-checkbox:checked");
		let action = deleteBtn.getAttribute("data-action");
		
		if (allNotiId.length == 0) {
			alert("至少選一個通知才能刪除!");
			return;
		} else {
			if(confirm("確定要刪除" + allNotiId.length + "個通知嗎?")){
			
				let form = document.createElement("form");
				form.method = "post";
				form.action = action;
	
				let actionInput = document.createElement("input");
				actionInput.type = "hidden";
				actionInput.name = "action";
				actionInput.value = "deleteNoti";
				form.appendChild(actionInput);
	
				allNotiId.forEach(function(id) {
					let notiIdInput = document.createElement("input");
					notiIdInput.type = "hidden";
					notiIdInput.name = "notiIds";
					notiIdInput.value = id.value;
					form.appendChild(notiIdInput);
				});
	
				document.body.appendChild(form);
				form.submit();

			}
		}

	});

});
