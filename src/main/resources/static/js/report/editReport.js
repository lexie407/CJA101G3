document.addEventListener("DOMContentLoaded", function(){
	let back_btn = document.getElementById("backToAdminNotificationsBtn");
	back_btn.addEventListener("click", function(){
		window.location.href = "/report/allReportList";
	});
});