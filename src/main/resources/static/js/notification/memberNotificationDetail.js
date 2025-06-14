document.addEventListener('DOMContentLoaded', function(){
   let backBtn = document.getElementsByClassName("back-to-list-btn")[0];
   backBtn.addEventListener("click", function(){
		let backForm = document.createElement("form");
		backForm.method = "get";
		backForm.action = "/notification/getMemNoti";
				
//		let actionInput = document.createElement("input");
//		actionInput.type = "hiddden";
//		actionInput.name = "action";
//		actionInput.value = "removeNotibyMember";
//		deleleForm.appendChild(actionInput);

		document.body.appendChild(backForm);
		backForm.submit();

      });
	  
	let deleteBtn = document.getElementsByClassName("delete-btn")[0];
	deleteBtn.addEventListener("click", function(){
		if(confirm("確定刪除嗎?")){
			let formAction = deleteBtn.getAttribute("data-action");
			let notiId = deleteBtn.getAttribute("data-notiId");
			
			let deleleForm = document.createElement("form");
			deleleForm.method = "post";
			deleleForm.action = formAction;
			
			let actionInput = document.createElement("input");
			actionInput.type = "hiddden";
			actionInput.name = "action";
			actionInput.value = "removeNotibyMember";
			deleleForm.appendChild(actionInput);
			
			let notiInput = document.createElement("input");
			notiInput.type = "hidden";
			notiInput.name = "notiIds";
			notiInput.value = notiId;
			deleleForm.appendChild(notiInput);
			
			document.body.appendChild(deleleForm);
			deleleForm.submit();
		}
	})  
});