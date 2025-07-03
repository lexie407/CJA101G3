document.addEventListener("DOMContentLoaded", function(){
	let back_btn = document.getElementById("backToAdminNotificationsBtn");
	back_btn.addEventListener("click", function(){
		window.location.href = "/Articlereport/allReportList";
	});
	
	// Lightbox elements
	const articleBtn = document.getElementById('articleBtn');
	const lightboxOverlay = document.getElementById('lightboxOverlay');
	const articleLightBox = document.getElementById('articleLightBox');
	const closeLightboxBtn = document.getElementById('closeLightboxBtn');

	// Show lightbox
	articleBtn.addEventListener('click', () => {
	    lightboxOverlay.style.display = 'block';
	    articleLightBox.style.display = 'block';
	});

	// Hide lightbox
	closeLightboxBtn.addEventListener('click', () => {
	    lightboxOverlay.style.display = 'none';
	    articleLightBox.style.display = 'none';
	});

	lightboxOverlay.addEventListener('click', () => {
	    lightboxOverlay.style.display = 'none';
	    articleLightBox.style.display = 'none';
	});
	
	
	let unDo_btn = document.getElementById("unDo");
	unDo_btn.addEventListener("click", function(){
		
		if(document.getElementById("notificationContent").value == ""){
					alert("結案說明不能空白!");
					return;
				}
		
		let form = document.createElement("form");
			form.method = "post";
			form.action = "/Articlereport/unEstablished";
			
			let input1 = document.createElement("input");
			input1.type = "hidden";
			input1.name = "artRepId";
						input1.value = document.getElementById("artRepId").value; // Changed from commRepId to artRepId
			form.appendChild(input1);
			
			let input2 = document.createElement("input");
			input2.type = "hidden";
			input2.name = "remark";
			input2.value = document.getElementById("notificationContent").value;
			form.appendChild(input2);
				
			document.body.appendChild(form);
			form.submit();
	});

});