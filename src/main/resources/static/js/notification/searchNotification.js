document.addEventListener("DOMContentLoaded", function() {
	
	//會員選取器
	$('#notificationMemberId').multiSelect({ // 注意這裡的方法名是小寫開頭的 'multiselect'
			nonSelectedText: '請選擇選項',
			enableFiltering: true, // 啟用搜尋功能
			includeSelectAllOption: true, // 允許選擇所有選項 (可選)
			buttonWidth: '100%', // 按鈕寬度 (可選)
			selectionHeader: "<label for='notificationMemberId'>查詢會員</label>",
			selectableHeader: "<label>會員清單</label>",
			selectableFooter: "<p style='font-size: 10px; text-align: center;'>點選上面要查詢的對象</p>",
		});
		
	//時間邏輯判斷
	let startTimeInput = document.getElementById('notificationTimeStart');
	let endTimeInput = document.getElementById('notificationTimeEnd');
	let searchForm = document.querySelector('.admin-notification-form form'); // 獲取查詢表單

	const timeErrorDiv = document.createElement('div');
    timeErrorDiv.className = 'error-message'; // 添加一個 class 以便樣式化
    timeErrorDiv.style.color = 'red';
    timeErrorDiv.style.marginTop = '0px'; // 調整位置
    timeErrorDiv.style.marginBottom = '10px'; // 調整位置
    timeErrorDiv.style.fontSize = '0.9em';
    endTimeInput.parentNode.insertBefore(timeErrorDiv, endTimeInput.nextSibling); // 插入到結束時間輸入框之後
		
    function validateTimeRange() {
        const startTime = new Date(startTimeInput.value);
        const endTime = new Date(endTimeInput.value);

        // 如果兩個時間都有值才進行比較
        if (startTimeInput.value && endTimeInput.value) {
            if (startTime > endTime) {
                timeErrorDiv.textContent = '結束時間不能早於開始時間！';
                endTimeInput.setCustomValidity('結束時間不能早於開始時間！'); // HTML5 自訂驗證訊息
                return false; // 驗證失敗
            } else {
                timeErrorDiv.textContent = '';
                endTimeInput.setCustomValidity(''); // 清除驗證訊息
                return true; // 驗證成功
            }
        } else {
            timeErrorDiv.textContent = ''; // 如果其中一個為空，清除錯誤訊息
            endTimeInput.setCustomValidity('');
            return true; // 允許提交
        }
    }

    // 監聽時間輸入框的變化事件
    startTimeInput.addEventListener('change', validateTimeRange);
    endTimeInput.addEventListener('change', validateTimeRange);

    // 監聽表單提交事件，阻止不合法的提交
    searchForm.addEventListener('submit', function(event) {
        if (!validateTimeRange()) {
            event.preventDefault(); // 阻止表單提交
            // 可以選擇性地滾動到錯誤訊息位置
            timeErrorDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    });
	
	//查詢結果綁定事件可以進入詳情
	let searchResult = document.getElementsByClassName("noti-table-line");
	for(let i = 0; i < searchResult.length; i++){
		searchResult[i].addEventListener("click", function(){
			let form = document.createElement("form");
				form.method = "post";
				form.action = "/notification/editNotification";
				
			let notiInput = document.createElement("input");
				notiInput.type = "hidden";
				notiInput.name = "notiId";
				notiInput.value = searchResult[i].getAttribute("data-notiId");
				form.appendChild(notiInput);
				
			document.body.appendChild(form);
			form.submit();
		});
	}

});