document.addEventListener("DOMContentLoaded", function() {

	let deleteBtn = document.getElementById("deleteAdminNotificationBtn");
	//刪除按鈕動作綁定，整理準備通知資料給Servlet
	if(deleteBtn != null){
		deleteBtn.addEventListener("click", function() {
				if (confirm("確定刪除嗎?")){
					let form = document.createElement("form");
					form.method = "post";
					form.action = "/notification/deleteNotification";

					let notiInput = document.createElement("input");
					notiInput.type = "input";
					notiInput.name = "notiId";
					notiInput.value = deleteBtn.getAttribute("data-notiId");
					form.appendChild(notiInput);

					document.body.appendChild(form);
					form.submit();
				}

			});
	}
	
	//綁定返回按鈕
	let backBtn = document.getElementById("backToAdminNotificationsBtn");
	backBtn.addEventListener("click", function(){
		if(sessionStorage.getItem("oriUrl") == "/notification/unSendNotification"){
			window.location.href = "/notification/unSendNotification"
		}else{
			window.location.href = "/notification/backDoSearchNotification";
		}
		
	})

});