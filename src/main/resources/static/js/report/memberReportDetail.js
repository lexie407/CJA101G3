document.addEventListener("DOMContentLoaded", function(){
	let back_btn = document.getElementsByClassName("back-to-list-btn material-button")[0];
	back_btn.addEventListener("click", function(){
		history.back();
	})
});