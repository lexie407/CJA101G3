document.addEventListener("DOMContentLoaded", function(){
	let back_btn = document.getElementById("backToAdminNotificationsBtn");
	back_btn.addEventListener("click", function(){
		window.location.href = "/CommentsReports/allReportList";
	});
	
	let unDo_btn = document.getElementById("unDo");
	unDo_btn.addEventListener("click", function(){
		
		console.log(document.getElementById("notificationContent").value);
		
		if(document.getElementById("notificationContent").value == ""){
			alert("結案說明不能空白!");
			return;
		}
		
		let form = document.createElement("form");
			form.method = "post";
			form.action = "/CommentsReports/unEstablished";
			
			let input1 = document.createElement("input");
			input1.type = "hidden";
			input1.name = "commRepId";
			input1.value = document.getElementById("commRepId").value;
			form.appendChild(input1);
			
			let input2 = document.createElement("input");
			input2.type = "hidden";
			input2.name = "remarks";
			input2.value = document.getElementById("notificationContent").value;
			form.appendChild(input2);
				
			document.body.appendChild(form);
			form.submit();
	})

});