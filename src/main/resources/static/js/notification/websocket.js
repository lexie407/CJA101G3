var stompClient = null;
       var unreadNotificationsCount = 0; // 未讀通知數量

       // **重要：獲取當前用戶的ID**
       // 在真實應用中，這個 'currentUserId' 應該從後端 Thymeleaf 模型中動態獲取，
       // 例如：var currentUserId = '[[${loggedInUserId}]]';
       // 這裡為了示範，我們假設一個固定的用戶ID。
       // 請確保這個ID與後端 SimpMessagingTemplate.convertAndSendToUser 方法中使用的ID一致。
	   var currentUserIdNav = null;
	   if(document.getElementById("currentUserNav")){
		currentUserIdNav = (document.getElementById("currentUserNav").value).toString();
	   }
	   var currentUserId = currentUserIdNav; // 範例用戶ID，請替換為實際登入用戶的ID

       // 頁面元素引用
       var notificationButtonHover = document.getElementById('notificationButtonHover');
       var notificationButton = document.getElementById('notificationButton');
       var realtimeNotificationsDiv = document.getElementById('realtimeNotifications');

       // 連接 WebSocket 服務
       function connectWebSocket() {
           var socket = new SockJS('/ws'); // 連接到 Spring Boot 定義的 WebSocket 端點
           stompClient = Stomp.over(socket);
           stompClient.connect({}, function (frame) {
               console.log('WebSocket 已連接: ' + frame);
               console.log('當前用戶訂閱ID:', currentUserId);

               // 訂閱針對當前用戶的私人通知佇列
               // 訊息會被路由到 /user/{currentUserId}/queue/notifications
			   stompClient.subscribe('/topic/notifications/' + currentUserId, function (message) {
			       var notification = JSON.parse(message.body);
			       handleNewNotification(notification);
			   });

           }, function (error) {
               console.error('WebSocket 連接錯誤:', error);
           });
       }

       // 處理新接收到的通知
       function handleNewNotification(notification) {
           console.log("收到新通知:", notification);
		   const onNotificationPage = window.location.pathname === "/notification/getMemNoti";
		   if(!onNotificationPage){
				unreadNotificationsCount++; 
				updateNotificationUI(); 
                // 使用 sessionStorage 保存狀態
                sessionStorage.setItem('hasUnreadNotifications', 'true');
                sessionStorage.setItem('unreadCount', unreadNotificationsCount);
		   }
       }

       // 更新通知按鈕和徽章的 UI
       function updateNotificationUI() {
           if (unreadNotificationsCount > 0) {
               notificationButton.classList.add('has_new_noti');
			   notificationButtonHover.innerHTML = `你有${unreadNotificationsCount}筆新通知`;
           } else {
               notificationButton.classList.remove('has_new_noti');
           }
       }

       // 當點擊通知按鈕時
       notificationButton.addEventListener('click', function() {
           console.log("通知按鈕被點擊。");
           resetNotifications();
       });

       // 重置未讀通知狀態
       function resetNotifications() {
           unreadNotificationsCount = 0;
           // 清除 sessionStorage
           sessionStorage.removeItem('hasUnreadNotifications');
           sessionStorage.removeItem('unreadCount');
           updateNotificationUI();
           // 實際應用中可能需要後端交互
       }
	   
	   // **新增此函數：斷開 WebSocket 連接**
	   function disconnect() {
	       if (stompClient !== null) {
	           stompClient.disconnect();
	           console.log("WebSocket 已斷開連接。");
	       }
	   }

       // 頁面載入時執行的邏輯
       window.onload = function() {
           if (currentUserId) {
               connectWebSocket();
           }
           // 從 sessionStorage 恢復狀態
           if (sessionStorage.getItem('hasUnreadNotifications') === 'true') {
               unreadNotificationsCount = parseInt(sessionStorage.getItem('unreadCount'), 10) || 0;
               updateNotificationUI();
           }
       };

       // 頁面關閉或導航離開時斷開 WebSocket 連接
       window.onbeforeunload = disconnect;
	   
	   //泡泡
	   document.addEventListener('DOMContentLoaded', function () {
	       function setupHoverBubble(triggerId, bubbleId) {
	           const triggerEl = document.getElementById(triggerId);
	           const bubbleEl = document.getElementById(bubbleId);

	           if (!triggerEl || !bubbleEl) return;

	           triggerEl.addEventListener('mouseenter', function () {
	               const triggerRect = triggerEl.getBoundingClientRect();
	               const parentRect = triggerEl.parentElement.getBoundingClientRect();
	               const bubbleHeight = bubbleEl.offsetHeight || 24;
	               const bubbleWidth = bubbleEl.offsetWidth || 60;

	               // 顯示泡泡（先顯示才能抓尺寸）
	               bubbleEl.classList.add('show');

	               // top：放在 icon 上方 10px
	               const top = (triggerRect.top - parentRect.top) - bubbleHeight - 10;

	               // left：水平置中於 icon
	               let left = (triggerRect.left - parentRect.left) + (triggerRect.width / 2) - (bubbleWidth / 2);

	               // 邊界控制，不可超出左側與右側
	               if (left < 0) left = 0;
	               if (left + bubbleWidth > parentRect.width) {
	                   left = parentRect.width - bubbleWidth;
	               }

	               // 設定定位
	               bubbleEl.style.top = `${top}px`;
	               bubbleEl.style.left = `${left}px`;
	           });

	           triggerEl.addEventListener('mouseleave', function () {
	               bubbleEl.classList.remove('show');
	           });
	       }

	       // 初始化泡泡提示
	       setupHoverBubble('chatWithMe', 'chatWithMeHover');
	       setupHoverBubble('notificationButton', 'notificationButtonHover');
	   });
	   
	   //登入按鈕
	   let loginBtn = null;
	   if(document.getElementsByClassName("logout-btn").length > 0){
			loginBtn = document.getElementsByClassName("logout-btn")[0];
	   
	   loginBtn.addEventListener("click", function(){
			window.location.href="/members/login"
	   });
	   
	   }
