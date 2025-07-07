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

               // (可選) 如果您也有公共廣播通知，可以在這裡訂閱
               // stompClient.subscribe('/topic/public-announcements', function (message) {
               //     var announcement = JSON.parse(message.body);
               //     displayRealtimeNotification("📢 公告: " + announcement.content);
               //     // 公告可能不計入未讀數，依據需求而定
               // });

           }, function (error) {
               console.error('WebSocket 連接錯誤:', error);
               // 這裡可以添加重試邏輯，例如 setTimeout(connectWebSocket, 5000);
           });
       }

       // 處理新接收到的通知
       function handleNewNotification(notification) {
           console.log("收到新通知:", notification);
		   // 檢查當前是否在通知列表頁
		   const onNotificationPage = window.location.pathname === "/notification/getMemNoti";
		   console.log(onNotificationPage);
		   if(!onNotificationPage){
				unreadNotificationsCount++; // 未讀通知數量增加
				updateNotificationUI(); // 更新 UI
		   }
           
//           displayRealtimeNotification("🔔 新通知給 " + notification.recipientUserId + ": " + notification.content + " (預計時間: " + (notification.sendTime ? new Date(notification.sendTime).toLocaleString() : '即時') + ")");
           
           // 可以選擇播放音效或發出瀏覽器通知
//            playNotificationSound();
//            showBrowserNotification(notification.content);
       }

       // 更新通知按鈕和徽章的 UI
       function updateNotificationUI() {
           if (unreadNotificationsCount > 0) {
               notificationButton.classList.add('has_new_noti'); // 添加 has_new_noti 類別，使其變色
			   notificationButtonHover.innerHTML = `你有${unreadNotificationsCount}筆新通知`;
           } else {
//               notificationButton.classList.remove('has_new_noti'); // 移除 has_new_noti 類別
//			   notificationButton.innerHTML = `<span class="material-icons">notifications</span>通知`;
           }
       }

       // 顯示即時通知到頁面列表
//       function displayRealtimeNotification(message) {
//           var p = document.createElement('p');
//           p.className = 'notification-item'; // 可以為此添加 CSS 樣式
//           p.textContent = message;
//           realtimeNotificationsDiv.prepend(p); // 新通知顯示在最上方
//           // 控制顯示的通知數量
//           while (realtimeNotificationsDiv.children.length > 10) {
//               realtimeNotificationsDiv.removeChild(realtimeNotificationsDiv.lastChild);
//           }
//       }

       // 當點擊通知按鈕時
       notificationButton.addEventListener('click', function() {
           console.log("通知按鈕被點擊。");
           // 在實際應用中，這裡會導航到通知列表頁面，或者彈出一個模態框顯示通知。
           // 點擊後，我們假設用戶已經「讀取」了通知，所以重置計數器和 UI
           resetNotifications();
           // 範例：導航到另一個通知列表頁面 (假設有 /user/notifications-list 這個頁面)
           // window.location.href = "/user/notifications-list";
       });

       // 重置未讀通知狀態
       function resetNotifications() {
           unreadNotificationsCount = 0;
           updateNotificationUI();
           // 在實際應用中，這裡還需要發送一個請求給後端，將資料庫中的通知標記為已讀。
           // fetch('/api/notifications/mark-all-as-read?userId=' + currentUserId, { method: 'POST' })
           //     .then(response => console.log('所有通知已標記為已讀'))
           //     .catch(error => console.error('標記已讀失敗:', error));
       }
	   
	   // **新增此函數：斷開 WebSocket 連接**
	   function disconnect() {
	       if (stompClient !== null) {
	           stompClient.disconnect();
	           console.log("WebSocket 已斷開連接。");
	       }
	   }

       // 頁面載入時自動連接 WebSocket
       window.onload = function() {
           connectWebSocket();
           // 初始化 UI 狀態
           updateNotificationUI(); 
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
