document.addEventListener("DOMContentLoaded", function() {

	sessionStorage.removeItem("oriUrl", window.location.pathname);
	
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

	// 使用事件委派來處理動態生成的 tr 元素的點擊事件
	$('#notificationSearchResultsTable tbody').on('click', 'tr.noti-table-line', function() {
		// this 現在指向被點擊的 tr 元素
		let notiId = $(this).data('notiid'); // 使用 jQuery 的 .data() 方法獲取 data-notiId 屬性值

		if (notiId) {
			let form = document.createElement("form");
			form.method = "post";
			form.action = "/notification/editNotification"; // 將目標 action 改為編輯頁面

			let notiInput = document.createElement("input");
			notiInput.type = "hidden";
			notiInput.name = "notiId";
			notiInput.value = notiId;
			form.appendChild(notiInput);

			document.body.appendChild(form);
			form.submit();
		}
	});

	// 將 selectedMemberIds 定義在更高層級的作用域
	let selectedMemberIds = new Set(); // 使用 let 或 const，確保它在整個 DOMContentLoaded 範圍內可見

	//datatable
	let table1 = new DataTable('#notificationSearchResultsTable');
    let table2 = new DataTable('#admin-table-member');

	$(document).ready(function() {
	        let memberTable = table2; // 使用 Lightbox 中的會員 Datatable 實例

	        // 全選 checkbox (Lightbox 內部的那個)
	        let selectAllCheckbox = document.getElementsByClassName("selectAllMembers")[0];

	        /**
	         * 函數：根據 selectedMemberIds 的狀態更新當前頁面 checkbox 的勾選狀態
	         */
	        function updateCheckboxesOnCurrentPage() {
	            console.log("--- 進入 updateCheckboxesOnCurrentPage ---");
	            console.log("目前 selectedMemberIds 大小:", selectedMemberIds.size);
	            let currentCheckboxes = memberTable.rows({ page: 'current' }).nodes().to$().find('.member-checkbox');

	            currentCheckboxes.each(function() {
	                const memberId = $(this).data('member-id'); // 從 data-member-id 屬性獲取 ID
	                // console.log("  更新當前頁面 checkbox ID:", memberId, "是否在 Set 中:", selectedMemberIds.has(memberId)); // 太詳細可選
	                if (selectedMemberIds.has(memberId)) {
	                    $(this).prop('checked', true);
	                } else {
	                    $(this).prop('checked', false);
	                }
	            });

	            // 更新全選 checkbox 的狀態：
	            // 如果當前頁面所有 checkbox 都被勾選，且當前頁面有 checkbox
	            const allCurrentPageChecked = Array.from(currentCheckboxes).every(cb => cb.checked);
	            const hasCheckboxesOnPage = currentCheckboxes.length > 0; // 判斷當前頁面是否有 checkbox
	            console.log("  當前頁面所有 checkbox 都勾選了嗎？", allCurrentPageChecked, "當前頁面有 checkbox 嗎？", hasCheckboxesOnPage);
	            selectAllCheckbox.checked = allCurrentPageChecked && hasCheckboxesOnPage;
	            console.log("--- 離開 updateCheckboxesOnCurrentPage ---");
	        }

	        /**
	         * 函數：初始化個別 checkbox 的事件監聽器 (主要綁定一次)
	         * 這個函數會在 Datatable 每次繪製完成後被呼叫，負責綁定事件和更新狀態
	         */
	        function initializeCheckboxListeners() {
	            // 獲取當前頁面所有 .member-checkbox，並綁定事件
	            let currentTableCheckboxes = memberTable.rows({ page: 'current' }).nodes().to$().find('.member-checkbox');

	            // 移除之前可能存在的事件監聽器，避免重複綁定
	            currentTableCheckboxes.off('change.individual');

	            // 監聽個別 checkbox 的 change 事件
	            currentTableCheckboxes.on('change.individual', function() {
	                const memberId = $(this).data('member-id'); // 獲取當前 checkbox 的 ID
	                console.log("--- 個別 checkbox 點擊 ---");
	                console.log("點擊的 checkbox ID:", memberId, "目前狀態:", this.checked);
	                if (this.checked) {
	                    selectedMemberIds.add(memberId); // 如果勾選，加入 Set
	                } else {
	                    selectedMemberIds.delete(memberId); // 如果取消勾選，從 Set 移除
	                }
	                console.log("selectedMemberIds 目前內容 (個別點擊後):", Array.from(selectedMemberIds));
	                console.log("selectedMemberIds 大小 (個別點擊後):", selectedMemberIds.size);
	                updateCheckboxesOnCurrentPage();
	                console.log("--- 個別 checkbox 點擊結束 ---");
	            });
	        }


	        // 如果全選 checkbox 存在
	        if (selectAllCheckbox) {
	            $(selectAllCheckbox).off('change.selectAll'); // 移除舊監聽器

	            // 監聽全選/取消全選功能
	            selectAllCheckbox.addEventListener('change', function() {
	                const isChecked = this.checked;
	                console.log("--- 全選 checkbox 點擊 ---");
	                console.log("全選 checkbox 點擊。isChecked:", isChecked);
	                if (isChecked) {
	                    selectedMemberIds.clear(); // 先清空，確保不會有舊資料
	                    console.log("selectedMemberIds 已清空 (全選操作前)。");
	                    // 遍歷所有行的 DOM 節點來獲取 data-member-id
	                    memberTable.rows().nodes().each(function(rowNode) {
	                        const checkbox = $(rowNode).find('.member-checkbox');
	                        if (checkbox.length > 0) { // 確保行中存在 checkbox
	                            const memberId = checkbox.data('member-id');
	                            console.log("  處理表格行。找到 checkbox, ID:", memberId);
	                            if (memberId !== undefined) { // 確保 memberId 有效
	                                selectedMemberIds.add(memberId);
	                            }
	                        }
	                    });
	                } else {
	                    // 取消全選：清空 selectedMemberIds
	                    selectedMemberIds.clear();
	                    console.log("selectedMemberIds 已清空 (取消全選操作)。");
	                }
	                console.log("selectedMemberIds 最終內容 (全選點擊後):", Array.from(selectedMemberIds));
	                console.log("selectedMemberIds 最終大小 (全選點擊後):", selectedMemberIds.size);
	                updateCheckboxesOnCurrentPage();
	                console.log("--- 全選 checkbox 點擊結束 ---");
	            }, false);
	        }

	        // 當 DataTables 繪製完成後（例如分頁、排序、搜尋），更新 checkbox 狀態和綁定監聽器
	        memberTable.on('draw.dt', function() {
	            // 每次繪製完成後，確保當前頁面的 checkbox 狀態正確
	            updateCheckboxesOnCurrentPage();
	            // 並重新綁定當前頁面 checkbox 的事件監聽器
	            initializeCheckboxListeners();
	        });

	        // 頁面首次載入時，執行初始化
	        initializeCheckboxListeners(); // 首次載入時綁定個別 checkbox 的監聽器
	        updateCheckboxesOnCurrentPage(); // 首次載入時更新 checkbox 狀態和全選框
		});
		
		// Lightbox elements
			const Btn = document.getElementById('pickMemberBtn');
			const lightboxOverlay = document.getElementById('lightboxOverlay');
			const articleLightBox = document.getElementById('articleLightBox');
			const closeLightboxBtn = document.getElementById('closeLightboxBtn');


			// Show lightbox
			Btn.addEventListener('click', () => {
				lightboxOverlay.style.display = 'block';
				articleLightBox.style.display = 'block';
			});

			// Hide lightbox
			closeLightboxBtn.addEventListener('click', () => {
				lightboxOverlay.style.display = 'none';
				articleLightBox.style.display = 'none';
				let selectNum = document.getElementById("selectNum");
				let checkedCount = selectedMemberIds.size;
				console.log("關閉 Lightbox。最終 selectedMemberIds 大小:", checkedCount);
				selectNum.innerHTML = "共選" + checkedCount + "位會員";
			});

			lightboxOverlay.addEventListener('click', () => {
				lightboxOverlay.style.display = 'none';
				articleLightBox.style.display = 'none';
				let selectNum = document.getElementById("selectNum");
				let checkedCount = selectedMemberIds.size;
				console.log("點擊 Lightbox 遮罩。最終 selectedMemberIds 大小:", checkedCount);
				selectNum.innerHTML = "共選" + checkedCount + "位會員";
			});

});