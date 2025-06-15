document.addEventListener("DOMContentLoaded", function() {

	//會員選取器
	$('#notificationMemberId').multiSelect({ // 注意這裡的方法名是小寫開頭的 'multiselect'
		nonSelectedText: '請選擇選項',
		enableFiltering: true, // 啟用搜尋功能
		includeSelectAllOption: true, // 允許選擇所有選項 (可選)
		buttonWidth: '100%', // 按鈕寬度 (可選)
		selectionHeader: "<label for='notificationMemberId'>查詢會員</label>",
		selectableHeader: "<label>會員清單</label>",
		selectableFooter: "<p style='font-size: 10px; text-align: center;'>點選上面要發通知的對象</p>",
	});


	var successMessageElement = document.getElementById('successMessage');
		var message = successMessageElement.value;
		if (message) {
			alert(message);
		}

});